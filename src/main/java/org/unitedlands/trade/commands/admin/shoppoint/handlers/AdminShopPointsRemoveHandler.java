package org.unitedlands.trade.commands.admin.shoppoint.handlers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.unitedlands.classes.BaseCommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.classes.ShopPoint;
import org.unitedlands.utils.Messenger;

public class AdminShopPointsRemoveHandler extends BaseCommandHandler<UnitedTrade> {

    public AdminShopPointsRemoveHandler(UnitedTrade plugin, IMessageProvider messageProvider) {
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

        ShopPoint shopPoint = plugin.getShopPointManager().getClosestShopPointInRadius(player.getLocation(),
                2);
        if (shopPoint == null) {
            Messenger.sendMessage(sender, messageProvider.get("messages.errors.no-close-shoppoint"), null,
                    messageProvider.get("messages.prefix"));
            return;
        }

        shopPoint.getLocation().getBlock().setType(Material.AIR);

        plugin.getShopPointManager().unregisterShopPoint(shopPoint);
        plugin.getShopPointManager().deleteFile(shopPoint, sender);
    }

}
