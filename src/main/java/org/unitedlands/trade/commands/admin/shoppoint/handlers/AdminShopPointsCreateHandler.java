package org.unitedlands.trade.commands.admin.shoppoint.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.unitedlands.classes.BaseCommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.classes.ShopPoint;
import org.unitedlands.utils.Messenger;

public class AdminShopPointsCreateHandler extends BaseCommandHandler<UnitedTrade> {

    public AdminShopPointsCreateHandler(UnitedTrade plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);
    }

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {

        if (args.length != 0) {
            Messenger.sendMessage(sender, messageProvider.get("messages.usage.create-shoppoint"), null,
                    messageProvider.get("messages.prefix"));
            return;
        }

        var player = (Player) sender;

        ShopPoint shopPoint = new ShopPoint();
        shopPoint.setId(UUID.randomUUID());
        shopPoint.setLocation(player.getLocation());
        shopPoint.setFacing(player.getFacing().getOppositeFace().toString());   
        
        plugin.getShopPointManager().registerShopPoint(shopPoint);
        plugin.getShopPointManager().saveShopPoint(shopPoint, sender);

        shopPoint.spawnChest();
    }

}
