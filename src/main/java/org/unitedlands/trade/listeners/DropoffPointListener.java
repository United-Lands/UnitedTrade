package org.unitedlands.trade.listeners;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.unitedlands.UnitedLib;
import org.unitedlands.factories.items.IItemFactory;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.classes.DropoffPoint;
import org.unitedlands.trade.classes.OrderItem;
import org.unitedlands.trade.classes.ShopPoint;
import org.unitedlands.trade.classes.events.ShopOpenEvent;
import org.unitedlands.trade.utils.TradeOrderBookUtil;
import org.unitedlands.utils.United;

import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;

public class DropoffPointListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onDropoffInteract(PlayerInteractEvent event) {

        if (event.getClickedBlock() == null)
            return;

        if (event.getClickedBlock().getType() != Material.TRAPPED_CHEST)
            return;

        var block = event.getClickedBlock();
        var dropffPoint = UnitedTrade.instance().getDropoffPointManager().getDropoffPoint(block);
        if (dropffPoint != null) {
            handleDropoffPoint(event, block, dropffPoint);
            return;
        }
        var shopPoint = UnitedTrade.instance().getShopPointManager().getShopPoint(block);
        if (shopPoint != null) {
            handleShopPoint(event, block, shopPoint);
            return;
        }

    }

    private void handleShopPoint(PlayerInteractEvent event, @Nullable Block block, ShopPoint shopPoint) {

        if (shopPoint.getInventory() != null) {
            event.setCancelled(true);
            Player player = (Player) event.getPlayer();

            Inventory inventory = null;
            if (shopPoint.useGlobalInventory()) {
                inventory = shopPoint.getInventory();
            } else {
                inventory = shopPoint.getInventory(player);
            }

            if (inventory == null) {
                United.logger().error("Couldn't get inventory for shop point " + shopPoint.getId());
                return;
            }

            ShopOpenEvent openEvent = new ShopOpenEvent();
            openEvent.setPlayer(player);
            openEvent.setShopPoint(shopPoint);
            openEvent.callEvent();

            if (openEvent.isCancelled())
                return;

            player.openInventory(inventory);
        }

    }

    private void handleDropoffPoint(PlayerInteractEvent event, @Nullable Block block, DropoffPoint dropffPoint) {

        event.setCancelled(true);
        Player player = (Player) event.getPlayer();

        var book = player.getInventory().getItemInMainHand();
        if (book == null || !TradeOrderBookUtil.isTradeOrderBook(book)) {
            United.messenger().send(player, "errors.must-hold-trade-book");
            return;
        }

        var order = TradeOrderBookUtil.getOrder(book);

        var tradepointId = order.getTradepointId();
        if (tradepointId != null && !dropffPoint.getTradepointId().equals(tradepointId)) {
            United.messenger().send(player, "errors.wrong-tradepoint");
            return;
        }

        var orderTracker = UnitedTrade.instance().getOrderTracker();

        var orderID = order.getId();
        var trackedOrder = orderTracker.getTrackedOrder(orderID);

        if (trackedOrder == null) {
            United.messenger().send(player, "errors.order-expired");
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            return;
        }

        var requiredItems = order.getRequiredItems();
        var barterItems = order.getBarterItems();

        List<ItemStack> missingItems = TradeOrderBookUtil.getMissingItems(player, requiredItems);

        if (!missingItems.isEmpty()) {

            IItemFactory itemFactory = UnitedLib.getInstance().getItemFactory();

            List<String> missing = new ArrayList<>();
            for (ItemStack item : missingItems) {
                String itemStr = itemFactory.getDisplayName(item);
                itemStr += " x" + item.getAmount();
                missing.add(itemStr);
            }

            United.messenger().send(player, "checkorder.missing", String.join(", ", missing));

        } else {

            // Remove all order items

            var inventory = player.getInventory();
            double payout = 0f;

            for (OrderItem orderItem : requiredItems) {
                var removedItems = orderItem.removeFromInventory(inventory);
                payout += removedItems * orderItem.getPrice();
            }
            inventory.setItemInMainHand(new ItemStack(Material.AIR));

            // Handle payout

            if (order.getCommandOnComplete() != null) {
                var cmd = order.getCommandOnComplete();
                cmd = cmd.replace("PLAYER", player.getName());

                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);

                if (order.isCommandReplacesPayout()) {
                    orderTracker.handleCompletedOrder(player, tradepointId, trackedOrder.getOrderNo(), 0d, new ArrayList<>());
                } else {
                    // Do normal payout
                    orderTracker.handleCompletedOrder(player, tradepointId, trackedOrder.getOrderNo(), payout, barterItems);
                }
            } else {
                // Do normal payout
                orderTracker.handleCompletedOrder(player, tradepointId, trackedOrder.getOrderNo(), payout, barterItems);
            }

            // Completion Effects

            Particle completeParticle = Particle.HAPPY_VILLAGER;
            try {
                completeParticle = Registry.PARTICLE_TYPE.get(TypedKey.create(RegistryKey.PARTICLE_TYPE,
                        UnitedTrade.instance().getConfig().getString("effects.complete-particle")));
            } catch (Exception ignore) {
            }
            Sound completeSound = Sound.ENTITY_PLAYER_LEVELUP;
            Double completeVolume = 1.0;
            try {
                completeSound = Registry.SOUNDS.get(TypedKey.create(RegistryKey.SOUND_EVENT,
                        UnitedTrade.instance().getConfig().getString("effects.complete-sound")));
                completeVolume = UnitedTrade.instance().getConfig().getDouble("effects.complete-volume", 1.0);
            } catch (Exception ignore) {
            }

            Location loc = block.getLocation().clone().add(0.5, 0.5, 0.5);
            block.getWorld().spawnParticle(completeParticle, loc, 16, 0.5, 0.5, 0.5);
            block.getWorld().playSound(loc, completeSound, completeVolume.floatValue(), 1f);

            orderTracker.removeTrackedOrder(order.getId());

            if (payout > 0) {
                United.messenger().send(player, "checkorder.complete", UnitedTrade.instance().getEconomyProvider().format(payout));
            }
            else {
                United.messenger().send(player, "checkorder.complete-barter");
            }

        }
    }

    public static boolean removeItems(Inventory inventory, ItemStack itemToRemove, int amount) {
        int toRemove = amount;

        var itemFactory = UnitedLib.getInstance().getItemFactory();

        for (ItemStack inventoryItem : inventory.getContents()) {
            if (inventoryItem == null)
                continue;
            if (!itemFactory.isItem(itemToRemove, inventoryItem))
                continue;

            int stackAmount = inventoryItem.getAmount();

            if (stackAmount > toRemove) {
                inventoryItem.setAmount(stackAmount - toRemove);
                return true;
            } else {
                inventory.removeItem(inventoryItem);
                toRemove -= stackAmount;

                if (toRemove == 0)
                    return true;
            }
        }

        return false;
    }

}
