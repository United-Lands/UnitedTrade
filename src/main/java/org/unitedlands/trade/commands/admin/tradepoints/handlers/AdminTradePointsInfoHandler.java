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
import org.unitedlands.utils.United;

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
            United.messenger().send(sender, "usage.tradepoint-info");
            return;
        }

        Player player = (Player) sender;
        var tradePoint = plugin.getTradePointManager().getClosestTradepointInRadius(player.getLocation(), 2);
        if (tradePoint == null) {
            United.messenger().send(sender, "errors.no-close-tradepoint");
            return;
        }

        United.messenger().send(player, FieldHelper.getFieldValuesString(TradePoint.class, tradePoint));

    }

}
