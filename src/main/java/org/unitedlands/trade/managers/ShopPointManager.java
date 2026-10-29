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
import org.unitedlands.trade.classes.ShopPoint;
import org.unitedlands.trade.utils.JsonUtils;
import org.unitedlands.utils.United;

public class ShopPointManager {

    private Set<ShopPoint> shopPoints = new HashSet<>();

    public void loadShopPoints() {

        shopPoints = new HashSet<>();

        String directoryPath = File.separator + "shop_points";
        File directory = new File(UnitedTrade.instance().getDataFolder(), directoryPath);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                United.logger().warning("Error creating shop_points directory.");
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

        United.logger().info("Loaded " + shopPoints.size() + " trade points.");

    }

    public ShopPoint loadShopPoint(File file) {
        try {
            return JsonUtils.loadObjectFromFile(file, ShopPoint.class);
        } catch (IOException ex) {
            United.logger().error(ex.getMessage());
            return null;
        }
    }

    public void saveShopPoint(ShopPoint shopPoint, CommandSender sender) {
        if (!saveShopPoint(shopPoint)) {
            United.messenger().send(sender, "save-error");
        } else {
            United.messenger().send(sender, "save-success");
        }
    }

    public boolean saveShopPoint(ShopPoint shopPoint) {
        var uuid = shopPoint.getId();
        var filePath = File.separator + "shop_points" + File.separator + uuid + ".json";

        File shopPointFile = new File(UnitedTrade.instance().getDataFolder(), filePath);
        if (!shopPointFile.exists()) {
            shopPointFile.getParentFile().mkdirs();
            try {
                shopPointFile.createNewFile();
            } catch (IOException ex) {
                United.logger().error(ex.getMessage());
            }
        }

        try {
            JsonUtils.saveObjectToFile(shopPoint, shopPointFile);
            return true;
        } catch (IOException ex) {
            United.logger().error(ex.getMessage());
            return false;
        }
    }

    public void deleteFile(ShopPoint shopPoint, CommandSender sender) {
        var uuid = shopPoint.getId();
        var filePath = File.separator + "shop_points" + File.separator + uuid + ".json";

        File shopPointFile = new File(UnitedTrade.instance().getDataFolder(), filePath);

        try {
            shopPointFile.delete();
            United.messenger().send(sender, "file-delete-success");
        } catch (Exception ex) {
            United.logger().error(ex.getMessage());
            United.messenger().send(sender, "file-delete-error");
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
