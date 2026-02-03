package org.unitedlands.trade;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.classes.ConfigFile;
import org.unitedlands.trade.classes.MessageProvider;
import org.unitedlands.trade.commands.AdminCommands;
import org.unitedlands.trade.commands.CheckOrderCommand;
import org.unitedlands.trade.integrations.interfaces.DefaultEconomyProvider;
import org.unitedlands.trade.integrations.interfaces.IEconomyProvider;
import org.unitedlands.trade.integrations.interfaces.VaultEcononyProvider;
import org.unitedlands.trade.listeners.DropoffPointListener;
import org.unitedlands.trade.listeners.InventoryListener;
import org.unitedlands.trade.listeners.LecternListener;
import org.unitedlands.trade.listeners.ServerListener;
import org.unitedlands.trade.managers.DropoffPointManager;
import org.unitedlands.trade.managers.OrderTemplateManager;
import org.unitedlands.trade.managers.OrderTracker;
import org.unitedlands.trade.managers.ShopPointManager;
import org.unitedlands.trade.managers.ShopTemplateManager;
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
    private ShopPointManager shopPointManager;
    private ShopTemplateManager shopTemplateManager;
    private OrderTemplateManager orderTemplateManager;
    private OrderTracker orderTracker;

    private IEconomyProvider economyProvider;

    private ConfigFile priceConfig;

    private boolean useFloodgate;

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
        priceConfig = new ConfigFile(this, "prices.yml");
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new DropoffPointListener(this, messageProvider), this);
        getServer().getPluginManager().registerEvents(new LecternListener(this, messageProvider), this);
        getServer().getPluginManager().registerEvents(new ServerListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this, messageProvider), this);
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
        shopPointManager = new ShopPointManager(this, messageProvider);
        orderTemplateManager = new OrderTemplateManager(this);
        shopTemplateManager = new ShopTemplateManager(this);
        orderTracker = new OrderTracker(this, messageProvider);
    }

    private void loadWrappers() {
        Plugin towny = Bukkit.getPluginManager().getPlugin("Towny");
        if (towny != null && towny.isEnabled()) {
            Logger.log("Towny found, enabling wrapper.", "UnitedTrade");
        }
    }

    private void loadIntegrations() {
        Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
        if (vault != null && vault.isEnabled()) {
            Logger.log("Enabling Vault integrations.", "UnitedTrade");
            economyProvider = new VaultEcononyProvider(this, messageProvider);
        } else {
            economyProvider = new DefaultEconomyProvider(this, messageProvider);
        }
        Plugin floodgate = Bukkit.getPluginManager().getPlugin("floodgate");
        if (floodgate != null && vault.isEnabled()) {
            Logger.log("Enabling floodgate integrations.", "UnitedTrade");
            useFloodgate = true;
        } 

    }

    public MessageProvider getMessageProvider() {
        return messageProvider;
    }

    public TradePointManager getTradePointManager() {
        return tradePointManager;
    }

    public DropoffPointManager getDropoffPointManager() {
        return dropoffPointManager;
    }

    public ShopPointManager getShopPointManager() {
        return shopPointManager;
    }

    public ShopTemplateManager getShopTemplateManager() {
        return shopTemplateManager;
    }

    public OrderTemplateManager getOrderTemplateManager() {
        return orderTemplateManager;
    }

    public OrderTracker getOrderTracker() {
        return orderTracker;
    }

    public ConfigFile getPriceConfig() {
        return priceConfig;
    }

    public IEconomyProvider getEconomyProvider() {
        return economyProvider;
    }

    public boolean useFloodgate() {
        return useFloodgate;
    }

    @Override
    public void onDisable() {
        orderTracker.stopTracking();
    }

}
