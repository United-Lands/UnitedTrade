package org.unitedlands.trade.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.UnitedLib;
import org.unitedlands.factories.items.IItemFactory;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.classes.MessageProvider;
import org.unitedlands.trade.utils.TradeOrderBookUtil;
import org.unitedlands.utils.Messenger;

public class CheckOrderCommand implements CommandExecutor {

    @SuppressWarnings("unused")
    private final UnitedTrade plugin;
    private final MessageProvider messageProvider;

    public CheckOrderCommand(UnitedTrade plugin, MessageProvider messageProvider) {
        this.plugin = plugin;
        this.messageProvider = messageProvider;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command label, @NotNull String alias,
            @NotNull String @NotNull [] args) {

        Player player = (Player) sender;
        IItemFactory itemFactory = UnitedLib.getInstance().getItemFactory();

        var book = player.getInventory().getItemInMainHand();
        if (book == null || !TradeOrderBookUtil.isTradeOrderBook(book)) {
            Messenger.sendMessage(player, messageProvider.get("messages.errors.must-hold-trade-book"), null,
                    messageProvider.get("messages.prefix"));
            return false;
        }

        var requiredItems = TradeOrderBookUtil.getRequiredItems(book);
        List<ItemStack> missingItems = TradeOrderBookUtil.getMissingItems(player, requiredItems);

        if (missingItems.isEmpty()) {
            Messenger.sendMessage(player, messageProvider.get("messages.checkorder.success"), null,
                    messageProvider.get("messages.prefix"));
        } else {
            List<String> missing = new ArrayList<>();
            for (ItemStack item : missingItems) {
                String itemStr = itemFactory.getDisplayName(item);
                itemStr += " x" + item.getAmount();
                missing.add(itemStr);
            }

            Messenger.sendMessage(player, messageProvider.get("messages.checkorder.missing"),
                    Map.of("missing", String.join(", ", missing)), messageProvider.get("messages.prefix"));

        }

        return true;
    }

}
