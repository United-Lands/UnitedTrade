package org.unitedlands.trade.managers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.classes.ShopItem;
import org.unitedlands.trade.classes.ShopTemplate;
import org.unitedlands.utils.United;

public class ShopTemplateManager {

    private Map<String, ShopTemplate> shopTemplates = new HashMap<>();

    public void loadTemplates() {

        shopTemplates = new HashMap<>();

        String directoryPath = File.separator + "shop_templates";
        File directory = new File(UnitedTrade.instance().getDataFolder(), directoryPath);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                United.logger().warning("Error creating shop_templates directory.");
            }
        }

        File[] filesList = directory.listFiles();

        if (filesList != null) {
            for (File file : filesList) {
                YamlConfiguration template = null;
                try {
                    template = YamlConfiguration.loadConfiguration(file);
                } catch (Exception ex) {
                    United.logger().error("Couldn't load file: " + file.getName());
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

        United.logger().info("Loaded " + shopTemplates.size() + " shop templates from files.");
    }

    public ShopTemplate getShopTemplate(String key) {
        return shopTemplates.get(key);
    }

    public Set<String> getShopTemplateKeys() {
        return shopTemplates.keySet();
    }

}
