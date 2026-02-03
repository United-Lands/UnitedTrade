package org.unitedlands.trade.listeners;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.unitedlands.UnitedLib;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.classes.ShopPoint;
import org.unitedlands.utils.Formatter;
import org.unitedlands.utils.Logger;
import org.unitedlands.utils.Messenger;

public class InventoryListener implements Listener {

    @SuppressWarnings("unused")
    private final UnitedTrade plugin;
    private final IMessageProvider messageProvider;

    public InventoryListener(UnitedTrade plugin, IMessageProvider messageProvider) {
        this.plugin = plugin;
        this.messageProvider = messageProvider;
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {

        if (event.getInventory().getHolder() instanceof ShopPoint shopPoint) {

            event.setCancelled(true);

            var inventory = event.getInventory();

            if (event.getRawSlot() < inventory.getSize()) {

                var item = inventory.getItem(event.getRawSlot());
                if (item != null && item.getType() != Material.AIR) {
                    
                    var meta = item.getItemMeta();
                    PersistentDataContainer pdc = meta.getPersistentDataContainer();
                    var price = pdc.get(new NamespacedKey(UnitedTrade.getInstance(), "shopitem.price"),
                            PersistentDataType.DOUBLE);

                    Player player = (Player) event.getWhoClicked();

                    if (price > 0) {
                        var economy = plugin.getEconomyProvider();
                        if (economy.getPlayerBalance(player) >= price) {
                            economy.takeMoneyFromPlayer(player, price, "Shop purchase");
                        } else {
                            Messenger.sendMessage(player, messageProvider.get("messages.shop.insufficient-funds"), null,
                                    messageProvider.get("messages.prefix"));
                            return;
                        }

                        var itemFactory = UnitedLib.getInstance().getItemFactory();
                        var itemId = itemFactory.getFilterName(item);
                        var newItem = itemFactory.getItemStack(itemId, item.getAmount());
                        if (newItem == null) {
                            Logger.logError("Could not create instance of " + itemId, "UnitedTrade");
                            return;
                        }

                        var leftOver = player.getInventory().addItem(newItem);
                        if (!leftOver.isEmpty())
                            for (var leftoverItemEntry : leftOver.entrySet())
                                shopPoint.getLocation().getWorld().dropItemNaturally(shopPoint.getLocation(),
                                        leftoverItemEntry.getValue());

                        inventory.setItem(event.getRawSlot(), new ItemStack(Material.AIR));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {

        if (event.getInventory().getHolder() instanceof ShopPoint shopPoint) {
            var player = (Player) event.getPlayer();

            String remainingTime = Formatter.formatDuration(shopPoint.getTimeToRefresh(player));
            Messenger.sendMessage(player, messageProvider.get("messages.shop.remaining-time"),
                    Map.of("remaining", remainingTime), messageProvider.get("messages.prefix"));
        }

    }

}
