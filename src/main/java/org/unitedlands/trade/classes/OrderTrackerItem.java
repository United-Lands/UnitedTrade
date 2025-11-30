package org.unitedlands.trade.classes;

import java.util.UUID;

import com.google.gson.annotations.Expose;

public class OrderTrackerItem {

    @Expose
    private UUID orderId;
    @Expose
    private String orderNo;
    @Expose
    private UUID tradepointId;
    @Expose
    private UUID playerId;
    @Expose
    private double penalty;
    @Expose
    private long endTime;

    public OrderTrackerItem(UUID orderId, String orderNo, UUID tradepointId, UUID playerId, double penalty, long endTime) {
        this.orderId = orderId;
        this.orderNo = orderNo;
        this.tradepointId = tradepointId;
        this.playerId = playerId;
        this.penalty = penalty;
        this.endTime = endTime;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public double getPenalty() {
        return penalty;
    }

    public void setPenalty(double penalty) {
        this.penalty = penalty;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public UUID getTradepointId() {
        return tradepointId;
    }

    public void setTradepointId(UUID tradepointId) {
        this.tradepointId = tradepointId;
    }

}
