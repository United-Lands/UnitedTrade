package org.unitedlands.trade.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OrderTemplateItemGroup {

    private List<String> materialGroups = new ArrayList<>();
    private int unitSize;
    private int minUnits;
    private int maxUnits;

    public List<String> getMaterialGroups() {
        return materialGroups;
    }

    public String getRandomMaterialGroup() {
        var rnd = new Random();
        if (materialGroups == null || materialGroups.isEmpty())
            return null;
        return materialGroups.get(rnd.nextInt(0, materialGroups.size()));
    }

    public void setMaterialGroups(List<String> materials) {
        this.materialGroups = materials;
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