package org.unitedlands.trade.commands.admin.tradepoints.handlers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.unitedlands.classes.BaseCommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.classes.TradePoint;
import org.unitedlands.utils.Logger;
import org.unitedlands.utils.United;

public class AdminTradePointsSetHandler extends BaseCommandHandler<UnitedTrade> {

    public AdminTradePointsSetHandler(UnitedTrade plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);
    }

    private List<String> propertyList = List.of("currentOrderNo", "enabled", "name", "ownerName",
            "pickupCooldown", "applyContractPenalties", "contractPenalty", "requiredPermissions",
            "blacklistedPermissions", "replaceBookOnRestock", "customRestockFrequency", "minReputation",
            "reputationOnComplete", "reputationOnFail");
    private List<String> multiWordPropertyList = List.of("requiredPermissionsError", "blacklistedPermissionsError");

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        if (args.length == 1) {
            var fullList = new ArrayList<String>(propertyList);
            fullList.addAll(multiWordPropertyList);
            return fullList;
        }
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {

        if (args.length < 2) {
            United.messenger().send(sender, "usage.tradepoint-set");
            return;
        }

        Player player = (Player) sender;
        var tradePoint = plugin.getTradePointManager().getClosestTradepointInRadius(player.getLocation(), 2);
        if (tradePoint == null) {
            United.messenger().send(sender, "errors.no-close-tradepoint");
            return;
        }

        if (multiWordPropertyList.contains(args[0])) {
            setField(player, tradePoint, args[0], String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
        } else {
            setField(player, tradePoint, args[0], args[1]);
        }

        plugin.getTradePointManager().saveTradePoint(tradePoint, sender);
    }

    private void setField(Player player, TradePoint tradePoint, String fieldName, String arg) {
        try {
            Field field = TradePoint.class.getDeclaredField(fieldName);
            field.setAccessible(true);

            Class<?> fieldType = field.getType();

            Object value;
            if (fieldType == int.class || fieldType == Integer.class) {
                value = Integer.parseInt(arg);
            } else if (fieldType == double.class || fieldType == Double.class) {
                value = Double.parseDouble(arg);
            } else if (fieldType == long.class || fieldType == Long.class) {
                value = Long.parseLong(arg);
            } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                value = Boolean.parseBoolean(arg);
            } else {
                if (arg.equals("NULL"))
                    value = null;
                else
                    value = arg;
            }

            field.set(tradePoint, value);

        } catch (NoSuchFieldException e) {
            United.logger().error("Field " + fieldName + " does not exist.");
        } catch (IllegalAccessException e) {
            United.logger().error("Unable to access field " + fieldName + ".");
        } catch (NumberFormatException e) {
            United.logger().error("Invalid value for field " + fieldName + ".");
        }
    }

}
