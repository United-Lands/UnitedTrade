package org.unitedlands.trade.classes.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.unitedlands.trade.classes.TradePoint;

public class TradeOrderBookPreTakeEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;

    private Player player;
    private TradePoint tradePoint;

    public TradeOrderBookPreTakeEvent(Player player, TradePoint tradePoint) {
        this.player = player;
        this.tradePoint = tradePoint;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public TradePoint getTradePoint() {
        return tradePoint;
    }

    public void setTradePoint(TradePoint tradePoint) {
        this.tradePoint = tradePoint;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;

    }

}
