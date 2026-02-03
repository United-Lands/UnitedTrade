package org.unitedlands.trade.integrations.interfaces;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.utils.Messenger;

import net.milkbowl.vault.economy.Economy;

public class VaultEcononyProvider implements IEconomyProvider {

    @SuppressWarnings("unused")
    private final UnitedTrade plugin;
    private final IMessageProvider messageProvider;

    private final Economy economy;

    public VaultEcononyProvider(UnitedTrade plugin, IMessageProvider messageProvider) {
        this.plugin = plugin;
        this.messageProvider = messageProvider;
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        economy = rsp.getProvider();
    }

    @Override
    public double getPlayerBalance(Player player) {
        return economy.getBalance(player);
    }

    @Override
    public boolean takeMoneyFromPlayer(Player player, double amount) {
        return takeMoneyFromPlayer(player, amount, null);
    }

    @Override
    public boolean takeMoneyFromPlayer(Player player, double amount, String reason) {
        var response = economy.withdrawPlayer(player, reason, amount);
        if (response.transactionSuccess()) {
            Messenger.sendMessage(player, messageProvider.get("messages.paid-money"),
                    Map.of("amount", String.format("%,.2f", amount)), messageProvider.get("messages.prefix"));
        }
        return response.transactionSuccess();
    }

    @Override
    public boolean giveMoneyToPlayer(Player player, double amount) {
        return giveMoneyToPlayer(player, amount, null);
    }

    @Override
    public boolean giveMoneyToPlayer(Player player, double amount, String reason) {
        var response = economy.depositPlayer(player, reason, amount);
        if (response.transactionSuccess()) {
            Messenger.sendMessage(player, messageProvider.get("messages.received-money"),
                    Map.of("amount", String.format("%,.2f", amount)), messageProvider.get("messages.prefix"));
        }
        return response.transactionSuccess();
    }

}
