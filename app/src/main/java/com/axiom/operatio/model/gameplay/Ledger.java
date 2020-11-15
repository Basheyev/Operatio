package com.axiom.operatio.model.gameplay;

import android.util.Log;

import com.axiom.operatio.model.production.Production;

public class Ledger {

    public static final int REPORTING_PERIOD = 60;

    private long startingCycle = 0;
    private double startingAssetsValue = 0;
    private double startingInventoryValue = 0;
    private double startingCash = 0;
    private double valuation;

    public void process(Production production) {
        long currentCycle = production.getCurrentCycle();

        if (currentCycle > startingCycle + REPORTING_PERIOD) {
            // TODO Evaluate indicators
            double assetsValuation = production.getValuation();
            double inventoryValuation = production.getInventory().getValuation();
            double cash = production.getCashBalance();
            double newValuation = cash + assetsValuation + inventoryValuation;

            Log.i("CAPITAL CHANGE DAY=" + (currentCycle / REPORTING_PERIOD), "$" + (long)(newValuation - valuation));

            startingAssetsValue = assetsValuation;
            startingInventoryValue = inventoryValuation;
            startingCash = cash;
            valuation = newValuation;
            startingCycle = currentCycle;
        }

    }



    public void registerCommodityPushed(int commodity, int quantity) {
     //   Log.i("COMMODITY PUSHED", Material.getMaterial(commodity).getName() + " +" + quantity);
        // TODO log commodity stored event
    }

    public void registerCommodityPolled(int commodity, int quantity) {
    //    Log.i("COMMODITY POLLED", Material.getMaterial(commodity).getName() + " -" + quantity);
        // TODO Log commodity shipped event
    }

    public void registerCommoditySold(int commodity, int quantity, double price) {
     //   Log.i("COMMODITY SOLD", Material.getMaterial(commodity).getName() + " -" + quantity + " +$" + price);
        // TODO Log commodity sold
    }

    public void registerCommodityBought(int commodity, int quantity, double price) {
     //   Log.i("COMMODITY BOUGHT", Material.getMaterial(commodity).getName() + " +" + quantity + " -$" + price);
        // TODO Log commodity bought
    }

    public void registerBlockBought(double price) {
    //    Log.i("BLOCK BOUGHT", " -$" + price);
        // TODO Log block bought
    }

    public void registerBlockSold(double price) {
    //    Log.i("BLOCK SOLD", " +$" + price);
        // TODO Log block sold
    }

    public void registerExpense(int type, double sum) {
     //   Log.i("EXPENSE", " -$" + sum);
        // TODO Log expense
    }

    public void registerIncome(int type, double sum) {
     //   Log.i("INCOME", " +$" + sum);
        // TODO Log income
    }

}
