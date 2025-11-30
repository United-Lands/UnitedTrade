package org.unitedlands.trade.commands.admin.dropoffpoint.handlers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.unitedlands.classes.BaseCommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.classes.DropoffPoint;
import org.unitedlands.utils.Messenger;

public class AdminDropoffPointsRemoveHandler extends BaseCommandHandler<UnitedTrade> {

    public AdminDropoffPointsRemoveHandler(UnitedTrade plugin, IMessageProvider messageProvider) {
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

        if (args.length != 0) {
            Messenger.sendMessage(sender, messageProvider.get("messages.usage.remove-dropoffpoint"), null,
                    messageProvider.get("messages.prefix"));
            return;
        }

        var player = (Player) sender;

        DropoffPoint dropoffPoint = plugin.getDropoffPointManager().getClosestDropoffPointInRadius(player.getLocation(),
                2);
        if (dropoffPoint == null) {
            Messenger.sendMessage(sender, messageProvider.get("messages.errors.no-close-dropoffpoint"), null,
                    messageProvider.get("messages.prefix"));
            return;
        }

        dropoffPoint.getLocation().getBlock().setType(Material.AIR);

        plugin.getDropoffPointManager().unregisterDropoffPoint(dropoffPoint);
        plugin.getDropoffPointManager().deleteFile(dropoffPoint, sender);
    }

}
