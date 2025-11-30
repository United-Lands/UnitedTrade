package org.unitedlands.trade.commands.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.unitedlands.classes.BaseCommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.classes.Order;
import org.unitedlands.trade.utils.TradeOrderBookUtil;
import org.unitedlands.utils.Logger;

public class AdminCreateTestOrderCommand extends BaseCommandHandler<UnitedTrade> {

    public AdminCreateTestOrderCommand(UnitedTrade plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);
    }

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        if (args.length == 1)
        {
            return plugin.getOrderTemplateManager().getOrderTemplateKeys().stream().collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {

        Player player = (Player) sender;

        var templateName = args[0];

        Order order = plugin.getOrderTemplateManager().generateRandomOrder(templateName, null);
        if (order == null)
        {
            Logger.logError("Couldn't get random order for template " + args[0], "UnitedTrade");
            return;
        }

        var book = TradeOrderBookUtil.createBook(order);

        player.getInventory().addItem(book);

    }

}
