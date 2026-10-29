package org.unitedlands.trade.commands.admin.dropoffpoint.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.unitedlands.classes.BaseCommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.classes.DropoffPoint;
import org.unitedlands.trade.classes.TradePoint;
import org.unitedlands.utils.United;

public class AdminDropoffPointsCreateHandler extends BaseCommandHandler<UnitedTrade> {

    public AdminDropoffPointsCreateHandler(UnitedTrade plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);
    }

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        if (args.length == 1)
            return plugin.getTradePointManager().getTradePointNames();
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {

        if (args.length != 1) {
            United.messenger().send(sender, "usage.create-dropoffpoint");
            return;
        }

        var tradepointName = args[0];
        TradePoint tradePoint = plugin.getTradePointManager().getTradePoint(tradepointName);
        if (tradePoint == null)
        {
            United.messenger().send(sender, "errors.unknown-tradepoint");
        }

        var player = (Player) sender;

        DropoffPoint dropoffPoint = new DropoffPoint();
        dropoffPoint.setId(UUID.randomUUID());
        dropoffPoint.setTradepointId(tradePoint.getId());
        dropoffPoint.setLocation(player.getLocation());
        dropoffPoint.setFacing(player.getFacing().getOppositeFace().toString());   
        
        plugin.getDropoffPointManager().registerDropoffPoint(dropoffPoint);
        plugin.getDropoffPointManager().saveDropoffPoint(dropoffPoint, sender);

        dropoffPoint.spawnChest();
    }

}
