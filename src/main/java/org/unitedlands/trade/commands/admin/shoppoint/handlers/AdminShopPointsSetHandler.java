package org.unitedlands.trade.commands.admin.shoppoint.handlers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.unitedlands.classes.BaseCommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.classes.ShopPoint;
import org.unitedlands.utils.Logger;
import org.unitedlands.utils.United;

public class AdminShopPointsSetHandler extends BaseCommandHandler<UnitedTrade> {

    public AdminShopPointsSetHandler(UnitedTrade plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);
    }

    private List<String> propertyList = Arrays.asList("template", "refillFrequency", "useGlobalInventory", "minReputation");

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        if (args.length == 1)
            return propertyList;
        if (args.length == 2 && args[0].equals("template"))
            return plugin.getShopTemplateManager().getShopTemplateKeys().stream().collect(Collectors.toList());
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {

        if (args.length < 2) {
            United.messenger().send(sender, "usage.tradepoint-set");
            return;
        }

        var player = (Player)sender;
        ShopPoint shopPoint = plugin.getShopPointManager().getClosestShopPointInRadius(player.getLocation(),
                2);
        if (shopPoint == null) {
            United.messenger().send(sender, "errors.no-close-shoppoint");
            return;
        }

        setField(player, shopPoint, args[0], args[1]);
        if (args[0].equals("template"))
            shopPoint.spawnChest();

        plugin.getShopPointManager().saveShopPoint(shopPoint, sender);
    }

    private void setField(Player player, ShopPoint shopPoint, String fieldName, String arg) {
        try {
            Field field = ShopPoint.class.getDeclaredField(fieldName);
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

            field.set(shopPoint, value);

        } catch (NoSuchFieldException e) {
            United.logger().error("Field " + fieldName + " does not exist.");
        } catch (IllegalAccessException e) {
            United.logger().error("Unable to access field " + fieldName + ".");
        } catch (NumberFormatException e) {
            United.logger().error("Invalid value for field " + fieldName + ".");
        }
    }

}
