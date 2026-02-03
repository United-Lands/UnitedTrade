package org.unitedlands.trade.integrations.interfaces;

import org.bukkit.entity.Player;

public interface IEconomyProvider {
    double getPlayerBalance(Player player);

    boolean takeMoneyFromPlayer(Player player, double amount);

    boolean takeMoneyFromPlayer(Player player, double amount, String reason);

    boolean giveMoneyToPlayer(Player player, double amount);

    boolean giveMoneyToPlayer(Player player, double amount, String reason);
}
