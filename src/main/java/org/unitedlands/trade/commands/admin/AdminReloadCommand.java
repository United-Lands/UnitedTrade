package org.unitedlands.trade.commands.admin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.unitedlands.classes.BaseCommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.utils.Logger;
import org.unitedlands.utils.United;

public class AdminReloadCommand extends BaseCommandHandler<UnitedTrade>{

    public AdminReloadCommand(UnitedTrade plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);
    }

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {

        United.logger().info("Stopping schedulers...");
        plugin.getOrderTracker().stopTracking();

        United.logger().info("Reloading configs...");

        plugin.reloadConfig();
        plugin.getPriceConfig().reload();
        
        plugin.getMessageProvider().reload(plugin.getConfig());

        plugin.getOrderTemplateManager().loadTemplates();
        plugin.getShopTemplateManager().loadTemplates();
        plugin.getTradePointManager().loadTradePoints();
        plugin.getDropoffPointManager().loadDropoffPoints();
        plugin.getShopPointManager().loadShopPoints();
        plugin.getOrderTracker().loadTrackedOrders();

        United.logger().info("Starting schedulers...");
        plugin.getOrderTracker().startTracking();

        United.logger().info("Done.");

        United.messenger().send(sender, "reload");

    }



}
