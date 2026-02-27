package org.unitedlands.trade.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

public class Order {

    private UUID id;
    private UUID tradepointId;
    private Integer orderNo;
    private List<ItemStack> requiredItems = new ArrayList<>();
    private List<ItemStack> barterItems = new ArrayList<>();
    private String customer;
    private String description;
    private long timelimit;
    private boolean barter;

    private double price = 0.0d;
    private double penalty = 0.0d;

    public Order() {
        this.id = UUID.randomUUID();
    }

    public Order(UUID id, List<ItemStack> requiredItems, String customer, String description) {
        this.id = UUID.randomUUID();
        this.requiredItems = requiredItems;
        this.customer = customer;
        this.description = description;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public List<ItemStack> getRequiredItems() {
        return requiredItems;
    }

    public void setRequiredItems(List<ItemStack> requiredItems) {
        this.requiredItems = requiredItems;
    }

    public List<ItemStack> getBarterItems() {
        return barterItems;
    }

    public void setBarterItems(List<ItemStack> barterItems) {
        this.barterItems = barterItems;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTimelimit() {
        return timelimit;
    }

    public void setTimelimit(long timelimit) {
        this.timelimit = timelimit;
    }

    public boolean isBarter() {
        return barter;
    }

    public void setBarter(boolean barter) {
        this.barter = barter;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public UUID getTradepointId() {
        return tradepointId;
    }

    public void setTradepointId(UUID tradepointId) {
        this.tradepointId = tradepointId;
    }

    public int getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(int orderNo) {
        this.orderNo = orderNo;
    }

    public double getPenalty() {
        return penalty;
    }

    public void setPenalty(double penalty) {
        this.penalty = penalty;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Order other = (Order) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
