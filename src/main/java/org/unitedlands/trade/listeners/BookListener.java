package org.unitedlands.trade.listeners;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.integrations.floodgate.FloodgateAPIIntegration;
import org.unitedlands.trade.utils.TradeOrderBookUtil;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;

public class BookListener implements Listener {

    private final UnitedTrade plugin;

    public BookListener(UnitedTrade plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBookInteract(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK))
            return;

        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.TRAPPED_CHEST) {
            return;
        }

        var book = event.getItem();
        if (book == null || book.getType() != Material.WRITTEN_BOOK)
            return;

        if (!TradeOrderBookUtil.isTradeOrderBook(book)) {
            return;
        }
        event.setCancelled(true);

        var player = event.getPlayer();

        // Show different UIs to Java and Bedrock players if floodgate is present
        if (!plugin.useFloodgate()) {
            handleJavaDialogue(player, book);
        } else {
            var floodgate = new FloodgateAPIIntegration(plugin);
            if (!floodgate.isBedrockPlayer(player))
                handleJavaDialogue(player, book);
            else {
                handleFloodgatePanel(floodgate, player, book);
            }
        }
    }

    private void handleFloodgatePanel(FloodgateAPIIntegration floodgate, Player player, ItemStack book) {
        floodgate.sendBookPanel(player, book);
    }

    private void handleJavaDialogue(Player player, ItemStack book) {

        List<DialogBody> dialogBody = TradeOrderBookUtil.getJavaPanelContent(book);

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Component.text("Trade Order"))
                        .body(dialogBody)
                        .build())
                .type(DialogType.notice()));

        player.showDialog(dialog);
    }

}
