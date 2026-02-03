package org.unitedlands.trade.commands.admin.shoppoint;

import org.unitedlands.classes.BaseSubcommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.commands.admin.shoppoint.handlers.AdminShopPointsCreateHandler;
import org.unitedlands.trade.commands.admin.shoppoint.handlers.AdminShopPointsPointsInfoHandler;
import org.unitedlands.trade.commands.admin.shoppoint.handlers.AdminShopPointsRemoveHandler;
import org.unitedlands.trade.commands.admin.shoppoint.handlers.AdminShopPointsSetHandler;

public class AdminShopPointsSubcommandHandler extends BaseSubcommandHandler<UnitedTrade> {

    public AdminShopPointsSubcommandHandler(UnitedTrade plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);
    }

    @Override
    protected void registerSubHandlers() {
        subHandlers.put("create", new AdminShopPointsCreateHandler(plugin, messageProvider));
        subHandlers.put("remove", new AdminShopPointsRemoveHandler(plugin, messageProvider));
        subHandlers.put("set", new AdminShopPointsSetHandler(plugin, messageProvider));
        subHandlers.put("info", new AdminShopPointsPointsInfoHandler(plugin, messageProvider));
    }

}
