package org.unitedlands.trade.classes;

public class OrderBarterItem {
    private String material;
    private int amount;

    
    public OrderBarterItem() {
    }

    public OrderBarterItem(String material, int amount) {
        this.material = material;
        this.amount = amount;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
   
}
