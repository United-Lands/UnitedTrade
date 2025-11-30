package org.unitedlands.trade.classes;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;

import com.google.gson.annotations.Expose;

public class DropoffPoint {

    @Expose
    private UUID id;
    @Expose
    private UUID tradepointId;
    @Expose
    private Location location;
    @Expose
    private String facing;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTradepointId() {
        return tradepointId;
    }

    public void setTradepointId(UUID tradepointId) {
        this.tradepointId = tradepointId;
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
        DropoffPoint other = (DropoffPoint) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    public void spawnChest() {

        var block = this.location.getBlock();
        if (block.getType() == Material.TRAPPED_CHEST)
            return;

        block.setType(Material.TRAPPED_CHEST);
        BlockFace face = BlockFace.valueOf(this.facing);
        var directional = (Directional) block.getBlockData();
        directional.setFacing(face);
        block.setBlockData(directional);
    }

}
