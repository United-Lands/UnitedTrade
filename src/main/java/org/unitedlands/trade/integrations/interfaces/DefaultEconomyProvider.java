package org.unitedlands.trade.integrations.interfaces;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.utils.United;

public class DefaultEconomyProvider implements IEconomyProvider {

    public DefaultEconomyProvider() {

    }

    
    @Override
    public String format(double amount) {
        return String.format("%.2f", amount);
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
            var cmd = UnitedTrade.instance().getConfig().getString("take-command");
            cmd = cmd.replace("{user}", player.getName());
            cmd = cmd.replace("{amount}", amount + "");
            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);

            United.messenger().send(player, "paid-money", String.format("%,.2f", amount));
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
            var cmd = UnitedTrade.instance().getConfig().getString("give-command");
            cmd = cmd.replace("{user}", player.getName());
            cmd = cmd.replace("{amount}", amount + "");
            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
            
            United.messenger().send(player, "received-money", String.format("%,.2f", amount));
        }
        return true;
    }

}
