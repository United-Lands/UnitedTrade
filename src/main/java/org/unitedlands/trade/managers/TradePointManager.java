package org.unitedlands.trade.managers;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.classes.TradePoint;
import org.unitedlands.trade.utils.JsonUtils;
import org.unitedlands.utils.United;

public class TradePointManager {

    private Set<TradePoint> tradePoints = new HashSet<>();

    public void loadTradePoints() {

        tradePoints = new HashSet<>();

        String directoryPath = File.separator + "trade_points";
        File directory = new File(UnitedTrade.instance().getDataFolder(), directoryPath);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                United.logger().warning("Error creating trade_points directory.");
            }
        }

        File[] filesList = directory.listFiles();
        if (filesList != null) {
            for (File file : filesList) {
                TradePoint tradePoint = loadTradePoint(file);
                if (tradePoint != null) {
                    tradePoints.add(tradePoint);
                    tradePoint.spawnLectern();
                }
            }
        }

        United.logger().info("Loaded " + tradePoints.size() + " trade points.");

    }

    public TradePoint loadTradePoint(File file) {
        try {
            return JsonUtils.loadObjectFromFile(file, TradePoint.class);
        } catch (IOException ex) {
            United.logger().error(ex.getMessage());
            return null;
        }
    }

    public void saveTradePoint(TradePoint tradePoint, CommandSender sender) {
        if (!saveTradePoint(tradePoint)) {
            United.messenger().send(sender, "save-error");
        } else {
            United.messenger().send(sender, "save-success");
        }
    }

    public boolean saveTradePoint(TradePoint tradePoint) {
        var uuid = tradePoint.getId();
        var filePath = File.separator + "trade_points" + File.separator + uuid + ".json";

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
            JsonUtils.saveObjectToFile(tradePoint, tradePointFile);
            return true;
        } catch (IOException ex) {
            United.logger().error(ex.getMessage());
            return false;
        }
    }

    public void deleteFile(TradePoint tradePoint, CommandSender sender) {
        var uuid = tradePoint.getId();
        var filePath = File.separator + "trade_points" + File.separator + uuid + ".json";

        File tradePointFile = new File(UnitedTrade.instance().getDataFolder(), filePath);

        try {
            tradePointFile.delete();
            United.messenger().send(sender, "file-delete-success");
        } catch (Exception ex) {
            United.logger().error(ex.getMessage());
            United.messenger().send(sender, "file-delete-error");
        }
    }

    public void restockTradePoints() {
        for (var tradePoint : tradePoints) {
            tradePoint.restock();
        }
    }

    public TradePoint getClosestTradepointInRadius(Location loc, double radius) {
        TradePoint closestTradePoint = null;
        for (var item : tradePoints) {
            var dist = item.getLocation().distance(loc);
            if (dist <= radius) {
                radius = dist;
                closestTradePoint = item;
            }
        }

        return closestTradePoint;
    }

    public TradePoint getTradePoint(UUID id) {
        if (id == null)
            return null;
        return tradePoints.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
    }

    public TradePoint getTradePoint(String name) {
        return tradePoints.stream().filter(t -> t.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public TradePoint getTradePoint(Block block) {
        for (var tradePoint : tradePoints) {
            if (tradePoint.getLocation().getBlock().equals(block))
                return tradePoint;
        }
        return null;
    }

    public List<String> getTradePointNames() {
        return tradePoints.stream().map(TradePoint::getName).collect(Collectors.toList());
    }

    public boolean registerTradePoint(TradePoint tradePoint) {
        return tradePoints.add(tradePoint);
    }

    public boolean unregisterTradePoint(TradePoint tradePoint) {
        return tradePoints.remove(tradePoint);
    }

}
