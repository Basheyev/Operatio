package com.axiom.operatio.model.gameplay;

import com.axiom.operatio.model.inventory.Inventory;
import com.axiom.operatio.model.production.Production;

import java.util.Arrays;

public class Ledger {

    public static final int REPORTING_PERIOD = 60;
    public static final int EXPENSE_BLOCK_BOUGHT    = 0x0001;
    public static final int EXPENSE_BLOCK_OPERATION = 0x0002;
    public static final int EXPENSE_MATERIAL_BOUGHT = 0x0003;
    public static final int INCOME_BLOCK_SOLD       = 0x1001;
    public static final int INCOME_MATERIAL_SOLD    = 0x1002;

    private long startingCycle = 0;
    private double periodCashflow = 0;
    private double periodIncome = 0;
    private double periodExpenses = 0;
    private double periodOperationalExpenses = 0;
    private double periodInvestExpenses = 0;
    private double periodInvestIncome = 0;
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

    public double getPeriodOperationalExpenses() {
        return periodOperationalExpenses;
    }

    public double getPeriodInvestExpenses() {
        return periodInvestExpenses;
    }

    public double getPeriodInvestIncome() {
        return periodInvestIncome;
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


    public void registerExpense(int type, double sum) {
        periodCashflow -= sum;
        periodExpenses += sum;
        if (type==EXPENSE_BLOCK_BOUGHT) {
            periodInvestExpenses += sum;
        }
        if (type==EXPENSE_BLOCK_OPERATION) {
            periodOperationalExpenses += sum;
        }
    }

    public void registerIncome(int type, double sum) {
        periodCashflow += sum;
        periodIncome += sum;
        if (type==INCOME_BLOCK_SOLD) {
            periodInvestIncome += sum;
        }
    }

}
