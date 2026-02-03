package org.unitedlands.trade.managers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.classes.ShopItem;
import org.unitedlands.trade.classes.ShopTemplate;
import org.unitedlands.utils.Logger;

public class ShopTemplateManager {

    private final UnitedTrade plugin;

    private Map<String, ShopTemplate> shopTemplates = new HashMap<>();

    public ShopTemplateManager(UnitedTrade plugin) {
        this.plugin = plugin;
    }

    public void loadTemplates() {

        shopTemplates = new HashMap<>();

        String directoryPath = File.separator + "shop_templates";
        File directory = new File(plugin.getDataFolder(), directoryPath);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                Logger.logWarning("Error creating shop_templates directory.", "UnitedTrade");
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

                    ShopTemplate shopTemplate = new ShopTemplate();

                    shopTemplate.setBuyCooldown(configTemplate.getLong("buy-cooldown") * 1000);
                    shopTemplate.setBuyCooldownPerItem(configTemplate.getBoolean("buy-cooldown-per-item"));
                    shopTemplate.setMinReputation(configTemplate.getDouble("up-min-reputation"));

                    var templateItemSection = configTemplate.getConfigurationSection("shop-items");
                    for (var templateItemKey : templateItemSection.getKeys(false)) {
                        var shopItemSection = templateItemSection.getConfigurationSection(templateItemKey);

                        var shopItem = new ShopItem();
                        shopItem.setMaterial(templateItemKey);
                        shopItem.setUnitSize(shopItemSection.getInt("unit-size"));
                        shopItem.setPrice(shopItemSection.getDouble("price"));

                        shopTemplate.getShopItems().add(shopItem);
                    }

                    shopTemplates.put(configTemplateKey, shopTemplate);
                }
            }

        }

        Logger.log("Loaded " + shopTemplates.size() + " shop templates from files.", "UnitedTrade");
    }

    public ShopTemplate getShopTemplate(String key) {
        return shopTemplates.get(key);
    }

    public Set<String> getShopTemplateKeys() {
        return shopTemplates.keySet();
    }

}
