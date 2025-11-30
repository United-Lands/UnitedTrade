package org.unitedlands.trade.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.classes.MessageProvider;
import org.unitedlands.trade.utils.TradeOrderBookUtil;
import org.unitedlands.utils.Messenger;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class DropoffPointListener implements Listener {

    private final UnitedTrade plugin;
    private final MessageProvider messageProvider;

    public DropoffPointListener(UnitedTrade plugin, MessageProvider messageProvider) {
        this.plugin = plugin;
        this.messageProvider = messageProvider;
    }

    @EventHandler
    public void onDropoffInteract(PlayerInteractEvent event) {

        if (event.getClickedBlock() == null)
            return;

        if (event.getClickedBlock().getType() != Material.TRAPPED_CHEST)
            return;

        var block = event.getClickedBlock();
        var dropffPoint = plugin.getDropoffPointManager().getDropoffPoint(block);
        if (dropffPoint == null)
            return;

        event.setCancelled(true);

        Player player = (Player) event.getPlayer();

        var book = player.getInventory().getItemInMainHand();
        if (book == null || !TradeOrderBookUtil.isTradeOrderBook(book)) {
            Messenger.sendMessage(player, messageProvider.get("messages.errors.must-hold-trade-book"), null,
                    messageProvider.get("messages.prefix"));
            return;
        }

        var tradepointId = TradeOrderBookUtil.getTradepointId(book);
        if (tradepointId != null && !dropffPoint.getTradepointId().equals(tradepointId)) {
            Messenger.sendMessage(player, messageProvider.get("messages.errors.wrong-tradepoint"), null,
                    messageProvider.get("messages.prefix"));
            return;
        }

        var orderTracker = plugin.getOrderTracker();

        var orderID = TradeOrderBookUtil.getOrderId(book);
        var trackedOrder = orderTracker.getTrackedOrder(orderID);

        if (trackedOrder == null)
        {
            Messenger.sendMessage(player, messageProvider.get("messages.errors.order-expired"), null,
                    messageProvider.get("messages.prefix"));
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            return;
        }
        
        var requiredItems = TradeOrderBookUtil.getRequiredItems(book);
        List<ItemStack> missingItems = TradeOrderBookUtil.getMissingItems(player, requiredItems);

        if (!missingItems.isEmpty()) {

            List<String> missing = new ArrayList<>();
            for (ItemStack item : missingItems) {
                String itemStr = "";
                ItemMeta meta = item.getItemMeta();
                Component displayName = meta.displayName();
                if (displayName != null) {
                    itemStr += PlainTextComponentSerializer.plainText().serialize(displayName);
                } else {
                    itemStr = TradeOrderBookUtil.formatReadable(item.getType().toString());
                }
                itemStr += " x" + item.getAmount();
                missing.add(itemStr);
            }

            Messenger.sendMessage(player, messageProvider.get("messages.checkorder.missing"),
                    Map.of("missing", String.join(", ", missing)), messageProvider.get("messages.prefix"));

        } else {

            var inventory = player.getInventory();
            var itemsToRemove = TradeOrderBookUtil.getRequiredItems(book);
            for (ItemStack itemStack : itemsToRemove) {
                removeItems(inventory, itemStack.getType(), itemStack.getAmount());
            }
            inventory.setItemInMainHand(new ItemStack(Material.AIR));

            var price = TradeOrderBookUtil.getPrice(book);
            var orderNo = TradeOrderBookUtil.getOrderNo(book);
            orderTracker.handleCompletedOrder(player, tradepointId, orderNo, price);

            Location loc = block.getLocation().clone().add(0.5, 0.5, 0.5);
            block.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 16, 0.5, 0.5, 0.5);
            block.getWorld().playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 8f, 1f);

            orderTracker.removeTrackedOrder(TradeOrderBookUtil.getOrderId(book));

            Messenger.sendMessage(player, messageProvider.get("messages.checkorder.complete"), null,
                    messageProvider.get("messages.prefix"));

        }

    }

    public static boolean removeItems(Inventory inventory, Material material, int amount) {
        int toRemove = amount;

        for (ItemStack item : inventory.getContents()) {
            if (item == null)
                continue;
            if (item.getType() != material)
                continue;

            int stackAmount = item.getAmount();

            if (stackAmount > toRemove) {
                item.setAmount(stackAmount - toRemove);
                return true;
            } else {
                inventory.removeItem(item);
                toRemove -= stackAmount;

                if (toRemove == 0)
                    return true;
            }
        }

        return false;
    }

}
