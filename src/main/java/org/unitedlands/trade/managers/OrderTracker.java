package org.unitedlands.trade.managers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.classes.OrderTrackerItem;
import org.unitedlands.trade.classes.TradePoint;
import org.unitedlands.trade.classes.events.TradeOrderCompletedEvent;
import org.unitedlands.trade.classes.events.TradeOrderFailedEvent;
import org.unitedlands.trade.classes.events.TradePointValidationEvent;
import org.unitedlands.trade.utils.JsonUtils;
import org.unitedlands.trade.utils.TradeOrderBookUtil;
import org.unitedlands.utils.United;

import com.google.gson.reflect.TypeToken;

public class OrderTracker {

    private Set<OrderTrackerItem> trackedOrders = new HashSet<>();

    private BukkitTask trackerTask;

    public boolean acceptTradeOrder(Player player, TradePoint tradePoint, ItemStack book) {

        var validationEvent = new TradePointValidationEvent(player, tradePoint);
        validationEvent.callEvent();

        if (!validationEvent.isValid()) {
            for (var message : validationEvent.getMessages()) {
                United.messenger().send(player, message);
            }
            return false;
        }

        var order = TradeOrderBookUtil.getOrder(book);
        var timelimit = order.getTimelimit();

        var orderTrackerItem = new OrderTrackerItem(
                order.getId(),
                String.format("%08d", order.getOrderNo()),
                tradePoint.getId(),
                player.getUniqueId(),
                order.getPenalty(),
                System.currentTimeMillis() + timelimit);
        addTrackedOrder(orderTrackerItem);

        United.messenger().send(player, "tradepoint.order-started", United.formatter().formatDuration(timelimit));
        tradePoint.addPlayerPickupCooldown(player.getUniqueId());

        return true;
    }

    public void addTrackedOrder(OrderTrackerItem item) {
        trackedOrders.add(item);
        saveTrackedOrders();
    }

    public OrderTrackerItem getTrackedOrder(UUID orderId) {
        return trackedOrders.stream().filter(o -> o.getOrderId().equals(orderId)).findFirst().orElse(null);
    }

    public void startTracking() {
        United.logger().info("Starting order tracking...");
        var frequency = UnitedTrade.instance().getConfig().getLong("check-frequency", 1200L);
        trackerTask = new BukkitRunnable() {
            @Override
            public void run() {
                checkTrackedOrders();
            }
        }.runTaskTimer(UnitedTrade.instance(), frequency, frequency);
    }

    public void stopTracking() {
        United.logger().info("Stopping order tracking...");
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
                United.messenger().send(player, "tradepoint.order-failed", item.getOrderNo());
            }
        }

        if (!expiredOrders.isEmpty()) {
            trackedOrders.removeAll(expiredOrders);
            saveTrackedOrders();
        }

        UnitedTrade.instance().getTradePointManager().restockTradePoints();
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
            UnitedTrade.instance().getEconomyProvider().takeMoneyFromPlayer(player, penalty, "Contract penalty");
        }
        TradePoint tradepoint = UnitedTrade.instance().getTradePointManager().getTradePoint(tradepointId);
        (new TradeOrderFailedEvent(player, tradepoint, penalty)).callEvent();
    }

    public void handleCompletedOrder(Player player, UUID tradepointId, String orderNo, double payment,
            List<ItemStack> barterItems) {

        TradePoint tradepoint = UnitedTrade.instance().getTradePointManager().getTradePoint(tradepointId);
        var event = new TradeOrderCompletedEvent(player, tradepoint, payment);
        event.callEvent();

        if (event.isCancelled())
            return;

        var bonus = event.getBonus();
        if (bonus != 0) {

            if (bonus > 0) {
                United.messenger().send(player, "checkorder.bonus", 
                    String.format("%,.2f", bonus) + United.messenger().get("currency"), event.getBonusReason());
            } else {
                United.messenger().send(player, "checkorder.malus", 
                    String.format("%,.2f", bonus) + United.messenger().get("currency"), event.getBonusReason());
            }
        }

        var totalPayment = event.getPayment() + event.getBonus();

        if (payment != 0) {
            UnitedTrade.instance().getEconomyProvider().giveMoneyToPlayer(player, totalPayment, "Order payment");
        }

        if (!barterItems.isEmpty()) {
            for (var item : barterItems) {
                var overflow = player.getInventory().addItem(item);
                for (var set : overflow.entrySet())
                    player.getLocation().getWorld().dropItemNaturally(player.getLocation(), set.getValue());
            }
        }

    }

    public boolean loadTrackedOrders() {

        var filename = "tracked_orders";
        var filePath = File.separator + "orders" + File.separator + filename + ".json";

        File trackedOrdersFile = new File(UnitedTrade.instance().getDataFolder(), filePath);
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
            United.logger().error(ex.getMessage());
            return null;
        }
    }

    public boolean saveTrackedOrders() {

        var filename = "tracked_orders";
        var filePath = File.separator + "orders" + File.separator + filename + ".json";

        File trackedOrdersFile = new File(UnitedTrade.instance().getDataFolder(), filePath);
        if (!trackedOrdersFile.exists()) {
            trackedOrdersFile.getParentFile().mkdirs();
            try {
                trackedOrdersFile.createNewFile();
            } catch (IOException ex) {
                United.logger().error(ex.getMessage());
            }
        }

        try {
            JsonUtils.saveObjectToFile(trackedOrders, trackedOrdersFile);
            return true;
        } catch (IOException ex) {
            United.logger().error(ex.getMessage());
            return false;
        }

    }

}
