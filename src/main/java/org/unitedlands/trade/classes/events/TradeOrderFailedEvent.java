package org.unitedlands.trade.classes.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.unitedlands.trade.classes.TradePoint;

public class TradeOrderFailedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private Player player;
    private TradePoint tradePoint;
    private double penalty;

    public TradeOrderFailedEvent(Player player, TradePoint tradePoint, double penalty) {
        this.player = player;
        this.tradePoint = tradePoint;
        this.penalty = penalty;
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

    public double getPenalty() {
        return penalty;
    }

    public void setPenalty(double penalty) {
        this.penalty = penalty;
    }


}
