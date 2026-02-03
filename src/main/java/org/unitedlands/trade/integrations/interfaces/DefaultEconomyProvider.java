package org.unitedlands.trade.integrations.interfaces;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.utils.Messenger;

public class DefaultEconomyProvider implements IEconomyProvider {

    private final UnitedTrade plugin;
    private final IMessageProvider messageProvider;

    public DefaultEconomyProvider(UnitedTrade plugin, IMessageProvider messageProvider) {
        this.plugin = plugin;
        this.messageProvider = messageProvider;
    }

    @Override
    public double getPlayerBalance(Player player) {
        return Double.MAX_VALUE;
    }

    @Override
    public boolean takeMoneyFromPlayer(Player player, double amount) {
        return takeMoneyFromPlayer(player, amount, null);
    }

    @Override
    public boolean takeMoneyFromPlayer(Player player, double amount, String reason) {
        if (amount > 0) {
            var cmd = plugin.getConfig().getString("take-command");
            cmd = cmd.replace("{user}", player.getName());
            cmd = cmd.replace("{amount}", amount + "");
            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);

            Messenger.sendMessage(player, messageProvider.get("messages.paid-money"),
                    Map.of("amount", String.format("%,.2f", amount)), messageProvider.get("messages.prefix"));
        }
        return true;
    }

    @Override
    public boolean giveMoneyToPlayer(Player player, double amount) {
        return giveMoneyToPlayer(player, amount, null);
    }

    @Override
    public boolean giveMoneyToPlayer(Player player, double amount, String reason) {
        if (amount > 0) {
            var cmd = plugin.getConfig().getString("give-command");
            cmd = cmd.replace("{user}", player.getName());
            cmd = cmd.replace("{amount}", amount + "");
            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
            
            Messenger.sendMessage(player, messageProvider.get("messages.received-money"),
                    Map.of("amount", String.format("%,.2f", amount)), messageProvider.get("messages.prefix"));
        }
        return true;
    }

}
