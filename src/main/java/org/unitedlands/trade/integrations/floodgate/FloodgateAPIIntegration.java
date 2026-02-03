package org.unitedlands.trade.integrations.floodgate;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.classes.TradePoint;
import org.unitedlands.trade.utils.TradeOrderBookUtil;

public class FloodgateAPIIntegration {

    private final UnitedTrade plugin;
    private final FloodgateApi instance;

    public FloodgateAPIIntegration(UnitedTrade plugin) {
        this.plugin = plugin;
        this.instance = FloodgateApi.getInstance();
    }

    public FloodgateApi getInstance() {
        return instance;
    }

    public boolean isBedrockPlayer(Player player) {
        return instance.isFloodgatePlayer(player.getUniqueId());
    }

    public void sendTradePointOrderPanel(Player player, TradePoint tradePoint, ItemStack book) {

        var content = TradeOrderBookUtil.getFloodgateContent(book);

        FloodgatePlayer floodgateplayer = FloodgateApi.getInstance().getPlayer(player.getUniqueId());
        floodgateplayer.sendForm(
                SimpleForm.builder()
                        .title(tradePoint.getCleanOwnerName() + "'s Trade Order")
                        .content(content)
                        .button("Accept trade order")
                        .validResultHandler(response -> handleClick(player, tradePoint, book)));
    }

    private void handleClick(Player player, TradePoint tradePoint, ItemStack book) {

        if (plugin.getOrderTracker().acceptTradeOrder(player, tradePoint, book)) {
            tradePoint.removeBook();
            var leftover = player.getInventory().addItem(book);
            if (leftover.size() > 0) {
                for (var entry : leftover.entrySet()) {
                    player.getLocation().getWorld().dropItemNaturally(player.getLocation(), entry.getValue());
                }
            }
        }

    }
}
