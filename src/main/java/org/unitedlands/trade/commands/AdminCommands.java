package org.unitedlands.trade.commands;

import org.unitedlands.classes.BaseCommandExecutor;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.commands.admin.AdminCreateTestOrderCommand;
import org.unitedlands.trade.commands.admin.AdminReloadCommand;
import org.unitedlands.trade.commands.admin.AdminValidateTestOrderCommand;
import org.unitedlands.trade.commands.admin.dropoffpoint.AdminDropoffPointsSubcommandHandler;
import org.unitedlands.trade.commands.admin.tradepoints.AdminTradePointsSubcommandHandler;

public class AdminCommands extends BaseCommandExecutor<UnitedTrade> {

    public AdminCommands(UnitedTrade plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);
    }

    @Override
    protected void registerHandlers() {
        handlers.put("reload", new AdminReloadCommand(plugin, messageProvider));
        handlers.put("createtestorder", new AdminCreateTestOrderCommand(plugin, messageProvider));
        handlers.put("validatetestorder", new AdminValidateTestOrderCommand(plugin, messageProvider));
        handlers.put("tradepoints", new AdminTradePointsSubcommandHandler(plugin, messageProvider));
        handlers.put("dropoffpoints", new AdminDropoffPointsSubcommandHandler(plugin, messageProvider));
    }

}
