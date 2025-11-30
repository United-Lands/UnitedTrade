package org.unitedlands.trade.commands.admin.dropoffpoint;

import org.unitedlands.classes.BaseSubcommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.trade.UnitedTrade;
import org.unitedlands.trade.commands.admin.dropoffpoint.handlers.AdminDropoffPointsCreateHandler;
import org.unitedlands.trade.commands.admin.dropoffpoint.handlers.AdminDropoffPointsRemoveHandler;

public class AdminDropoffPointsSubcommandHandler extends BaseSubcommandHandler<UnitedTrade> {

    public AdminDropoffPointsSubcommandHandler(UnitedTrade plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);
    }

    @Override
    protected void registerSubHandlers() {
        subHandlers.put("create", new AdminDropoffPointsCreateHandler(plugin, messageProvider));
        subHandlers.put("remove", new AdminDropoffPointsRemoveHandler(plugin, messageProvider));
    }

}
