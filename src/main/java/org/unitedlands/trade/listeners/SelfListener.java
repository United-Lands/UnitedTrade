package org.unitedlands.trade.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.unitedlands.trade.classes.events.TradePointValidationEvent;
import org.unitedlands.utils.United;

import net.kyori.adventure.text.Component;

public class SelfListener implements Listener {

    @EventHandler
    public void onTradePointValidation(TradePointValidationEvent event) {

        var tradePoint = event.getTradePoint();
        var player = event.getPlayer();

        if (tradePoint.isPlayerOnPickupCooldown(player.getUniqueId())) {
            var cooldownMillis = tradePoint.getPickupCooldown() * 1000;
            var remaining = tradePoint.getPlayerPickupCooldownRemaining(player.getUniqueId());

            event.setValid(false);
            event.getMessages().add(
                    United.miniMessage().get().deserialize(
                        United.messenger().get("tradepoint.on-cooldown", 
                            United.formatter().formatDuration(cooldownMillis),
                            United.formatter().formatDuration(remaining)))
                    );
        }

        if (tradePoint.getRequiredPermissions() != null) {
            var permissionsArray = tradePoint.getRequiredPermissions().split(",");
            boolean playerHasAllRequiredPermissions = true;
            for (var permString : permissionsArray) {
                var perm = permString.trim();
                if (!player.hasPermission(perm))
                    playerHasAllRequiredPermissions = false;
            }

            if (!playerHasAllRequiredPermissions) {
                event.setValid(false);
                if (tradePoint.getRequiredPermissionsError() != null) {
                    event.getMessages().add(
                            Component.text("<red>" + tradePoint.getRequiredPermissionsError() + "</red>"));
                } else {
                    event.getMessages().add(
                        United.miniMessage().get().deserialize(
                            United.messenger().get("tradepoint.permission-error"))
                        );
                }
            }
        }

        if (tradePoint.getBlacklistedPermissions() != null) {
            var permissionsArray = tradePoint.getBlacklistedPermissions().split(",");
            boolean playerHasBlacklistedPermission = false;
            for (var permString : permissionsArray) {
                var perm = permString.trim();
                if (player.hasPermission(perm))
                    playerHasBlacklistedPermission = true;
            }

            if (playerHasBlacklistedPermission) {
                event.setValid(false);
                if (tradePoint.getBlacklistedPermissionsError() != null) {
                    event.getMessages().add(
                            Component.text("<red>" + tradePoint.getBlacklistedPermissionsError() + "</red>"));
                } else {
                    event.getMessages().add(
                        United.miniMessage().get().deserialize(
                            United.messenger().get("tradepoint.permission-error"))
                        );
                }
            }
        }
    }

}
