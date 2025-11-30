package org.unitedlands.trade.commands.admin.tradepoints;

import org.unitedlands.classes.BaseSubcommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.commands.admin.tradepoints.handlers.AdminTradePointsAddTemplateHandler;
import org.unitedlands.trade.commands.admin.tradepoints.handlers.AdminTradePointsCreateHandler;
import org.unitedlands.trade.commands.admin.tradepoints.handlers.AdminTradePointsInfoHandler;
import org.unitedlands.trade.commands.admin.tradepoints.handlers.AdminTradePointsRemoveHandler;
import org.unitedlands.trade.commands.admin.tradepoints.handlers.AdminTradePointsRemoveTemplateHandler;
import org.unitedlands.trade.commands.admin.tradepoints.handlers.AdminTradePointsRestockHandler;
import org.unitedlands.trade.commands.admin.tradepoints.handlers.AdminTradePointsSetHandler;

public class AdminTradePointsSubcommandHandler extends BaseSubcommandHandler<UnitedTrade> {

    public AdminTradePointsSubcommandHandler(UnitedTrade plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);
    }

    @Override
    protected void registerSubHandlers() {
        subHandlers.put("info", new AdminTradePointsInfoHandler(plugin, messageProvider));
        subHandlers.put("set", new AdminTradePointsSetHandler(plugin, messageProvider));
        subHandlers.put("create", new AdminTradePointsCreateHandler(plugin, messageProvider));
        subHandlers.put("remove", new AdminTradePointsRemoveHandler(plugin, messageProvider));
        subHandlers.put("restock", new AdminTradePointsRestockHandler(plugin, messageProvider));
        subHandlers.put("addtemplate", new AdminTradePointsAddTemplateHandler(plugin, messageProvider));
        subHandlers.put("removetemplate", new AdminTradePointsRemoveTemplateHandler(plugin, messageProvider));
    }

}
