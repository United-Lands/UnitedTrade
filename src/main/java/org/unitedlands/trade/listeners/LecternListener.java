package org.unitedlands.trade.listeners;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.classes.MessageProvider;
import org.unitedlands.trade.classes.TradePoint;
import org.unitedlands.trade.integrations.floodgate.FloodgateAPIIntegration;
import org.unitedlands.trade.utils.TradeOrderBookUtil;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.event.player.PlayerInsertLecternBookEvent;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;

public class LecternListener implements Listener {

    private final UnitedTrade plugin;
    @SuppressWarnings("unused")
    private final MessageProvider messageProvider;

    public LecternListener(UnitedTrade plugin, MessageProvider messageProvider) {
        this.plugin = plugin;
        this.messageProvider = messageProvider;
    }

    @EventHandler
    public void onLecternInteract(PlayerInteractEvent event) {

        if (event.getClickedBlock() == null)
            return;

        if (event.getClickedBlock().getType() != Material.LECTERN)
            return;

        var block = event.getClickedBlock();
        var tradePoint = plugin.getTradePointManager().getTradePoint(block);
        if (tradePoint == null)
            return;

        event.setCancelled(true);

        var player = event.getPlayer();

        if (!plugin.getOrderTracker().hasPickupRequirements(player, tradePoint))
            return;

        var book = tradePoint.getBook();
        if (book == null) {
            return;
        }

        // Show different UIs to Java and Bedrock players if floodgate is present
        if (!plugin.useFloodgate()) {
            handleJavaDialogue(player, tradePoint, book);
        } else {
            var floodgate = new FloodgateAPIIntegration(plugin);
            if (!floodgate.isBedrockPlayer(player))
                handleJavaDialogue(player, tradePoint, book);
            else {
                handleFloodgatePanel(floodgate, player, tradePoint, book);
            }
        }

    }

    private void handleJavaDialogue(Player player, TradePoint tradePoint, ItemStack book) {

        List<DialogBody> dialogBody = TradeOrderBookUtil.getJavaPanelContent(book);

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Component.text("Trade Order"))
                        .body(dialogBody)
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(Component.text("Accept trade order"))
                                .action(
                                        DialogAction.customClick(
                                                (view, audience) -> {
                                                    if (plugin.getOrderTracker().acceptTradeOrder(player, tradePoint,
                                                            book)) {
                                                        tradePoint.removeBook();
                                                        var leftover = player.getInventory().addItem(book);
                                                        if (leftover.size() > 0) {
                                                            for (var entry : leftover.entrySet()) {
                                                                player.getLocation().getWorld().dropItemNaturally(
                                                                        player.getLocation(), entry.getValue());
                                                            }
                                                        }
                                                    }
                                                },
                                                ClickCallback.Options.builder().build()))
                                .build(),
                        ActionButton.builder(Component.text("Cancel")).build())));

        player.showDialog(dialog);
    }

    private void handleFloodgatePanel(FloodgateAPIIntegration floodgate, Player player, TradePoint tradePoint,
            ItemStack book) {
        floodgate.sendTradePointOrderPanel(player, tradePoint, book);
    }

    @EventHandler
    public void onBookPlace(PlayerInsertLecternBookEvent event) {
        var block = event.getLectern().getBlock();
        var tradePoint = plugin.getTradePointManager().getTradePoint(block);
        if (tradePoint == null)
            return;

        event.setCancelled(true);
    }

}
