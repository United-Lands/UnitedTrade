package org.unitedlands.trade.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OrderTemplateItemGroup {

    private List<String> materials = new ArrayList<>();
    private int unitSize;
    private int minUnits;
    private int maxUnits;

    public List<String> getMaterials() {
        return materials;
    }

    public String getRandomMaterialGroup() {
        var rnd = new Random();
        if (materials == null || materials.isEmpty())
            return null;
        return materials.get(rnd.nextInt(0, materials.size()));
    }

    public void setMaterials(List<String> materials) {
        this.materials = materials;
    }

    public int getUnitSize() {
        return unitSize;
    }

    public void setUnitSize(int unitSize) {
        this.unitSize = unitSize;
    }

    public int getMinUnits() {
        return minUnits;
    }

    public void setMinUnits(int minUnits) {
        this.minUnits = minUnits;
    }

    public int getMaxUnits() {
        return maxUnits;
    }

    public void setMaxUnits(int maxUnits) {
        this.maxUnits = maxUnits;
    }

    public int getRandomAmount() {
        if (minUnits == maxUnits)
            return unitSize * minUnits;

        var rnd = new Random();
        int randomUnits = rnd.nextInt(minUnits, maxUnits);

        return randomUnits * unitSize;
    }

}