package org.unitedlands.trade.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.LecternInventory;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.utils.TradeOrderBookUtil;
import org.unitedlands.trade.utils.annotations.Info;

import com.google.gson.annotations.Expose;

public class TradePoint {

    @Expose
    @Info
    private UUID id;
    @Expose
    private Location location;
    @Expose
    @Info
    private boolean enabled = false;
    @Expose
    @Info
    private int currentOrderNo = 1;
    @Expose
    private String facing;
    @Expose
    @Info
    private String name;
    @Expose
    @Info
    private String ownerName;
    @Expose
    @Info
    private long pickupCooldown;
    @Expose
    @Info
    private boolean applyContractPenalties = false;
    @Expose
    @Info
    private double contractPenalty = 0.1d;

    @Expose
    private List<String> orderTemplates = new ArrayList<>();
    @Expose
    private Map<UUID, Long> playerPickupCooldowns = new HashMap<>();

    public TradePoint() {

    }

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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getFacing() {
        return facing;
    }

    public void setFacing(String facing) {
        this.facing = facing;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getCleanOwnerName() {
        return ownerName.replace("_", " ");
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public long getPickupCooldown() {
        return pickupCooldown;
    }

    public void setPickupCooldown(long cooldown) {
        this.pickupCooldown = cooldown;
    }

    public List<String> getOrderTemplates() {
        return orderTemplates;
    }

    public void setOrderTemplates(List<String> orderTemplates) {
        this.orderTemplates = orderTemplates;
    }

    public int getAndRaiseOrderNo() {
        var current = currentOrderNo;
        currentOrderNo++;

        UnitedTrade.getInstance().getTradePointManager().saveTradePoint(this);

        return current;
    }

    public boolean applyContractPenalties() {
        return applyContractPenalties;
    }

    public void setApplyContractPenalties(boolean applyContractPenalties) {
        this.applyContractPenalties = applyContractPenalties;
    }

    public double getContractPenalty() {
        return contractPenalty;
    }

    public void setContractPenalty(double contractPenalty) {
        this.contractPenalty = contractPenalty;
    }

    public void spawnLectern() {

        var block = this.location.getBlock();
        if (block.getType() == Material.LECTERN)
            return;

        block.setType(Material.LECTERN);
        BlockFace face = BlockFace.valueOf(this.facing);
        var directional = (Directional) block.getBlockData();
        directional.setFacing(face);
        block.setBlockData(directional);
    }

    public void addPlayerPickupCooldown(UUID playerId) {
        playerPickupCooldowns.put(playerId, System.currentTimeMillis());
        UnitedTrade.getInstance().getTradePointManager().saveTradePoint(this);
    }

    public void removePlayerPuickupCooldown(UUID playerId) {
        playerPickupCooldowns.remove(playerId);
        UnitedTrade.getInstance().getTradePointManager().saveTradePoint(this);
    }

    public boolean isPlayerOnPickupCooldown(UUID playerId) {
        var cooldownStart = playerPickupCooldowns.get(playerId);
        if (cooldownStart == null)
            return false;

        if (System.currentTimeMillis() - cooldownStart < (pickupCooldown * 1000))
            return true;

        return false;
    }

    public void restock() {

        if (!enabled)
            return;

        if (this.orderTemplates == null || this.orderTemplates.isEmpty())
            return;

        Block block = this.location.getBlock();
        if (block.getType() != Material.LECTERN)
            spawnLectern();

        if (block.getBlockData() instanceof org.bukkit.block.data.type.Lectern lectern) {
            if (lectern.hasBook())
                return;

            Random rnd = new Random();
            String randomTemplate = orderTemplates.get(rnd.nextInt(0, orderTemplates.size()));

            Order order = UnitedTrade.getInstance().getOrderTemplateManager().generateRandomOrder(randomTemplate, this);
            if (order != null) {
                ItemStack book = TradeOrderBookUtil.createBook(order);
                if (book != null) {

                    org.bukkit.block.Lectern lecternState = (org.bukkit.block.Lectern) block.getState();
                    LecternInventory lecternInv = (LecternInventory) lecternState.getInventory();
                    lecternInv.setBook(book);

                    lectern.setHasBook(true);
                    block.setBlockData(lectern, true);

                    Location loc = block.getLocation().clone().add(0.5, 0.5, 0.5);

                    block.getWorld().spawnParticle(Particle.FIREWORK, loc, 16, 0.5, 0.5, 0.5);
                    block.getWorld().playSound(loc, Sound.ENTITY_ENDERMITE_DEATH, 8f, 1f);
                }
            }

        }

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        TradePoint other = (TradePoint) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

}
