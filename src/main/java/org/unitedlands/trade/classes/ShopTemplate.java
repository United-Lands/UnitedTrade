package org.unitedlands.trade.classes;

import java.util.ArrayList;
import java.util.List;

public class ShopTemplate {

    private long buyCooldown;
    private boolean buyCooldownPerItem;
    private List<ShopItem> shopItems = new ArrayList<>();
    private double minReputation;

    public long getBuyCooldown() {
        return buyCooldown;
    }
    public void setBuyCooldown(long buyCooldown) {
        this.buyCooldown = buyCooldown;
    }
    public boolean isBuyCooldownPerItem() {
        return buyCooldownPerItem;
    }
    public void setBuyCooldownPerItem(boolean buyCooldownPerItem) {
        this.buyCooldownPerItem = buyCooldownPerItem;
    }
    public List<ShopItem> getShopItems() {
        return shopItems;
    }
    public void setShopItems(List<ShopItem> shopItems) {
        this.shopItems = shopItems;
    }
    public double getMinReputation() {
        return minReputation;
    }
    public void setMinReputation(double minReputation) {
        this.minReputation = minReputation;
    }

    
}
