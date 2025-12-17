package org.unitedlands.trade.managers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.classes.MessageProvider;
import org.unitedlands.trade.classes.OrderTrackerItem;
import org.unitedlands.trade.classes.TradePoint;
import org.unitedlands.trade.classes.events.TradeOrderCompletedEvent;
import org.unitedlands.trade.classes.events.TradeOrderFailedEvent;
import org.unitedlands.trade.utils.JsonUtils;
import org.unitedlands.utils.Logger;
import org.unitedlands.utils.Messenger;

import com.google.gson.reflect.TypeToken;

public class OrderTracker {

    private final UnitedTrade plugin;
    private final MessageProvider messageProvider;

    private Set<OrderTrackerItem> trackedOrders = new HashSet<>();

    private BukkitTask trackerTask;

    public OrderTracker(UnitedTrade plugin, MessageProvider messageProvider) {
        this.plugin = plugin;
        this.messageProvider = messageProvider;
    }

    public void addTrackedOrder(OrderTrackerItem item) {
        trackedOrders.add(item);
        saveTrackedOrders();
    }

    public OrderTrackerItem getTrackedOrder(UUID orderId) {
        return trackedOrders.stream().filter(o -> o.getOrderId().equals(orderId)).findFirst().orElse(null);
    }

    public void startTracking() {
        Logger.log("Starting order tracking...", "UnitedTrade");
        var frequency = plugin.getConfig().getLong("check-frequency", 1200L);
        trackerTask = new BukkitRunnable() {
            @Override
            public void run() {
                checkTrackedOrders();
            }
        }.runTaskTimer(plugin, frequency, frequency);
    }

    public void stopTracking() {
        Logger.log("Stopping order tracking...");
        if (trackerTask != null)
            trackerTask.cancel();
    }

    protected void checkTrackedOrders() {
        List<OrderTrackerItem> expiredOrders = new ArrayList<>();
        for (var item : trackedOrders) {
            if (System.currentTimeMillis() > item.getEndTime()) {
                expiredOrders.add(item);
            }
        }

        for (var item : expiredOrders) {
            var player = Bukkit.getPlayer(item.getPlayerId());
            if (player == null)
                continue;
            handleExpiredOrder(player, item.getTradepointId(), item.getOrderNo(), item.getPenalty());
            if (player.isOnline()) {
                Messenger.sendMessage(player, messageProvider.get("messages.tradepoint.order-failed"),
                        Map.of("order-no", item.getOrderNo()), messageProvider.get("messages.prefix"));
            }
        }

        if (!expiredOrders.isEmpty()) {
            trackedOrders.removeAll(expiredOrders);
            saveTrackedOrders();
        }

        plugin.getTradePointManager().restockTradePoints();
    }

    public boolean removeTrackedOrder(UUID orderId) {
        var order = trackedOrders.stream().filter(o -> o.getOrderId().equals(orderId)).findFirst().orElse(null);
        if (order == null)
            return false;
        if (trackedOrders.remove(order)) {
            saveTrackedOrders();
            return true;
        }
        return false;
    }

    public void handleExpiredOrder(Player player, UUID tradepointId, String orderNo, double penalty) {
        if (penalty != 0) {
            var cmd = UnitedTrade.getInstance().getConfig().getString("take-command");
            cmd = cmd.replace("{user}", player.getName());
            cmd = cmd.replace("{amount}", penalty + "");
            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
        }
        Logger.log("handleExpiredOrder");
        TradePoint tradepoint = plugin.getTradePointManager().getTradePoint(tradepointId);
        (new TradeOrderFailedEvent(player, tradepoint, penalty)).callEvent();
    }

    public void handleCompletedOrder(Player player, UUID tradepointId, String orderNo, double payment) {

        TradePoint tradepoint = plugin.getTradePointManager().getTradePoint(tradepointId);
        var event = new TradeOrderCompletedEvent(player, tradepoint, payment);
        event.callEvent();

        if (event.isCancelled())
            return;

        var bonus = event.getBonus();
        if (bonus != 0) {

            if (bonus > 0) {
                Messenger.sendMessage(player, messageProvider.get("messages.checkorder.bonus"),
                        Map.of("bonus", String.format("%,.2f", bonus) + messageProvider.get("messages.currency"),
                                "reason", event.getBonusReason()),
                        messageProvider.get("messages.prefix"));
            } else {
                                Messenger.sendMessage(player, messageProvider.get("messages.checkorder.malus"),
                        Map.of("bonus", String.format("%,.2f", bonus) + messageProvider.get("messages.currency"),
                                "reason", event.getBonusReason()),
                        messageProvider.get("messages.prefix"));
            }
        }

        var totalPayment = event.getPayment() + event.getBonus();

        if (payment != 0) {
            var cmd = UnitedTrade.getInstance().getConfig().getString("pay-command");
            cmd = cmd.replace("{user}", player.getName());
            cmd = cmd.replace("{amount}", totalPayment + "");
            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
        }
    }

    public boolean loadTrackedOrders() {

        var filename = "tracked_orders";
        var filePath = File.separator + "orders" + File.separator + filename + ".json";

        File trackedOrdersFile = new File(plugin.getDataFolder(), filePath);
        Set<OrderTrackerItem> orders = loadTrackedOrderFile(trackedOrdersFile);
        if (orders != null) {
            trackedOrders = orders;
            return true;
        }
        return false;
    }

    public Set<OrderTrackerItem> loadTrackedOrderFile(File file) {
        try {
            Type type = new TypeToken<Set<OrderTrackerItem>>() {
            }.getType();
            return JsonUtils.loadObjectFromFile(file, type);
        } catch (IOException ex) {
            Logger.logError(ex.getMessage(), "UnitedTrade");
            return null;
        }
    }

    public boolean saveTrackedOrders() {

        var filename = "tracked_orders";
        var filePath = File.separator + "orders" + File.separator + filename + ".json";

        File trackedOrdersFile = new File(plugin.getDataFolder(), filePath);
        if (!trackedOrdersFile.exists()) {
            trackedOrdersFile.getParentFile().mkdirs();
            try {
                trackedOrdersFile.createNewFile();
            } catch (IOException ex) {
                Logger.logError(ex.getMessage(), "UnitedTrade");
            }
        }

        try {
            JsonUtils.saveObjectToFile(trackedOrders, trackedOrdersFile);
            return true;
        } catch (IOException ex) {
            Logger.logError(ex.getMessage(), "UnitedTrade");
            return false;
        }

    }

}
