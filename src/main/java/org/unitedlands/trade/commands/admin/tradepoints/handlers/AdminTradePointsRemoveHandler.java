package org.unitedlands.trade.commands.admin.tradepoints.handlers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.unitedlands.classes.BaseCommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.utils.Messenger;

public class AdminTradePointsRemoveHandler extends BaseCommandHandler<UnitedTrade> {

    public AdminTradePointsRemoveHandler(UnitedTrade plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);
    }

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {

        if (args.length != 0) {
            Messenger.sendMessage(sender, messageProvider.get("messages.usage.remove-tradepoint"), null,
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

        tradePoint.getLocation().getBlock().setType(Material.AIR);
        
        plugin.getTradePointManager().unregisterTradePoint(tradePoint);
        plugin.getTradePointManager().deleteFile(tradePoint, sender);

    }

}
