package org.unitedlands.trade.classes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.unitedlands.UnitedLib;

public class OrderItem {
    private ItemStack item;
    private int minAmount;
    private int maxAmount;
    private double price = 0d;

    public OrderItem() {
    }

    public OrderItem(ItemStack item, int minAmount, int maxAmount, double price) {
        this.item = item;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.price = price;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public int getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(int minAmount) {
        this.minAmount = minAmount;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(int maxAmount) {
        this.maxAmount = maxAmount;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getMinPayout() {
        return minAmount * price;
    }

    public double getMaxPayout() {
        return maxAmount * price;
    }

    public int missingAmount(PlayerInventory inventory) {
        int total = 0;
        var itemFactory = UnitedLib.getInstance().getItemFactory();
        var orderItemId = itemFactory.getFilterName(item);
        for (ItemStack stack : inventory.getStorageContents()) {
            if (stack != null && stack.getType() != Material.AIR) {
                var itemId = itemFactory.getFilterName(stack);
                if (itemId.equals(orderItemId)) {
                    total += stack.getAmount();
                }
            }
        }
        return Math.max(0, minAmount - total);
    }

    public int removeFromInventory(PlayerInventory inventory) {

        var itemFactory = UnitedLib.getInstance().getItemFactory();
        var orderItemId = itemFactory.getFilterName(item);

        // First pass: count total available
        int total = 0;
        for (ItemStack stack : inventory.getStorageContents()) {
            if (stack != null && stack.getType() != Material.AIR) {
                var itemId = itemFactory.getFilterName(stack);
                if (itemId.equals(orderItemId)) {
                    total += stack.getAmount();
                }
            }
        }

        if (total < minAmount)
            return -1;

        // Second pass: remove up to maxAmount
        int remaining = Math.min(total, maxAmount);
        int removed = remaining;
        ItemStack[] contents = inventory.getStorageContents();

        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack stack = contents[i];
            var itemId = itemFactory.getFilterName(stack);

            if (stack == null || !itemId.equals(orderItemId))
                continue;

            if (stack.getAmount() <= remaining) {
                remaining -= stack.getAmount();
                contents[i] = null;
            } else {
                stack.setAmount(stack.getAmount() - remaining);
                remaining = 0;
            }
        }

        inventory.setStorageContents(contents);
        return removed;
    }

}
