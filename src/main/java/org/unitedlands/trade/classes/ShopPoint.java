package org.unitedlands.trade.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.UnitedLib;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.utils.annotations.Info;
import org.unitedlands.utils.Logger;

import com.google.gson.annotations.Expose;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ShopPoint implements InventoryHolder {

    @Expose
    private UUID id;
    @Expose
    private Location location;
    @Expose
    private String facing;
    @Expose
    @Info
    private String template;
    @Expose
    @Info
    private double refillFrequency;
    @Expose
    @Info
    private boolean useGlobalInventory;
    @Expose
    @Info
    private double minReputation = -200;

    private Inventory globalInventory;
    long inventoryGenerationTime;

    Map<UUID, Inventory> playerInventories = new HashMap<>();
    Map<UUID, Long> playerIventoryGenerationTimes = new HashMap<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getFacing() {
        return facing;
    }

    public void setFacing(String facing) {
        this.facing = facing;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String templateId) {
        this.template = templateId;
    }

    public boolean useGlobalInventory() {
        return useGlobalInventory;
    }

    public void setUseGlobalInventory(boolean useGlobalInventory) {
        this.useGlobalInventory = useGlobalInventory;
    }

    public double getMinReputation() {
        return minReputation;
    }

    public void setMinReputation(double minReputation) {
        this.minReputation = minReputation;
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
        ShopPoint other = (ShopPoint) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    public void spawnChest() {

        Logger.log("Spawning chest for shop point " + id, "UnitedTrade");

        var block = this.location.getBlock();
        block.setType(Material.TRAPPED_CHEST);
        BlockFace face = BlockFace.valueOf(this.facing);
        var directional = (Directional) block.getBlockData();
        directional.setFacing(face);
        block.setBlockData(directional);
    }

    void fillInventory(Inventory inventory) {

        if (template == null)
            return;

        var shopTemplate = UnitedTrade.getInstance().getShopTemplateManager().getShopTemplate(template);
        if (shopTemplate == null) {
            Logger.logError("Could not find shop template key " + template, "UnitedTrade");
            return;
        }

        var itemFactory = UnitedLib.getInstance().getItemFactory();
        for (ShopItem shopItem : shopTemplate.getShopItems()) {
            ItemStack item = itemFactory.getItemStack(shopItem.getMaterial(), shopItem.getUnitSize());
            if (item != null) {

                var currency = UnitedTrade.getInstance().getConfig().getString("messages.currency", "$");
                var miniMessage = MiniMessage.miniMessage();

                var meta = item.getItemMeta();
                List<Component> lore = new ArrayList<>();

                lore.add(miniMessage.deserialize("<!italic><gold><bold>" + String.format("%,.2f", shopItem.getPrice()) + currency + "</bold></gold>"));
                lore.add(Component.empty());
                lore.add(miniMessage.deserialize("<reset><gray><em>Click to purchase</em></gray>"));
                meta.lore(lore);

                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                pdc.set(new NamespacedKey(UnitedTrade.getInstance(), "shopitem.price"), PersistentDataType.DOUBLE,
                        shopItem.getPrice());

                item.setItemMeta(meta);

                inventory.addItem(item);
            }
        }
    }

    @Override
    public @NotNull Inventory getInventory() {

        if (System.currentTimeMillis() - inventoryGenerationTime > refillFrequency * 1000) {
            if (globalInventory == null) {
                globalInventory = Bukkit.createInventory(this, InventoryType.CHEST,
                        Component.text("Trader Shop (Global)"));
            }
            globalInventory.clear();
            inventoryGenerationTime = System.currentTimeMillis();
            fillInventory(globalInventory);
        }

        return globalInventory;
    }

    public Inventory getInventory(Player player) {

        Inventory playerInventory = null;
        if (playerInventories.containsKey(player.getUniqueId())) {
            playerInventory = playerInventories.get(player.getUniqueId());
            if (playerIventoryGenerationTimes.containsKey(player.getUniqueId())) {
                var generationTime = playerIventoryGenerationTimes.get(player.getUniqueId());
                if (System.currentTimeMillis() - generationTime > refillFrequency * 1000) {
                    playerInventory.clear();
                    fillInventory(playerInventory);
                    playerIventoryGenerationTimes.put(player.getUniqueId(), System.currentTimeMillis());
                }
            }
        } else {
            playerInventory = Bukkit.createInventory(this, InventoryType.CHEST,
                    Component.text("Trader Shop (" + player.getName() + ")"));
            fillInventory(playerInventory);
            playerInventories.put(player.getUniqueId(), playerInventory);
            playerIventoryGenerationTimes.put(player.getUniqueId(), System.currentTimeMillis());
        }

        return playerInventory;
    }

    public long getTimeToRefresh(Player player) {

        if (useGlobalInventory) {
            return (long)Math.round(refillFrequency * 1000) - Math.max(0, System.currentTimeMillis() - inventoryGenerationTime);
        } else {
            if (playerIventoryGenerationTimes.containsKey(player.getUniqueId()))
                return (long)Math.round(refillFrequency * 1000) - Math.max(0,
                        System.currentTimeMillis() - playerIventoryGenerationTimes.get(player.getUniqueId()));
        }
        return 0;
    }

}
