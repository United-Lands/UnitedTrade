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
import org.bukkit.Registry;
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

import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;

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
    private boolean replaceBookOnRestock = false;
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
    private long customRestockFrequency = 0;
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

    @Expose
    @Info
    private String requiredPermissions;
    @Expose
    @Info
    private String blacklistedPermissions;

    @Expose
    @Info
    private double minReputation = 0;

    @Expose
    @Info
    private double reputationOnComplete = 5;

    @Expose
    @Info
    private double reputationOnFail = -5;

    private long lastRestockTime = 0;

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

    public String getRequiredPermissions() {
        return requiredPermissions;
    }

    public void setRequiredPermissions(String requiredPermissions) {
        this.requiredPermissions = requiredPermissions;
    }

    public String getBlacklistedPermissions() {
        return blacklistedPermissions;
    }

    public void setBlacklistedPermissions(String blacklistedPermissions) {
        this.blacklistedPermissions = blacklistedPermissions;
    }

    public double getMinReputation() {
        return minReputation;
    }

    public void setMinReputation(double minReputation) {
        this.minReputation = minReputation;
    }

    public double getReputationOnComplete() {
        return reputationOnComplete;
    }

    public void setReputationOnComplete(double reputationOnComplete) {
        this.reputationOnComplete = reputationOnComplete;
    }

    public double getReputationOnFail() {
        return reputationOnFail;
    }

    public void setReputationOnFail(double reputationOnFail) {
        this.reputationOnFail = reputationOnFail;
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

        if (!this.getLocation().getChunk().isLoaded())
            return;
        
        Block block = this.location.getBlock();
        if (block.getType() != Material.LECTERN)
            spawnLectern();

        if (customRestockFrequency != 0)
        {
            if (System.currentTimeMillis() - lastRestockTime < (customRestockFrequency * 1000))
                return;
        }

        if (block.getBlockData() instanceof org.bukkit.block.data.type.Lectern lectern) {
            if (lectern.hasBook()) {
                if (this.replaceBookOnRestock) {
                    removeBook();
                } else {
                    return;
                }
            }

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

                    Particle restockParticle = Particle.DUST_PLUME;
                    try {
                        restockParticle = Registry.PARTICLE_TYPE.get(TypedKey.create(RegistryKey.PARTICLE_TYPE,
                                UnitedTrade.getInstance().getConfig().getString("effects.complete-particle")));
                    } catch (Exception ignore) {
                    }
                    Sound restockSound = Sound.ENTITY_ENDERMITE_DEATH;
                    try {
                        restockSound = Registry.SOUNDS.get(TypedKey.create(RegistryKey.SOUND_EVENT,
                                UnitedTrade.getInstance().getConfig().getString("effects.complete-sound")));
                    } catch (Exception ignore) {
                    }

                    block.getWorld().spawnParticle(restockParticle, loc, 16, 0.5, 0.5, 0.5);
                    block.getWorld().playSound(loc, restockSound, 8f, 1f);

                    lastRestockTime = System.currentTimeMillis();
                }
            }

        }

    }

    public void removeBook() {
        Block block = this.location.getBlock();
        if (block.getType() != Material.LECTERN)
            return;

        if (block.getBlockData() instanceof org.bukkit.block.data.type.Lectern lectern) {
            org.bukkit.block.Lectern lecternState = (org.bukkit.block.Lectern) block.getState();
            LecternInventory lecternInv = (LecternInventory) lecternState.getInventory();
            lecternInv.setBook(null);

            lectern.setHasBook(false);
            block.setBlockData(lectern, true);
        }
        return;
    }

    public ItemStack getBook() {
        Block block = this.location.getBlock();
        if (block.getType() != Material.LECTERN)
            return null;

        if (block.getBlockData() instanceof org.bukkit.block.data.type.Lectern) {
            org.bukkit.block.Lectern lecternState = (org.bukkit.block.Lectern) block.getState();
            LecternInventory lecternInv = (LecternInventory) lecternState.getInventory();
            return lecternInv.getBook();
        }
        return null;
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
