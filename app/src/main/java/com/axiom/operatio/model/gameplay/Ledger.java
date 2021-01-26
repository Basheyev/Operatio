package com.axiom.operatio.model.gameplay;

import com.axiom.operatio.model.inventory.Inventory;
import com.axiom.operatio.model.production.Production;

import java.util.Arrays;

/**
 * Главный журнал регистрации всех производственных и финансовых событий
 */
public class Ledger {
    //---------------------------------------------------------------------------------------------
    public static final int REPORTING_PERIOD = 60;               // Длительность периода в циклах
    //---------------------------------------------------------------------------------------------
    public static final int EXPENSE_BLOCK_BOUGHT    = 0x0001;    // Расходы на покупку блока
    public static final int EXPENSE_BLOCK_OPERATION = 0x0002;    // Расходы на операцию блока
    public static final int EXPENSE_MATERIAL_BOUGHT = 0x0003;    // Расходы на покупку материала
    public static final int REVENUE_BLOCK_SOLD      = 0x1001;    // Выручка от продажи блока
    public static final int REVENUE_MATERIAL_SOLD   = 0x1002;    // Выручка от продажи материала
    //---------------------------------------------------------------------------------------------
    private Production production;
    private long startingCycle = 0;
    //---------------------------------------------------------------------------------------------
    private double periodOperRevenue = 0;                        // Операционная выручка за период
    private double periodOperExpenses = 0;                       // Операционные расходы за период
    private double periodOperMargin = 0;                         // Операционная маржа за период
    private double periodMaintenanceCost = 0;                    // Расходы на операции за период
    private double periodAssetsBought = 0;                       // Приобретено активов на сумму
    private double periodAssetsSold = 0;                         // Продано активов на сумму
    private double currentCashBalance = 0;                       // Текущий остаток денег
    //---------------------------------------------------------------------------------------------
    private int[] manufacturedCommodities;                       // Объем произведенных материалов
    private double[] soldCommoditiesSum;                         // Объем проданных материалов
    private int[] soldCommoditiesAmount;                         // Количество проданных материалов
    private double[] boughtCommoditiesSum;                       // Объем приобретенных материалов
    private int[] boughtCommoditiesAmount;                       // Количество приобретенных материалов
    //---------------------------------------------------------------------------------------------


    public Ledger(Production production) {
        this.production = production;
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


    public void process() {
        currentCashBalance = production.getCashBalance();

        long currentCycle = production.getCurrentCycle();
        if (currentCycle > startingCycle + REPORTING_PERIOD) {
            // TODO Report
            startingCycle = currentCycle;
        }

    }


    public double getCapitalization() {
        double assetsValuation = production.getAssetsValuation();                  // Активы
        double inventoryValuation = production.getInventory().getValuation();      // Материалы на складе
        double workInProgressValuation = production.getWorkInProgressValuation();  // Материалы в цеху
        double cash = production.getCashBalance();                                 // Деньги
        return cash + assetsValuation + inventoryValuation + workInProgressValuation;  // Капитализация
    }

    public double getCurrentCashBalance() {
        return currentCashBalance;
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

    public double getCommoditySoldSum(int commodity) {
        return soldCommoditiesSum[commodity];
    }

    public double getPeriodAssetsBought() {
        return periodAssetsBought;
    }

    public double getPeriodAssetsSold() {
        return periodAssetsSold;
    }

    public int getCommoditySoldAmount(int commodity) { return soldCommoditiesAmount[commodity]; }

    public double getCommodityBoughtSum(int commodity) {
        return boughtCommoditiesSum[commodity];
    }

    public int getCommodityBoughtAmount(int commodity) { return boughtCommoditiesAmount[commodity]; }

    public int getCommodityManufacturedAmount(int commodity) {
        return manufacturedCommodities[commodity];
    }

    //--------------------------------------------------------------------------------------------

    public void registerExpense(int type, double sum) {
        if (type==EXPENSE_BLOCK_BOUGHT) {
            periodAssetsBought += sum;
            return;
        }
        if (type==EXPENSE_BLOCK_OPERATION) periodMaintenanceCost += sum;
        periodOperExpenses += sum;
        periodOperMargin = periodOperRevenue - periodOperExpenses;

    }

    public void registerRevenue(int type, double sum) {
        if (type==REVENUE_BLOCK_SOLD) {
            periodAssetsSold += sum;
            return;
        }
        periodOperRevenue += sum;
        periodOperMargin = periodOperRevenue - periodOperExpenses;
    }

    public void registerCommodityManufactured(int commodity, int quantity) {
        manufacturedCommodities[commodity] += quantity;
    }

    public void registerCommoditySold(int commodity, int quantity, double price) {
        soldCommoditiesSum[commodity] += quantity * price;
        soldCommoditiesAmount[commodity] += quantity;
    }

    public void registerCommodityBought(int commodity, int quantity, double price) {
        boughtCommoditiesSum[commodity] += quantity * price;
        boughtCommoditiesAmount[commodity] += quantity;
    }

}
