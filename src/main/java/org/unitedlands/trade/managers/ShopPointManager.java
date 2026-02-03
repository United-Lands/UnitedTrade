package org.unitedlands.trade.managers;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.classes.MessageProvider;
import org.unitedlands.trade.classes.ShopPoint;
import org.unitedlands.trade.utils.JsonUtils;
import org.unitedlands.utils.Logger;
import org.unitedlands.utils.Messenger;

public class ShopPointManager {

    private final UnitedTrade plugin;
    private final MessageProvider messageProvider;

    private Set<ShopPoint> shopPoints = new HashSet<>();

    public ShopPointManager(UnitedTrade plugin, MessageProvider messageProvider) {
        this.plugin = plugin;
        this.messageProvider = messageProvider;
    }

    public void loadShopPoints() {

        shopPoints = new HashSet<>();

        String directoryPath = File.separator + "shop_points";
        File directory = new File(plugin.getDataFolder(), directoryPath);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                Logger.logWarning("Error creating shop_points directory.", "UnitedTrade");
            }
        }

        File[] filesList = directory.listFiles();
        if (filesList != null) {
            for (File file : filesList) {
                ShopPoint shopPoint = loadShopPoint(file);
                if (shopPoint != null) {
                    shopPoints.add(shopPoint);
                    shopPoint.spawnChest();
                }
            }
        }

        Logger.log("Loaded " + shopPoints.size() + " trade points.", "UnitedTrade");

    }

    public ShopPoint loadShopPoint(File file) {
        try {
            return JsonUtils.loadObjectFromFile(file, ShopPoint.class);
        } catch (IOException ex) {
            Logger.logError(ex.getMessage(), "UnitedTrade");
            return null;
        }
    }

    public void saveShopPoint(ShopPoint shopPoint, CommandSender sender) {
        if (!saveShopPoint(shopPoint)) {
            Messenger.sendMessage(sender, messageProvider.get("messages.save-error"), null,
                    messageProvider.get("messages.prefix"));
        } else {
            Messenger.sendMessage(sender, messageProvider.get("messages.save-success"), null,
                    messageProvider.get("messages.prefix"));
        }
    }

    public boolean saveShopPoint(ShopPoint shopPoint) {
        var uuid = shopPoint.getId();
        var filePath = File.separator + "shop_points" + File.separator + uuid + ".json";

        File shopPointFile = new File(plugin.getDataFolder(), filePath);
        if (!shopPointFile.exists()) {
            shopPointFile.getParentFile().mkdirs();
            try {
                shopPointFile.createNewFile();
            } catch (IOException ex) {
                Logger.logError(ex.getMessage(), "UnitedTrade");
            }
        }

        try {
            JsonUtils.saveObjectToFile(shopPoint, shopPointFile);
            return true;
        } catch (IOException ex) {
            Logger.logError(ex.getMessage(), "UnitedTrade");
            return false;
        }
    }

    public void deleteFile(ShopPoint shopPoint, CommandSender sender) {
        var uuid = shopPoint.getId();
        var filePath = File.separator + "shop_points" + File.separator + uuid + ".json";

        File shopPointFile = new File(plugin.getDataFolder(), filePath);

        try {
            shopPointFile.delete();
            Messenger.sendMessage(sender, messageProvider.get("messages.file-delete-success"), null,
                    messageProvider.get("messages.prefix"));
        } catch (Exception ex) {
            Logger.logError(ex.getMessage(), "UnitedTrade");
            Messenger.sendMessage(sender, messageProvider.get("messages.file-delete-error"), null,
                    messageProvider.get("messages.prefix"));
        }
    }

    public ShopPoint getClosestShopPointInRadius(Location loc, double radius) {
        ShopPoint closestShopPoint = null;
        for (var item : shopPoints) {
            var dist = item.getLocation().distance(loc);
            if (dist <= radius) {
                radius = dist;
                closestShopPoint = item;
            }
        }

        return closestShopPoint;
    }

    public ShopPoint getShopPoint(UUID id) {
        return shopPoints.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
    }

    public ShopPoint getShopPoint(Block block) {
        for (var shopPoint : shopPoints) {
            if (shopPoint.getLocation().getBlock().equals(block))
                return shopPoint;
        }
        return null;
    }

    public boolean registerShopPoint(ShopPoint dropoffPoint) {
        return shopPoints.add(dropoffPoint);
    }

    public boolean unregisterShopPoint(ShopPoint dropoffPoint) {
        return shopPoints.remove(dropoffPoint);
    }
}
