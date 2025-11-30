package org.unitedlands.trade.commands.admin.tradepoints.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.unitedlands.classes.BaseCommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.classes.TradePoint;
import org.unitedlands.utils.Messenger;

public class AdminTradePointsCreateHandler extends BaseCommandHandler<UnitedTrade> {

    public AdminTradePointsCreateHandler(UnitedTrade plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);
    }

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {

        if (args.length != 1) {
            Messenger.sendMessage(sender, messageProvider.get("messages.usage.create-tradepoint"), null,
                    messageProvider.get("messages.prefix"));
            return;
        }

        var name = args[0];
        var player = (Player) sender;

        TradePoint tradePoint = new TradePoint();
        tradePoint.setId(UUID.randomUUID());
        tradePoint.setName(name);
        tradePoint.setOwnerName("NPC_Trader");
        tradePoint.setLocation(player.getLocation());
        tradePoint.setFacing(player.getFacing().getOppositeFace().toString());   
        
        plugin.getTradePointManager().registerTradePoint(tradePoint);
        plugin.getTradePointManager().saveTradePoint(tradePoint, sender);

        tradePoint.spawnLectern();
    }

}
