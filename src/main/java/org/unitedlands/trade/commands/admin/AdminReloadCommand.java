package org.unitedlands.trade.commands.admin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.unitedlands.classes.BaseCommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.utils.Logger;
import org.unitedlands.utils.Messenger;

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

        Logger.log("Stopping schedulers...", "UnitedTrade");
        plugin.getOrderTracker().stopTracking();

        Logger.log("Reloading configs...", "UnitedTrade");

        plugin.reloadConfig();
        plugin.getMaterialConfig().reload();
        
        plugin.getMessageProvider().reload(plugin.getConfig());

        plugin.getOrderTemplateManager().loadTemplates();
        plugin.getTradePointManager().loadTradePoints();
        plugin.getDropoffPointManager().loadDropoffPoints();
        plugin.getOrderTracker().loadTrackedOrders();

        Logger.log("Starting schedulers...", "UnitedTrade");
        plugin.getOrderTracker().startTracking();

        Logger.log("Done.", "UnitedTrade");

        Messenger.sendMessage(sender, messageProvider.get("messages.reload"), null, messageProvider.get("messages.prefix"));

    }



}
