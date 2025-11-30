package org.unitedlands.trade.classes.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.unitedlands.trade.classes.TradePoint;

public class TradeOrderCompletedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private Player player;
    private TradePoint tradePoint;
    private double payment;

    public TradeOrderCompletedEvent(Player player, TradePoint tradePoint, double payment) {
        this.player = player;
        this.tradePoint = tradePoint;
        this.payment = payment;
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

    public double getPayment() {
        return payment;
    }

    public void setPayment(double payment) {
        this.payment = payment;
    }

    public TradePoint getTradePoint() {
        return tradePoint;
    }

    public void setTradePoint(TradePoint tradePoint) {
        this.tradePoint = tradePoint;
    }

}
