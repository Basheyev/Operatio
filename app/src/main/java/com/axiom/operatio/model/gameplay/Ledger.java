package com.axiom.operatio.model.gameplay;

import com.axiom.operatio.model.inventory.Inventory;
import com.axiom.operatio.model.production.Production;

import java.util.Arrays;

// TODO Вытащить производственные показатели
// TODO Вытащить натуральные показатели покупки/продаж
public class Ledger {

    public static final int REPORTING_PERIOD = 60;
    
    public static final int EXPENSE_BLOCK_BOUGHT    = 0x0001;
    public static final int EXPENSE_BLOCK_OPERATION = 0x0002;
    public static final int EXPENSE_MATERIAL_BOUGHT = 0x0003;
    public static final int REVENUE_BLOCK_SOLD      = 0x1001;
    public static final int REVENUE_MATERIAL_SOLD   = 0x1002;

    private long startingCycle = 0;

    private double periodOperRevenue = 0;
    private double periodOperExpenses = 0;
    private double periodOperMargin = 0;

    private double periodMaintenanceCost = 0;
    private double periodInvestExpenses = 0;
    private double periodInvestRevenue = 0;

    private double previousValuation = 0;

    private int[] manufacturedCommodities;
    private double[] soldCommoditiesSum;
    private int[] soldCommoditiesAmount;
    private double[] boughtCommoditiesSum;
    private int[] boughtCommoditiesAmount;

    public Ledger() {
        manufacturedCommodities = new int[Inventory.SKU_COUNT];
        soldCommoditiesSum = new double[Inventory.SKU_COUNT];
        soldCommoditiesAmount = new int[Inventory.SKU_COUNT];
        boughtCommoditiesSum = new double[Inventory.SKU_COUNT];
        boughtCommoditiesAmount = new int[Inventory.SKU_COUNT];
        Arrays.fill(manufacturedCommodities, 0);
        Arrays.fill(soldCommoditiesSum, 0);
        Arrays.fill(soldCommoditiesAmount, 0);
        Arrays.fill(boughtCommoditiesSum, 0);
        Arrays.fill(boughtCommoditiesAmount, 0);
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
        double assetsValuation = production.getAssetsValuation();                     // Активы
        double inventoryValuation = production.getInventory().getValuation();   // Материалы
        double workInProgrssValuation = production.getWorkInProgressValuation();
        double cash = production.getCashBalance();                              // Деньги
        return cash + assetsValuation + inventoryValuation + workInProgrssValuation;                     // Капитализация
    }


    public double getPeriodOperMargin() {
        return periodOperMargin;
    }

    public double getPeriodOperRevenue() {
        return periodOperRevenue;
    }

    public double getPeriodOperExpenses() {
        return periodOperExpenses;
    }

    public double getPeriodMaintenanceCost() {
        return periodMaintenanceCost;
    }

    public double getPeriodInvestExpenses() {
        return periodInvestExpenses;
    }

    public double getPeriodInvestRevenue() {
        return periodInvestRevenue;
    }

    public double getPreviousValuation() {
        return previousValuation;
    }

    public double getCommoditySoldSum(int commodity) {
        return soldCommoditiesSum[commodity];
    }

    public int getCommoditySoldAmount(int commodity) { return soldCommoditiesAmount[commodity]; }

    public double getCommodityBoughtSum(int commodity) {
        return boughtCommoditiesSum[commodity];
    }

    public int getCommodityBoughtAmount(int commodity) { return boughtCommoditiesAmount[commodity]; }

    public void registerCommodityPushed(int commodity, int quantity) {
        // manufacturedCommodities[commodity] += quantity;
        // TODO Отправлено на склад (можно разгонять) надо мерить у источника - машины
    }

    public void registerCommodityPolled(int commodity, int quantity) {

    }

    public void registerCommoditySold(int commodity, int quantity, double price) {
        soldCommoditiesSum[commodity] += quantity * price;
        soldCommoditiesAmount[commodity] += quantity;
    }

    public void registerCommodityBought(int commodity, int quantity, double price) {
        boughtCommoditiesSum[commodity] += quantity * price;
        boughtCommoditiesAmount[commodity] += quantity;
    }


    public void registerExpense(int type, double sum) {
        if (type==EXPENSE_BLOCK_BOUGHT) {
            periodInvestExpenses += sum;
            return;
        }
        periodOperExpenses += sum;
        periodOperMargin = periodOperRevenue - periodOperExpenses;
        if (type==EXPENSE_BLOCK_OPERATION) periodMaintenanceCost += sum;
    }

    public void registerRevenue(int type, double sum) {
        if (type==REVENUE_BLOCK_SOLD) {
            periodInvestRevenue += sum;
            return;
        }
        periodOperRevenue += sum;
        periodOperMargin = periodOperRevenue - periodOperExpenses;
    }

}
