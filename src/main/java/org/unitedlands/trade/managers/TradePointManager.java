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
import org.unitedlands.trade.classes.MessageProvider;
import org.unitedlands.trade.classes.TradePoint;
import org.unitedlands.trade.utils.JsonUtils;
import org.unitedlands.utils.Logger;
import org.unitedlands.utils.Messenger;

public class TradePointManager {

    private final UnitedTrade plugin;
    private final MessageProvider messageProvider;

    private Set<TradePoint> tradePoints = new HashSet<>();

    public TradePointManager(UnitedTrade plugin, MessageProvider messageProvider) {
        this.plugin = plugin;
        this.messageProvider = messageProvider;
    }

    public void loadTradePoints() {

        tradePoints = new HashSet<>();

        String directoryPath = File.separator + "trade_points";
        File directory = new File(plugin.getDataFolder(), directoryPath);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                Logger.logWarning("Error creating trade_points directory.", "UnitedTrade");
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

        Logger.log("Loaded " + tradePoints.size() + " trade points.", "UnitedTrade");

    }

    public TradePoint loadTradePoint(File file) {
        try {
            return JsonUtils.loadObjectFromFile(file, TradePoint.class);
        } catch (IOException ex) {
            Logger.logError(ex.getMessage(), "UnitedTrade");
            return null;
        }
    }

    public void saveTradePoint(TradePoint tradePoint, CommandSender sender) {
        if (!saveTradePoint(tradePoint)) {
            Messenger.sendMessage(sender, messageProvider.get("messages.save-error"), null,
                    messageProvider.get("messages.prefix"));
        } else {
            Messenger.sendMessage(sender, messageProvider.get("messages.save-success"), null,
                    messageProvider.get("messages.prefix"));
        }
    }

    public boolean saveTradePoint(TradePoint tradePoint) {
        var uuid = tradePoint.getId();
        var filePath = File.separator + "trade_points" + File.separator + uuid + ".json";

        File tradePointFile = new File(plugin.getDataFolder(), filePath);
        if (!tradePointFile.exists()) {
            tradePointFile.getParentFile().mkdirs();
            try {
                tradePointFile.createNewFile();
            } catch (IOException ex) {
                Logger.logError(ex.getMessage(), "UnitedTrade");
            }
        }

        try {
            JsonUtils.saveObjectToFile(tradePoint, tradePointFile);
            return true;
        } catch (IOException ex) {
            Logger.logError(ex.getMessage(), "UnitedTrade");
            return false;
        }
    }

    public void deleteFile(TradePoint tradePoint, CommandSender sender) {
        var uuid = tradePoint.getId();
        var filePath = File.separator + "trade_points" + File.separator + uuid + ".json";

        File tradePointFile = new File(plugin.getDataFolder(), filePath);

        try {
            tradePointFile.delete();
            Messenger.sendMessage(sender, messageProvider.get("messages.file-delete-success"), null,
                    messageProvider.get("messages.prefix"));
        } catch (Exception ex) {
            Logger.logError(ex.getMessage(), "UnitedTrade");
            Messenger.sendMessage(sender, messageProvider.get("messages.file-delete-error"), null,
                    messageProvider.get("messages.prefix"));
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
