package org.unitedlands.trade.commands.admin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.unitedlands.classes.BaseCommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.utils.TradeOrderBookUtil;
import org.unitedlands.utils.Logger;

public class AdminValidateTestOrderCommand extends BaseCommandHandler<UnitedTrade> {

    public AdminValidateTestOrderCommand(UnitedTrade plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);
    }

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {

        Player player = (Player) sender;

        var orderBook = player.getInventory().getItemInMainHand();

        if (!TradeOrderBookUtil.isTradeOrderBook(orderBook)) {
            Logger.log("Invalid trade book", "UnitedTrade");
            return;
        }

        var items = TradeOrderBookUtil.getRequiredItems(orderBook);
        Logger.log("Items: " + items.size());
        for (var item : items) {
            player.getInventory().addItem(item);
        }

    }

}
