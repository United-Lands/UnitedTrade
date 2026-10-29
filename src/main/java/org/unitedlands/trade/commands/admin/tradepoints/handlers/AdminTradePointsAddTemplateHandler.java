package org.unitedlands.trade.commands.admin.tradepoints.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.unitedlands.classes.BaseCommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.utils.United;

public class AdminTradePointsAddTemplateHandler extends BaseCommandHandler<UnitedTrade> {

    public AdminTradePointsAddTemplateHandler(UnitedTrade plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);
    }

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return plugin.getOrderTemplateManager().getOrderTemplateKeys().stream().collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {

        if (args.length != 1) {
            United.messenger().send(sender, "usage.addtemplate-tradepoint");
            return;
        }

        Player player = (Player) sender;

        var tradePoint = plugin.getTradePointManager().getClosestTradepointInRadius(player.getLocation(), 2);
        if (tradePoint == null) {
            United.messenger().send(sender, "errors.no-close-tradepoint");
            return;
        }

        if (tradePoint.getOrderTemplates().add(args[0]))
            plugin.getTradePointManager().saveTradePoint(tradePoint, sender);
    }

}
