package org.unitedlands.trade.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OrderTemplate {

    private long timelimit;
    private boolean barter;

    private List<String> randomDescriptions = new ArrayList<>();
    private List<OrderTemplateItemGroup> itemGroups = new ArrayList<>();
    private List<OrderBarterItem> barterItems = new ArrayList<>();

    public long getTimelimit() {
        return timelimit;
    }

    public boolean isBarter() {
        return barter;
    }

    public void setBarter(boolean barter) {
        this.barter = barter;
    }

    public void setTimelimit(long timelimit) {
        this.timelimit = timelimit;
    }

    public String getRandomDescription() {
        var rnd = new Random();
        if (randomDescriptions == null || randomDescriptions.isEmpty())
            return null;
        return randomDescriptions.get(rnd.nextInt(0, randomDescriptions.size()));
    }

    public List<String> getRandomDescriptions() {
        return randomDescriptions;
    }

    public void setRandomDescriptions(List<String> randomDescriptions) {
        this.randomDescriptions = randomDescriptions;
    }

    public List<OrderTemplateItemGroup> getItemGroups() {
        return itemGroups;
    }

    public void setItemGroups(List<OrderTemplateItemGroup> items) {
        this.itemGroups = items;
    }

    public List<OrderBarterItem> getBarterItems() {
        return barterItems;
    }

    public void setBarterItems(List<OrderBarterItem> barterItems) {
        this.barterItems = barterItems;
    }

}
