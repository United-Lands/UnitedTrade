package org.unitedlands.trade.commands.admin.tradepoints.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.unitedlands.classes.BaseCommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.utils.Messenger;

public class AdminTradePointsRemoveTemplateHandler extends BaseCommandHandler<UnitedTrade> {

    public AdminTradePointsRemoveTemplateHandler(UnitedTrade plugin, IMessageProvider messageProvider) {
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
            Messenger.sendMessage(sender, messageProvider.get("messages.usage.removetemplate-tradepoint"), null,
                    messageProvider.get("messages.prefix"));
            return;
        }

        Player player = (Player) sender;

        var tradePoint = plugin.getTradePointManager().getClosestTradepointInRadius(player.getLocation(), 2);
        if (tradePoint == null) {
            Messenger.sendMessage(sender, messageProvider.get("messages.errors.no-close-tradepoint"), null,
                    messageProvider.get("messages.prefix"));
            return;
        }

        if (tradePoint.getOrderTemplates().remove(args[0]))
            plugin.getTradePointManager().saveTradePoint(tradePoint, sender);
    }

}
