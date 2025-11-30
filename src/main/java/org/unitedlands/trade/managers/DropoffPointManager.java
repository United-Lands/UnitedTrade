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
import org.unitedlands.trade.classes.MessageProvider;
import org.unitedlands.trade.utils.JsonUtils;
import org.unitedlands.utils.Logger;
import org.unitedlands.utils.Messenger;

public class DropoffPointManager {

    private final UnitedTrade plugin;
    private final MessageProvider messageProvider;

    private Set<DropoffPoint> dropoffPoints = new HashSet<>();

    public DropoffPointManager(UnitedTrade plugin, MessageProvider messageProvider) {
        this.plugin = plugin;
        this.messageProvider = messageProvider;
    }

    public void loadDropoffPoints() {

        dropoffPoints = new HashSet<>();

        String directoryPath = File.separator + "dropoff_points";
        File directory = new File(plugin.getDataFolder(), directoryPath);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                Logger.logWarning("Error creating dropoff_points directory.", "UnitedTrade");
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

        Logger.log("Loaded " + dropoffPoints.size() + " trade points.", "UnitedTrade");

    }

    public DropoffPoint loadDropffPoint(File file) {
        try {
            return JsonUtils.loadObjectFromFile(file, DropoffPoint.class);
        } catch (IOException ex) {
            Logger.logError(ex.getMessage(), "UnitedTrade");
            return null;
        }
    }

    public void saveDropoffPoint(DropoffPoint dropffPoint, CommandSender sender) {
        if (!saveDropoffPoint(dropffPoint)) {
            Messenger.sendMessage(sender, messageProvider.get("messages.save-error"), null,
                    messageProvider.get("messages.prefix"));
        } else {
            Messenger.sendMessage(sender, messageProvider.get("messages.save-success"), null,
                    messageProvider.get("messages.prefix"));
        }
    }

    public boolean saveDropoffPoint(DropoffPoint dropoffPoint) {
        var uuid = dropoffPoint.getId();
        var filePath = File.separator + "dropoff_points" + File.separator + uuid + ".json";

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
            JsonUtils.saveObjectToFile(dropoffPoint, tradePointFile);
            return true;
        } catch (IOException ex) {
            Logger.logError(ex.getMessage(), "UnitedTrade");
            return false;
        }
    }

    public void deleteFile(DropoffPoint dropoffPoint, CommandSender sender) {
        var uuid = dropoffPoint.getId();
        var filePath = File.separator + "dropoff_points" + File.separator + uuid + ".json";

        File dropoffPointFile = new File(plugin.getDataFolder(), filePath);

        try {
            dropoffPointFile.delete();
            Messenger.sendMessage(sender, messageProvider.get("messages.file-delete-success"), null,
                    messageProvider.get("messages.prefix"));
        } catch (Exception ex) {
            Logger.logError(ex.getMessage(), "UnitedTrade");
            Messenger.sendMessage(sender, messageProvider.get("messages.file-delete-error"), null,
                    messageProvider.get("messages.prefix"));
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
