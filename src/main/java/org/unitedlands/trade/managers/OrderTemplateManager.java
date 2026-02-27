package org.unitedlands.trade.managers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;
import org.unitedlands.UnitedLib;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.classes.Order;
import org.unitedlands.trade.classes.OrderBarterItem;
import org.unitedlands.trade.classes.OrderTemplate;
import org.unitedlands.trade.classes.OrderTemplateItemGroup;
import org.unitedlands.trade.classes.TradePoint;
import org.unitedlands.utils.Logger;

public class OrderTemplateManager {

    private final UnitedTrade plugin;

    private Map<String, OrderTemplate> orderTemplates = new HashMap<>();

    public OrderTemplateManager(UnitedTrade plugin) {
        this.plugin = plugin;
    }

    public void loadTemplates() {

        orderTemplates = new HashMap<>();

        String directoryPath = File.separator + "order_templates";
        File directory = new File(plugin.getDataFolder(), directoryPath);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                Logger.logWarning("Error creating order_templates directory.", "UnitedTrade");
            }
        }

        File[] filesList = directory.listFiles();

        if (filesList != null) {
            for (File file : filesList) {
                YamlConfiguration template = null;
                try {
                    template = YamlConfiguration.loadConfiguration(file);
                } catch (Exception ex) {
                    Logger.logError("Couldn't load file: " + file.getName(), "UnitedTrade");
                    continue;
                }

                var root = template.getRoot();

                for (var configTemplateKey : root.getKeys(false)) {

                    var configTemplate = root.getConfigurationSection(configTemplateKey);

                    OrderTemplate orderTemplate = new OrderTemplate();

                    orderTemplate.setTimelimit(configTemplate.getLong("timelimit") * 1000);
                    orderTemplate.setBarter(configTemplate.getBoolean("barter", false));
                    orderTemplate.setRandomDescriptions(configTemplate.getStringList("random-descriptions"));

                    var templateItemSection = configTemplate.getConfigurationSection("order-items");
                    for (var templateItemKey : templateItemSection.getKeys(false)) {
                        var templateItem = templateItemSection.getConfigurationSection(templateItemKey);

                        OrderTemplateItemGroup item = new OrderTemplateItemGroup();
                        item.setMaterials(templateItem.getStringList("materials"));
                        item.setUnitSize(templateItem.getInt("unitsize", 0));

                        var unitsize = templateItem.getString("units");
                        var split = unitsize.split("-");
                        if (split.length == 1) {
                            item.setMinUnits(Integer.parseInt(split[0]));
                            item.setMaxUnits(Integer.parseInt(split[0]));
                        } else if (split.length == 2) {
                            item.setMinUnits(Integer.parseInt(split[0]));
                            item.setMaxUnits(Integer.parseInt(split[1]));
                        } else {
                            Logger.logError("Invalid unit size in " + configTemplateKey + "." + templateItemKey,
                                    "UnitedTrade");
                        }

                        orderTemplate.getItemGroups().add(item);
                    }

                    var barterItemsSection = configTemplate.getConfigurationSection("barter-items");
                    if (barterItemsSection != null) {
                        for (var barterItemKey : barterItemsSection.getKeys(false)) {
                            var barterItem = barterItemsSection.getConfigurationSection(barterItemKey);
                            OrderBarterItem item = new OrderBarterItem();
                            item.setMaterial(barterItem.getString("material"));
                            item.setAmount(barterItem.getInt("amount", 0));

                            orderTemplate.getBarterItems().add(item);
                        }
                    }

                    orderTemplates.put(configTemplateKey, orderTemplate);
                }
            }
        }

        Logger.log("Loaded " + orderTemplates.size() + " order templates from files.", "UnitedTrade");
    }

    public Set<String> getOrderTemplateKeys() {
        return orderTemplates.keySet();
    }

    public Order generateRandomOrder(String templateName, TradePoint tradePoint) {

        var template = orderTemplates.get(templateName);
        if (templateName == null) {
            Logger.logWarning("Unknown template name: " + templateName, "UnitedTrade");
            return null;
        }

        var order = new Order();

        order.setBarter(template.isBarter());

        var priceConfig = UnitedTrade.getInstance().getPriceConfig().get();
        var globalAdjustment = priceConfig.getDouble("global-adjustment", 1d);

        var price = 0d;
        for (var item : template.getItemGroups()) {

            var amount = item.getRandomAmount();

            var rnd = new Random();
            var materialName = item.getMaterials().get(rnd.nextInt(0, item.getMaterials().size()));

            var itemStack = UnitedLib.getInstance().getItemFactory().getItemStack(materialName, amount);
            if (itemStack != null) {
                order.getRequiredItems().add(itemStack);
                price += priceConfig.getDouble("materials." + materialName, 0d) * globalAdjustment * amount;
            }
        }

        if (!order.isBarter()) {
            order.setPrice(price);
        }

        for (var item : template.getBarterItems()) {

            var itemStack = UnitedLib.getInstance().getItemFactory().getItemStack(item.getMaterial(), item.getAmount());
            if (itemStack != null) {
                order.getBarterItems().add(itemStack);
            }
        }

        order.setCustomer(tradePoint.getCleanOwnerName());
        order.setDescription(template.getRandomDescription());
        order.setTimelimit(template.getTimelimit());
        if (tradePoint != null) {
            order.setTradepointId(tradePoint.getId());
            order.setOrderNo(tradePoint.getAndRaiseOrderNo());
            if (tradePoint.applyContractPenalties()) {
                order.setPenalty(price * tradePoint.getContractPenalty());
            }
        }

        return order;
    }

}
