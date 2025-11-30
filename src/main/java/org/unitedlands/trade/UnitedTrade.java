package org.unitedlands.trade;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.classes.ConfigFile;
import org.unitedlands.trade.classes.MessageProvider;
import org.unitedlands.trade.commands.AdminCommands;
import org.unitedlands.trade.commands.CheckOrderCommand;
import org.unitedlands.trade.listeners.DropoffPointListener;
import org.unitedlands.trade.listeners.LecternListener;
import org.unitedlands.trade.listeners.ServerListener;
import org.unitedlands.trade.managers.DropoffPointManager;
import org.unitedlands.trade.managers.OrderTemplateManager;
import org.unitedlands.trade.managers.OrderTracker;
import org.unitedlands.trade.managers.TradePointManager;
import org.unitedlands.utils.Logger;

public class UnitedTrade extends JavaPlugin {

    private static UnitedTrade instance;

    public static UnitedTrade getInstance() {
        return instance;
    }

    private MessageProvider messageProvider;
    private TradePointManager tradePointManager;
    private DropoffPointManager dropoffPointManager;
    private OrderTemplateManager orderTemplateManager;
    private OrderTracker orderTracker;

    private ConfigFile materialConfig;

    @Override
    public void onEnable() {

        instance = this;

        saveDefaultConfig();

        messageProvider = new MessageProvider(getConfig());

        loadConfigs();
        loadManagers();
        loadWrappers();

        registerEvents();
        registerCommands();

        loadIntegrations();

        Logger.log("UnitedTrade initialized.", "UnitedTrade");
    }

    private void loadConfigs() {
        materialConfig = new ConfigFile(this, "materials.yml");
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new DropoffPointListener(this, messageProvider), this);
        getServer().getPluginManager().registerEvents(new LecternListener(this, messageProvider), this);
        getServer().getPluginManager().registerEvents(new ServerListener(this), this);
    }

    private void registerCommands() {
        var adminCommands = new AdminCommands(this, messageProvider);
        getCommand("unitedtradeadmin").setExecutor(adminCommands);
        getCommand("unitedtradeadmin").setTabCompleter(adminCommands);

        var checkOrderCommand = new CheckOrderCommand(this, messageProvider);
        getCommand("checkorder").setExecutor(checkOrderCommand);
    }

    private void loadManagers() {
        tradePointManager = new TradePointManager(this, messageProvider);
        dropoffPointManager = new DropoffPointManager(this, messageProvider);
        orderTemplateManager = new OrderTemplateManager(this);
        orderTracker = new OrderTracker(this, messageProvider);
    }

    private void loadWrappers() {
        Plugin towny = Bukkit.getPluginManager().getPlugin("Towny");
        if (towny != null && towny.isEnabled()) {
            Logger.log("Towny found, enabling wrapper.", "UnitedTrade");
        }
    }

    private void loadIntegrations() {
        Plugin towny = Bukkit.getPluginManager().getPlugin("Towny");
        if (towny != null && towny.isEnabled()) {
            Logger.log("Enabling Towny integrations.", "UnitedTrade");
        }
    }

    public MessageProvider getMessageProvider() {
        return messageProvider;
    }

    public OrderTemplateManager getOrderTemplateManager() {
        return orderTemplateManager;
    }

    public TradePointManager getTradePointManager() {
        return tradePointManager;
    }

    public DropoffPointManager getDropoffPointManager() {
        return dropoffPointManager;
    }
    
    public OrderTracker getOrderTracker() {
        return orderTracker;
    }

    public ConfigFile getMaterialConfig() {
        return materialConfig;
    }

    @Override
    public void onDisable() {
        orderTracker.stopTracking();
    }

}
