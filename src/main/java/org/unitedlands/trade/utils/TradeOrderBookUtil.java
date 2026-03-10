package org.unitedlands.trade.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.unitedlands.utils.Formatter;
import org.unitedlands.utils.Logger;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class TradeOrderBookUtil {

    public static ItemStack createBook(Order order) {

        MessageProvider messageProvider = UnitedTrade.getInstance().getMessageProvider();
        IItemFactory itemFactory = UnitedLib.getInstance().getItemFactory();

        var book = new ItemStack(Material.WRITTEN_BOOK);

        var bookMeta = (BookMeta) book.getItemMeta();

        String page1Str = "";
        page1Str += order.getDescription() + "\n\n";
        page1Str += "<bold>" + messageProvider.get("messages.tradebook.contractor") + "</bold>:\n" + order.getCustomer()
                + "\n";

        if (!order.isBarter()) {
            page1Str += "<bold>" + messageProvider.get("messages.tradebook.payment") + "</bold>:\n<dark_green>"
                    + String.format("%,.2f", order.getPrice()) + messageProvider.get("messages.currency")
                    + "</dark_green>\n";
            if (order.getPenalty() != 0) {
                page1Str += "<bold>" + messageProvider.get("messages.tradebook.penalty") + "</bold>:\n<red>"
                        + String.format("%,.2f", order.getPenalty()) + messageProvider.get("messages.currency")
                        + "</red>\n";
            }
        }
        page1Str += "<bold>" + messageProvider.get("messages.tradebook.timelimit") + "</bold>:\n"
                + Formatter.formatDuration(order.getTimelimit()) + "\n";

        var barterItems = order.getBarterItems();
        if (barterItems.size() > 0) {
            page1Str += "<bold>" + messageProvider.get("messages.tradebook.barter") + "</bold>: ";
            for (var item : barterItems) {
                String material = itemFactory.getDisplayName(item);
                String amount = item.getAmount() + "";
                page1Str += amount + "x " + material;
            }
            page1Str += "\n";
        }

        Component page1 = MiniMessage.miniMessage().deserialize(page1Str);
        bookMeta.addPages(page1);

        List<String> itemPages = new ArrayList<>();

        var line = messageProvider.get("messages.tradebook.required-items-line");

        var requiredItems = order.getRequiredItems();

        int blockSize = 5;
        for (int i = 0; i < requiredItems.size(); i += blockSize) {
            int end = Math.min(i + blockSize, requiredItems.size());
            List<ItemStack> block = requiredItems.subList(i, end);

            String itemPageStr = "";
            for (ItemStack item : block) {
                String material = itemFactory.getDisplayName(item);
                String amount = item.getAmount() + "";
                itemPageStr += line.replace("{material}", material).replace("{amount}", amount);
            }
            itemPages.add(itemPageStr);
        }

        for (var pageStr : itemPages) {
            Component itemPage = MiniMessage.miniMessage().deserialize(pageStr);
            bookMeta.addPages(itemPage);
        }

        bookMeta.setAuthor(order.getCustomer());

        // Add hidden enchantment to make the item have the enchanted glow effect
        bookMeta.addEnchant(Enchantment.LURE, 1, false);
        bookMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        var orderNo = String.format("%08d", order.getOrderNo());
        bookMeta.displayName(Component.text("Order No " + orderNo));

        var itemsString = "";
        for (var item : requiredItems) {
            itemsString += itemFactory.getFilterName(item) + "#" + item.getAmount() + ";";
        }

        var barterItemsString = "";
        for (var item : barterItems) {
            barterItemsString += itemFactory.getFilterName(item) + "#" + item.getAmount() + ";";
        }

        PersistentDataContainer pdc = bookMeta.getPersistentDataContainer();
        pdc.set(getKey("tradebook.orderId"), PersistentDataType.STRING, order.getId().toString());
        pdc.set(getKey("tradebook.orderNo"), PersistentDataType.STRING, orderNo.toString());
        pdc.set(getKey("tradebook.description"), PersistentDataType.STRING, order.getDescription());
        if (order.getTradepointId() != null)
            pdc.set(getKey("tradebook.tradepointId"), PersistentDataType.STRING, order.getTradepointId().toString());
        pdc.set(getKey("tradebook.requiredItems"), PersistentDataType.STRING, itemsString);
        pdc.set(getKey("tradebook.barterItems"), PersistentDataType.STRING, barterItemsString);
        pdc.set(getKey("tradebook.timelimit"), PersistentDataType.LONG, order.getTimelimit());
        pdc.set(getKey("tradebook.price"), PersistentDataType.DOUBLE, order.getPrice());
        pdc.set(getKey("tradebook.penalty"), PersistentDataType.DOUBLE, order.getPenalty());
        pdc.set(getKey("tradebook.barter"), PersistentDataType.BOOLEAN, order.isBarter());
        pdc.set(getKey("tradebook.tradeorderbook"), PersistentDataType.INTEGER, 1);

        book.setItemMeta(bookMeta);

        return book;
    }

    public static String getFloodgateContent(ItemStack book) {

        MessageProvider messageProvider = UnitedTrade.getInstance().getMessageProvider();
        IItemFactory itemFactory = UnitedLib.getInstance().getItemFactory();

        var description = getDescription(book);
        var price = getPrice(book);
        var penalty = getPenalty(book);
        var timelimit = Formatter.formatDuration(getTimelimit(book));
        var requiredItems = getRequiredItems(book);
        var barter = getIsBarter(book);
        var barterItems = getBarterItems(book);

        String content = "§7" + description + "§r\n\n";
        if (!barter) {
            content += "§l" + messageProvider.get("messages.tradebook.payment") + ": §2" + String.format("%,.2f", price)
                    + "§r\n";
            content += "§l" + messageProvider.get("messages.tradebook.payment") + ": §c"
                    + String.format("%,.2f", penalty) + "§r\n";
        }
        if (!barterItems.isEmpty()) {
            var barterItemStr = "";
            for (var item : barterItems) {
                String material = itemFactory.getDisplayName(item);
                String amount = item.getAmount() + "";
                barterItemStr += "§6" + amount + "§7x " + material + "\n";
            }
            content += "§l" + messageProvider.get("messages.tradebook.barter") + ":" + "§r\n";
            content += barterItemStr;
        }

        content += "§l" + messageProvider.get("messages.tradebook.timelimit") + ": §6" + timelimit + "§r\n\n";

        var itemStr = "";
        for (var item : requiredItems) {
            String material = itemFactory.getDisplayName(item);
            String amount = item.getAmount() + "";
            itemStr += "§6" + amount + "§7x " + material + "\n";
        }
        content += "§l" + messageProvider.get("messages.tradebook.required-items") + ":" + "§r\n\n";
        content += itemStr;

        return content;
    }

    public static List<Component> getBasePanelComponents(ItemStack book) {

        MessageProvider messageProvider = UnitedTrade.getInstance().getMessageProvider();

        List<Component> components = new ArrayList<>();

        var description = getDescription(book);
        var price = getPrice(book);
        var penalty = getPenalty(book);
        var timelimit = Formatter.formatDuration(getTimelimit(book));
        var barter = getIsBarter(book);

        var miniMessage = MiniMessage.miniMessage();
        components.add(miniMessage.deserialize("<gray>" + description + "</gray><br>"));

        var paymentLines = "";
        if (!barter) {
            paymentLines += "<bold>" + messageProvider.get("messages.tradebook.payment") + ": </bold><dark_green>"
                    + String.format("%,.2f", price) + "</dark_green><br>" +
                    "<bold>" + messageProvider.get("messages.tradebook.penalty") + ": </bold><red>"
                    + String.format("%,.2f", penalty) + "</red><br>";
        }
        paymentLines += "<bold>" + messageProvider.get("messages.tradebook.timelimit") + ": </bold><gold>" + timelimit
                + "<gold>";

        components.add(miniMessage.deserialize(paymentLines));
        return components;
    }

    public static UUID getOrderId(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        try {
            return UUID.fromString(pdc.get(getKey("tradebook.orderId"), PersistentDataType.STRING));
        } catch (Exception ex) {
            return null;
        }
    }

    public static boolean getIsBarter(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        try {
            return pdc.get(getKey("tradebook.barter"), PersistentDataType.BOOLEAN);
        } catch (Exception ex) {
            return false;
        }
    }

    public static String getOrderNo(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        try {
            return pdc.get(getKey("tradebook.orderNo"), PersistentDataType.STRING);
        } catch (Exception ex) {
            return null;
        }
    }

    public static String getDescription(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        try {
            return pdc.get(getKey("tradebook.description"), PersistentDataType.STRING);
        } catch (Exception ex) {
            return null;
        }
    }

    public static UUID getTradepointId(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        try {
            return UUID.fromString(pdc.get(getKey("tradebook.tradepointId"), PersistentDataType.STRING));
        } catch (Exception ex) {
            return null;
        }
    }

    public static List<ItemStack> getRequiredItems(ItemStack book) {
        ItemMeta meta = book.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        var itemFactory = UnitedLib.getInstance().getItemFactory();
        try {
            var result = new ArrayList<ItemStack>();
            var list = pdc.get(getKey("tradebook.requiredItems"), PersistentDataType.STRING);
            var itemsAmounts = list.split(";");
            for (var itemAmount : itemsAmounts) {
                var split = itemAmount.split("#");
                if (split.length == 2) {
                    result.add(itemFactory.getItemStack(split[0], Integer.parseInt(split[1])));
                }
            }
            return result;
        } catch (Exception ex) {
            Logger.logError("Failed trade book required item parsing.", "UnitedTrade");
            return null;
        }
    }

    public static List<ItemStack> getBarterItems(ItemStack book) {
        ItemMeta meta = book.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
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

    public static long getTimelimit(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return 0;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        try {
            return pdc.get(getKey("tradebook.timelimit"), PersistentDataType.LONG);
        } catch (Exception ex) {
            return 0;
        }
    }

    public static double getPrice(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return 0;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        try {
            return pdc.get(getKey("tradebook.price"), PersistentDataType.DOUBLE);
        } catch (Exception ex) {
            return 0;
        }
    }

    public static double getPenalty(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return 0;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        try {
            return pdc.get(getKey("tradebook.penalty"), PersistentDataType.DOUBLE);
        } catch (Exception ex) {
            return 0;
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

    public static List<ItemStack> getMissingItems(Player player, List<ItemStack> required) {
        List<ItemStack> missing = new ArrayList<>();

        var itemFactory = UnitedLib.getInstance().getItemFactory();

        Map<String, Integer> needed = new HashMap<>();
        for (ItemStack req : required) {
            needed.merge(itemFactory.getFilterName(req), req.getAmount(), Integer::sum);
        }

        // Count what the player currently has
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType().isAir())
                continue;

            String type = itemFactory.getFilterName(item);
            if (!needed.containsKey(type))
                continue;

            int have = item.getAmount();
            int stillNeeded = needed.get(type);

            int remaining = stillNeeded - have;
            if (remaining > 0) {
                needed.put(type, remaining);
            } else {
                needed.remove(type);
            }
        }

        for (Map.Entry<String, Integer> entry : needed.entrySet()) {
            missing.add(new ItemStack(itemFactory.getItemStack(entry.getKey(), entry.getValue())));
        }

        return missing;
    }

}
