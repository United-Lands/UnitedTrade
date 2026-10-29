package org.unitedlands.trade.integrations.quickshop;

import java.math.BigDecimal;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.unitedlands.UnitedLib;
import org.unitedlands.unitedlands.managers.UnitedLandsDataManager;
import org.unitedlands.unitedlands.managers.UnitedLandsEconomyManager;

import com.ghostchu.quickshop.api.event.economy.ShopSuccessPurchaseEvent;

public class QuickShopListener implements Listener {

    @EventHandler
    private void onPurchase(ShopSuccessPurchaseEvent event) {

        var customer = UnitedLandsDataManager.instance().getCitizen(event.getPurchaser().getUniqueId());
        if (customer == null)
            return;
        var shopOwner = UnitedLandsDataManager.instance().getCitizen(event.getShop().getOwner().getUniqueId());
        if (shopOwner == null)
            return;

        // If a shop buys, see if we have to handle taxes
        if (event.getShop().isBuying()) {

            // Add a positive virtual bank record for the customer
            UnitedLandsEconomyManager.instance().addVirtualBankRecord(
                    customer.getUuid(),
                    new BigDecimal(event.getBalance()),
                    "Sold " + event.getAmount() + "x "
                            + UnitedLib.getInstance().getItemFactory().getDisplayName(event.getShop().getItem()));

            // Add a negative virtual bank record for the shop owner
            UnitedLandsEconomyManager.instance().addVirtualBankRecord(
                    shopOwner.getUuid(),
                    new BigDecimal(event.getBalance() * -1),
                    "Shop bought " + event.getAmount() + "x "
                            + UnitedLib.getInstance().getItemFactory().getDisplayName(event.getShop().getItem()));

            if (shopOwner.hasSettlement()) {

                var percent = shopOwner.getSettlement().getTax();
                var settlementShare = event.getBalance() * percent;

                // Reroactively deduct the income tax from the account
                UnitedLandsEconomyManager.instance().withdraw(
                        shopOwner.getUuid(),
                        settlementShare,
                        "Shop income tax");
                // Pay the income tax to the settlement
                UnitedLandsEconomyManager.instance().depositAndTax(
                        shopOwner.getSettlement(),
                        settlementShare,
                        "Shop income tax");
            }

        } else {

            // Add a virtual bank record for the customer
            UnitedLandsEconomyManager.instance().addVirtualBankRecord(
                    customer.getUuid(),
                    new BigDecimal(event.getBalance() * -1),
                    "Bought " + event.getAmount() + "x "
                            + UnitedLib.getInstance().getItemFactory().getDisplayName(event.getShop().getItem()));

            // Add a virtual bank record for the shop owner
            UnitedLandsEconomyManager.instance().addVirtualBankRecord(
                    shopOwner.getUuid(),
                    new BigDecimal(event.getBalance()),
                    "Shop sold " + event.getAmount() + "x "
                            + UnitedLib.getInstance().getItemFactory().getDisplayName(event.getShop().getItem()));

        }

    }

}
