package org.unitedlands.trade.integrations.interfaces;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.utils.United;

import net.milkbowl.vault.economy.Economy;

public class VaultEcononyProvider implements IEconomyProvider {

    private final Economy economy;

    public VaultEcononyProvider() {
        RegisteredServiceProvider<Economy> rsp = UnitedTrade.instance().getServer().getServicesManager().getRegistration(Economy.class);
        economy = rsp.getProvider();
    }

    @Override
    public String format(double amount) {
        return economy.format(amount);
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
            United.messenger().send(player, "paid-money", String.format("%,.2f", amount));
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
            United.messenger().send(player, "received-money", String.format("%,.2f", amount));
        }
        return response.transactionSuccess();
    }

}
