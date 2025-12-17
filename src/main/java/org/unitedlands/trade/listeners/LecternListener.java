package org.unitedlands.trade.listeners;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.classes.MessageProvider;
import org.unitedlands.trade.classes.OrderTrackerItem;
import org.unitedlands.trade.classes.events.TradeOrderBookPreTakeEvent;
import org.unitedlands.trade.utils.TradeOrderBookUtil;
import org.unitedlands.utils.Formatter;
import org.unitedlands.utils.Messenger;

import io.papermc.paper.event.player.PlayerInsertLecternBookEvent;

public class LecternListener implements Listener {

    private final UnitedTrade plugin;
    private final MessageProvider messageProvider;

    public LecternListener(UnitedTrade plugin, MessageProvider messageProvider) {
        this.plugin = plugin;
        this.messageProvider = messageProvider;
    }

    @EventHandler
    public void onBookPlace(PlayerInsertLecternBookEvent event) {
        var block = event.getLectern().getBlock();
        var tradePoint = plugin.getTradePointManager().getTradePoint(block);
        if (tradePoint == null)
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onBookPickup(PlayerTakeLecternBookEvent event) {
        var block = event.getLectern().getBlock();
        var tradePoint = plugin.getTradePointManager().getTradePoint(block);
        if (tradePoint == null)
            return;

        Player player = event.getPlayer();

        if (tradePoint.isPlayerOnPickupCooldown(player.getUniqueId())) {
            Messenger.sendMessage(player, messageProvider.get("messages.tradepoint.on-cooldown"),
                    Map.of("cooldown", Formatter.formatDuration(tradePoint.getPickupCooldown() * 1000)),
                    messageProvider.get("messages.prefix"));
            event.setCancelled(true);
            return;
        }

        if (tradePoint.getRequiredPermissions() != null) {
            var permissionsArray = tradePoint.getRequiredPermissions().split(",");
            boolean playerHasAllRequiredPermissions = true;
            for (var permString : permissionsArray) {
                var perm = permString.trim();
                if (!player.hasPermission(perm))
                    playerHasAllRequiredPermissions = false;
            }

            if (!playerHasAllRequiredPermissions) {
                Messenger.sendMessage(player, messageProvider.get("messages.tradepoint.permission-error"), null,
                        messageProvider.get("messages.prefix"));
                event.setCancelled(true);
                return;
            }
        }

        if (tradePoint.getBlacklistedPermissions() != null) {
            var permissionsArray = tradePoint.getBlacklistedPermissions().split(",");
            boolean playerHasBlacklistedPermission = false;
            for (var permString : permissionsArray) {
                var perm = permString.trim();
                if (player.hasPermission(perm))
                    playerHasBlacklistedPermission = true;
            }

            if (playerHasBlacklistedPermission) {
                Messenger.sendMessage(player, messageProvider.get("messages.tradepoint.permission-error"), null,
                        messageProvider.get("messages.prefix"));
                event.setCancelled(true);
                return;
            }
        }

        var preTakeEvent = new TradeOrderBookPreTakeEvent(player, tradePoint);
        preTakeEvent.callEvent();

        // Some plugin may cancel the event, e.g. due to lack of reputation or wars,
        // preventing players from taking order from this trade point
        if (preTakeEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        var book = event.getBook();

        var orderId = TradeOrderBookUtil.getOrderId(book);
        var orderNo = TradeOrderBookUtil.getOrderNo(book);
        var timelimit = TradeOrderBookUtil.getTimelimit(book);
        var penalty = TradeOrderBookUtil.getPenalty(book);

        var orderTrackerItem = new OrderTrackerItem(orderId, orderNo, tradePoint.getId(), player.getUniqueId(),
                penalty, System.currentTimeMillis() + timelimit);
        plugin.getOrderTracker().addTrackedOrder(orderTrackerItem);

        Messenger.sendMessage(player, messageProvider.get("messages.tradepoint.order-started"),
                Map.of("remaining", Formatter.formatDuration(timelimit)), messageProvider.get("messages.prefix"));
        tradePoint.addPlayerPickupCooldown(player.getUniqueId());

    }

}
