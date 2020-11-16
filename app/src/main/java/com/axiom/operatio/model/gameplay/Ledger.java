package com.axiom.operatio.model.gameplay;

import com.axiom.operatio.model.inventory.Inventory;
import com.axiom.operatio.model.production.Production;

import java.util.Arrays;

public class Ledger {

    public static final int REPORTING_PERIOD = 60;

    private long startingCycle = 0;
    private double periodCashflow = 0;
    private double periodIncome = 0;
    private double periodExpenses = 0;
    private double previousValuation = 0;
    private int[] manufacturedCommodities;
    private double[] soldCommodities;
    private double[] boughtCommodities;

    public Ledger() {
        manufacturedCommodities = new int[Inventory.SKU_COUNT];
        soldCommodities = new double[Inventory.SKU_COUNT];
        boughtCommodities = new double[Inventory.SKU_COUNT];
        Arrays.fill(soldCommodities, 0);
        Arrays.fill(boughtCommodities, 0);
    }

    public void process(Production production) {
        long currentCycle = production.getCurrentCycle();
        if (currentCycle > startingCycle + REPORTING_PERIOD) {
            double valuation = getCapitalization(production);
         //   periodCashflow = 0;
         //   periodIncome = 0;
         //   periodExpenses = 0;
         //   Arrays.fill(soldCommodities, 0);
         //   Arrays.fill(boughtCommodities, 0);
            previousValuation = valuation;
            startingCycle = currentCycle;
        }
    }

    public double getCapitalization(Production production) {
        double assetsValuation = production.getValuation();                     // Активы
        double inventoryValuation = production.getInventory().getValuation();   // Материалы
        double cash = production.getCashBalance();                              // Деньги
        return cash + assetsValuation + inventoryValuation;                     // Капитализация
    }


    public double getPeriodCashflow() {
        return periodCashflow;
    }

    public double getPeriodIncome() {
        return periodIncome;
    }

    public double getPeriodExpenses() {
        return periodExpenses;
    }

    public double getPreviousValuation() {
        return previousValuation;
    }

    public double getCommoditySold(int commodity) {
        return soldCommodities[commodity];
    }

    public double getCommodityBought(int commodity) {
        return boughtCommodities[commodity];
    }

    public void registerCommodityPushed(int commodity, int quantity) {

    }

    public void registerCommodityPolled(int commodity, int quantity) {

    }

    public void registerCommoditySold(int commodity, int quantity, double price) {
        soldCommodities[commodity] += quantity * price;
    }

    public void registerCommodityBought(int commodity, int quantity, double price) {
        boughtCommodities[commodity] += quantity * price;
    }

    public void registerBlockBought(double price) {
        periodCashflow -= price;
        periodExpenses += price;
    }

    public void registerBlockSold(double price) {
        periodCashflow += price;
        periodIncome += price;
    }

    public void registerExpense(int type, double sum) {
        periodCashflow -= sum;
        periodExpenses += sum;
    }

    public void registerIncome(int type, double sum) {
        periodCashflow += sum;
        periodIncome += sum;
    }

}
