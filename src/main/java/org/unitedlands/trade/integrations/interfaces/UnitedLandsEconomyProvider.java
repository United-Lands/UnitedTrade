package org.unitedlands.trade.integrations.interfaces;

import org.bukkit.entity.Player;
import org.unitedlands.unitedlands.managers.UnitedLandsDataManager;
import org.unitedlands.unitedlands.managers.UnitedLandsEconomyManager;
import org.unitedlands.utils.United;

public class UnitedLandsEconomyProvider implements IEconomyProvider {

    @Override
    public double getPlayerBalance(Player player) {
        return UnitedLandsEconomyManager.instance().getBalance(player.getUniqueId()).doubleValue();
    }

    @Override
    public boolean takeMoneyFromPlayer(Player player, double amount) {
        return UnitedLandsEconomyManager.instance().withdraw(player.getUniqueId(), amount, null);
    }

    @Override
    public boolean takeMoneyFromPlayer(Player player, double amount, String reason) {
        return UnitedLandsEconomyManager.instance().withdraw(player.getUniqueId(), amount, reason);
    }

    @Override
    public boolean giveMoneyToPlayer(Player player, double amount) {
        return giveMoneyToPlayer(player, amount, null);
    }

    @Override
    public boolean giveMoneyToPlayer(Player player, double amount, String reason) {
        var citizen = UnitedLandsDataManager.instance().getCitizen(player);
        if (citizen == null) {
            United.logger().warning("Could not get citizen data of player " + player.getName());
            return UnitedLandsEconomyManager.instance().deposit(player.getUniqueId(), amount, reason);
        }
        return UnitedLandsEconomyManager.instance().depositAndTax(citizen, amount, reason);
    }

}
