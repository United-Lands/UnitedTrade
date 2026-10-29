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
import org.unitedlands.trade.classes.DropoffPoint;
import org.unitedlands.trade.utils.JsonUtils;
import org.unitedlands.utils.United;

public class DropoffPointManager {


    private Set<DropoffPoint> dropoffPoints = new HashSet<>();

    public void loadDropoffPoints() {

        dropoffPoints = new HashSet<>();

        String directoryPath = File.separator + "dropoff_points";
        File directory = new File(UnitedTrade.instance().getDataFolder(), directoryPath);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                United.logger().warning("Error creating dropoff_points directory.");
            }
        }

        File[] filesList = directory.listFiles();
        if (filesList != null) {
            for (File file : filesList) {
                DropoffPoint dropoffPoint = loadDropffPoint(file);
                if (dropoffPoint != null) {
                    dropoffPoints.add(dropoffPoint);
                    dropoffPoint.spawnChest();
                }
            }
        }

        United.logger().info("Loaded " + dropoffPoints.size() + " trade points.");

    }

    public DropoffPoint loadDropffPoint(File file) {
        try {
            return JsonUtils.loadObjectFromFile(file, DropoffPoint.class);
        } catch (IOException ex) {
            United.logger().error(ex.getMessage());
            return null;
        }
    }

    public void saveDropoffPoint(DropoffPoint dropffPoint, CommandSender sender) {
        if (!saveDropoffPoint(dropffPoint)) {
            United.messenger().send(sender, "save-error");
        } else {
            United.messenger().send(sender, "save-success");
        }
    }

    public boolean saveDropoffPoint(DropoffPoint dropoffPoint) {
        var uuid = dropoffPoint.getId();
        var filePath = File.separator + "dropoff_points" + File.separator + uuid + ".json";

        File tradePointFile = new File(UnitedTrade.instance().getDataFolder(), filePath);
        if (!tradePointFile.exists()) {
            tradePointFile.getParentFile().mkdirs();
            try {
                tradePointFile.createNewFile();
            } catch (IOException ex) {
                United.logger().error(ex.getMessage());
            }
        }

        try {
            JsonUtils.saveObjectToFile(dropoffPoint, tradePointFile);
            return true;
        } catch (IOException ex) {
            United.logger().error(ex.getMessage());
            return false;
        }
    }

    public void deleteFile(DropoffPoint dropoffPoint, CommandSender sender) {
        var uuid = dropoffPoint.getId();
        var filePath = File.separator + "dropoff_points" + File.separator + uuid + ".json";

        File dropoffPointFile = new File(UnitedTrade.instance().getDataFolder(), filePath);

        try {
            dropoffPointFile.delete();
            United.messenger().send(sender, "file-delete-success");
        } catch (Exception ex) {
            United.logger().error(ex.getMessage());
            United.messenger().send(sender, "file-delete-error");
        }
    }

    public DropoffPoint getClosestDropoffPointInRadius(Location loc, double radius) {
        DropoffPoint closestTradePoint = null;
        for (var item : dropoffPoints) {
            var dist = item.getLocation().distance(loc);
            if (dist <= radius) {
                radius = dist;
                closestTradePoint = item;
            }
        }

        return closestTradePoint;
    }

    public DropoffPoint getDropoffPoint(UUID id) {
        return dropoffPoints.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
    }

    public DropoffPoint getDropoffPoint(Block block) {
        for (var dropoffPoint : dropoffPoints) {
            if (dropoffPoint.getLocation().getBlock().equals(block))
                return dropoffPoint;
        }
        return null;
    }

    public boolean registerDropoffPoint(DropoffPoint dropoffPoint) {
        return dropoffPoints.add(dropoffPoint);
    }

    public boolean unregisterDropoffPoint(DropoffPoint dropoffPoint) {
        return dropoffPoints.remove(dropoffPoint);
    }
}
