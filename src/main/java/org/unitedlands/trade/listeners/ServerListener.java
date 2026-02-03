package org.unitedlands.trade.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.unitedlands.trade.UnitedTrade;

public class ServerListener implements Listener {

    private final UnitedTrade plugin;

    public ServerListener(UnitedTrade plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        plugin.getTradePointManager().loadTradePoints();
        plugin.getOrderTemplateManager().loadTemplates();
        plugin.getShopTemplateManager().loadTemplates();
        plugin.getDropoffPointManager().loadDropoffPoints();
        plugin.getShopPointManager().loadShopPoints();
        plugin.getOrderTracker().loadTrackedOrders();
        plugin.getOrderTracker().startTracking();
    }

}
