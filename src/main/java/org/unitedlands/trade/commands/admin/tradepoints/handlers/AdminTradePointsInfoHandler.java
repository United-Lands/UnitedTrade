package org.unitedlands.trade.commands.admin.tradepoints.handlers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.unitedlands.classes.BaseCommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.classes.TradePoint;
import org.unitedlands.trade.utils.FieldHelper;
import org.unitedlands.utils.Messenger;

public class AdminTradePointsInfoHandler extends BaseCommandHandler<UnitedTrade> {

    public AdminTradePointsInfoHandler(UnitedTrade plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);
    }

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {

        if (args.length != 0) {
            Messenger.sendMessage(sender, messageProvider.get("messages.usage.tradepoint-info"), null,
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

        Messenger.sendMessage(player, FieldHelper.getFieldValuesString(TradePoint.class, tradePoint));

    }

}
