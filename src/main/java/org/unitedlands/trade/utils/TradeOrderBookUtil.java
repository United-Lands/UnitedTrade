package org.unitedlands.trade.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.unitedlands.UnitedLib;
import org.unitedlands.factories.items.IItemFactory;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.classes.MessageProvider;
import org.unitedlands.trade.classes.Order;
import org.unitedlands.trade.classes.OrderItem;
import org.unitedlands.utils.Formatter;
import org.unitedlands.utils.Logger;

import io.papermc.paper.registry.data.dialog.body.DialogBody;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class TradeOrderBookUtil {

    public static ItemStack createBook(Order order) {

        @SuppressWarnings("unused")
        MessageProvider messageProvider = UnitedTrade.getInstance().getMessageProvider();
        IItemFactory itemFactory = UnitedLib.getInstance().getItemFactory();

        var book = new ItemStack(Material.WRITTEN_BOOK);

        var bookMeta = (BookMeta) book.getItemMeta();

        var barterItems = order.getBarterItems();
        var requiredItems = order.getRequiredItems();

        bookMeta.setAuthor(order.getCustomer());

        // Add hidden enchantment to make the item have the enchanted glow effect
        bookMeta.addEnchant(Enchantment.LURE, 1, false);
        bookMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        var orderNo = String.format("%08d", order.getOrderNo());
        bookMeta.displayName(Component.text("Order No " + orderNo));

        var itemsString = "";
        for (var orderItem : requiredItems) {
            itemsString += itemFactory.getFilterName(orderItem.getItem()) + "#" + orderItem.getMinAmount() + "#"
                    + orderItem.getMaxAmount() + "#" + orderItem.getPrice() + ";";
        }

        var barterItemsString = "";
        for (var barterItem : barterItems) {
            barterItemsString += itemFactory.getFilterName(barterItem) + "#" + barterItem.getAmount() + ";";
        }

        PersistentDataContainer pdc = bookMeta.getPersistentDataContainer();
        pdc.set(getKey("tradebook.orderId"), PersistentDataType.STRING, order.getId().toString());
        pdc.set(getKey("tradebook.orderNo"), PersistentDataType.INTEGER, order.getOrderNo());
        pdc.set(getKey("tradebook.description"), PersistentDataType.STRING, order.getDescription());
        if (order.getTradepointId() != null)
            pdc.set(getKey("tradebook.tradepointId"), PersistentDataType.STRING, order.getTradepointId().toString());
        pdc.set(getKey("tradebook.requiredItems"), PersistentDataType.STRING, itemsString);
        pdc.set(getKey("tradebook.barterItems"), PersistentDataType.STRING, barterItemsString);
        pdc.set(getKey("tradebook.timelimit"), PersistentDataType.LONG, order.getTimelimit());
        pdc.set(getKey("tradebook.penalty"), PersistentDataType.DOUBLE, order.getPenalty());
        pdc.set(getKey("tradebook.barter"), PersistentDataType.BOOLEAN, order.isBarter());
        pdc.set(getKey("tradebook.tradeorderbook"), PersistentDataType.INTEGER, 1);

        book.setItemMeta(bookMeta);

        return book;
    }

    public static Order getOrder(ItemStack book) {
        var order = new Order();
        ItemMeta meta = book.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        try {
            order.setId(UUID.fromString(pdc.get(getKey("tradebook.orderId"), PersistentDataType.STRING)));
            order.setOrderNo(pdc.get(getKey("tradebook.orderNo"), PersistentDataType.INTEGER));
            order.setDescription(pdc.get(getKey("tradebook.description"), PersistentDataType.STRING));
            order.setTradepointId(
                    UUID.fromString(pdc.get(getKey("tradebook.tradepointId"), PersistentDataType.STRING)));
            order.setBarter(pdc.get(getKey("tradebook.barter"), PersistentDataType.BOOLEAN));
            order.setTimelimit(pdc.get(getKey("tradebook.timelimit"), PersistentDataType.LONG));
            order.setPenalty(pdc.get(getKey("tradebook.penalty"), PersistentDataType.DOUBLE));
            order.setRequiredItems(getRequiredItems(pdc));
            order.setBarterItems(getBarterItems(pdc));
        } catch (Exception ex) {
            Logger.logError("Could not parse data from order book: ", ex.getMessage());
            return null;
        }

        return order;

    }

    private static List<OrderItem> getRequiredItems(PersistentDataContainer pdc) {
        var itemFactory = UnitedLib.getInstance().getItemFactory();
        try {
            var result = new ArrayList<OrderItem>();
            var list = pdc.get(getKey("tradebook.requiredItems"), PersistentDataType.STRING);
            var itemsAmounts = list.split(";");
            for (var itemAmount : itemsAmounts) {
                var split = itemAmount.split("#");
                if (split.length == 4) {
                    result.add(new OrderItem(itemFactory.getItemStack(split[0], 1), Integer.parseInt(split[1]),
                            Integer.parseInt(split[2]), Double.parseDouble(split[3])));
                }
            }
            return result;
        } catch (Exception ex) {
            Logger.logError("Failed trade book required item parsing.", "UnitedTrade");
            return null;
        }
    }

    private static List<ItemStack> getBarterItems(PersistentDataContainer pdc) {
        var itemFactory = UnitedLib.getInstance().getItemFactory();
        try {
            var result = new ArrayList<ItemStack>();
            var list = pdc.get(getKey("tradebook.barterItems"), PersistentDataType.STRING);
            var itemsAmounts = list.split(";");
            for (var itemAmount : itemsAmounts) {
                var split = itemAmount.split("#");
                if (split.length == 2) {
                    result.add(itemFactory.getItemStack(split[0], Integer.parseInt(split[1])));
                }
            }
            return result;
        } catch (Exception ex) {
            Logger.logError("Failed trade book barter item parsing.", "UnitedTrade");
            return null;
        }
    }

    public static boolean isTradeOrderBook(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return false;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        try {
            return pdc.get(getKey("tradebook.tradeorderbook"), PersistentDataType.INTEGER) == 1;
        } catch (Exception ex) {
            return false;
        }
    }

    private static NamespacedKey getKey(String name) {
        return new NamespacedKey(UnitedTrade.getInstance(), name);
    }

    public static String getFloodgatePanelContent(ItemStack book) {

        MessageProvider messageProvider = UnitedTrade.getInstance().getMessageProvider();
        IItemFactory itemFactory = UnitedLib.getInstance().getItemFactory();

        var order = getOrder(book);

        String content = "§7" + order.getDescription() + "§r\n\n";
        if (!order.isBarter()) {

            var minPayout = order.getMinPayout();
            var maxPayout = order.getMaxPayout();
            var payoutStr = minPayout != maxPayout
                    ? String.format("%,.2f", minPayout) + "-" + String.format("%,.2f", maxPayout)
                    : String.format("%,.2f", minPayout);

            content += "§l" + messageProvider.get("messages.tradebook.payment") + ": §2" + payoutStr + "§r\n";
            content += "§l" + messageProvider.get("messages.tradebook.penalty") + ": §c"
                    + String.format("%,.2f", order.getPenalty()) + "§r\n";
        }
        if (!order.getBarterItems().isEmpty()) {
            var barterItemStr = "";
            for (var item : order.getBarterItems()) {
                String material = itemFactory.getDisplayName(item);
                String amount = item.getAmount() + "";
                barterItemStr += "§6" + amount + "§7x " + material + "\n";
            }
            content += "§l" + messageProvider.get("messages.tradebook.barter") + ":" + "§r\n";
            content += barterItemStr;
        }

        content += "§l" + messageProvider.get("messages.tradebook.timelimit") + ": §6"
                + Formatter.formatDuration(order.getTimelimit()) + "§r\n\n";

        var itemStr = "";
        for (var orderItem : order.getRequiredItems()) {
            String material = itemFactory.getDisplayName(orderItem.getItem());
            String minAmount = orderItem.getMinAmount() + "";
            String maxAmount = orderItem.getMaxAmount() + "";
            String amountStr = minAmount != maxAmount ? minAmount + "-" + maxAmount : minAmount + "";
            itemStr += "§6" + amountStr + "§7x " + material + "\n";
        }
        content += "§l" + messageProvider.get("messages.tradebook.required-items") + ":" + "§r\n\n";
        content += itemStr;

        return content;
    }

    public static List<DialogBody> getJavaPanelContent(ItemStack book) {

        MessageProvider messageProvider = UnitedTrade.getInstance().getMessageProvider();

        List<DialogBody> dialogBody = new ArrayList<>();

        var order = getOrder(book);

        var miniMessage = MiniMessage.miniMessage();
        var itemFactory = UnitedLib.getInstance().getItemFactory();

        // Description
        dialogBody.add(
                DialogBody.plainMessage(miniMessage.deserialize("<gray>" + order.getDescription() + "</gray><br>")));

        // Payout / Penalty
        if (!order.isBarter()) {
            var payoutLines = "";
            var minPayout = order.getMinPayout();
            var maxPayout = order.getMaxPayout();
            var payoutStr = minPayout != maxPayout
                    ? String.format("%,.2f", minPayout) + "-" + String.format("%,.2f", maxPayout)
                    : String.format("%,.2f", minPayout);

            payoutLines += "<bold>" + messageProvider.get("messages.tradebook.payment") + ": </bold><dark_green>"
                    + payoutStr + "</dark_green><br>" +
                    "<bold>" + messageProvider.get("messages.tradebook.penalty") + ": </bold><red>"
                    + String.format("%,.2f", order.getPenalty()) + "</red><br>";

            dialogBody.add(DialogBody.plainMessage(miniMessage.deserialize(payoutLines)));
        }

        // Time Limit
        dialogBody.add(DialogBody.plainMessage(miniMessage
                .deserialize("<bold>" + messageProvider.get("messages.tradebook.timelimit") + ": </bold><gold>"
                        + Formatter.formatDuration(order.getTimelimit()) + "<gold>")));

        // Barter Items
        var barterItems = new ArrayList<>(order.getBarterItems());
        if (!barterItems.isEmpty()) {
            dialogBody.add(DialogBody.plainMessage(
                    miniMessage.deserialize("<bold>" + messageProvider.get("messages.tradebook.barter") + ":")));

            for (var item : barterItems) {
                String material = itemFactory.getDisplayName(item);
                var amount = item.getAmount() + "";
                item.setAmount(1);
                dialogBody.add(DialogBody.item(item,
                        DialogBody.plainMessage(
                                miniMessage.deserialize("<gold>" + amount + "</gold><gray>x</gray> " + material)),
                        true, true, 18, 16));
            }
        }

        // Required Items
        var requiredItems = new ArrayList<>(order.getRequiredItems());
        dialogBody.add(DialogBody.plainMessage(
                miniMessage.deserialize("<bold>" + messageProvider.get("messages.tradebook.required-items") + ":")));
        for (var item : requiredItems) {
            var minAmount = item.getMinAmount() + "";
            var maxAmount = item.getMaxAmount() + "";
            var amountStr = !minAmount.equals(maxAmount) ? minAmount + "-" + maxAmount : minAmount + "";
            item.getItem().setAmount(1);
            String material = itemFactory.getDisplayName(item.getItem());
            dialogBody.add(DialogBody.item(item.getItem(),
                    DialogBody.plainMessage(
                            miniMessage.deserialize("<gold>" + amountStr + "</gold><gray>x</gray> " + material)),
                    true, true, 18, 16));
        }
        
        return dialogBody;
    }

    public static List<ItemStack> getMissingItems(Player player, List<OrderItem> required) {
        List<ItemStack> missing = new ArrayList<>();

        var inventory = player.getInventory();

        for (OrderItem requiredItem : required) {
            var missingAmount = requiredItem.missingAmount(inventory);
            if (missingAmount > 0) {
                var clone = requiredItem.getItem().clone();
                clone.setAmount(missingAmount);
                missing.add(clone);
            }
        }
        return missing;
    }

}
