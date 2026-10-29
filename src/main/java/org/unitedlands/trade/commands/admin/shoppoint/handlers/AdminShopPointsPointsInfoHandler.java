package org.unitedlands.trade.commands.admin.shoppoint.handlers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.unitedlands.classes.BaseCommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.classes.ShopPoint;
import org.unitedlands.trade.utils.FieldHelper;
import org.unitedlands.utils.United;

public class AdminShopPointsPointsInfoHandler extends BaseCommandHandler<UnitedTrade> {

    public AdminShopPointsPointsInfoHandler(UnitedTrade plugin, IMessageProvider messageProvider) {
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
        ShopPoint shopPoint = plugin.getShopPointManager().getClosestShopPointInRadius(player.getLocation(),
                2);
        if (shopPoint == null) {
            United.messenger().send(sender, "errors.no-close-shoppoint");
            return;
        }

        United.messenger().send(player, FieldHelper.getFieldValuesString(ShopPoint.class, shopPoint));

    }

}
