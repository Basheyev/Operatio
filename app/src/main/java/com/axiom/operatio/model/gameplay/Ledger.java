package com.axiom.operatio.model.gameplay;

import com.axiom.operatio.model.inventory.Inventory;
import com.axiom.operatio.model.production.Production;

import java.util.Arrays;

/**
 * Главный журнал регистрации всех производственных и финансовых событий
 */
public class Ledger {
    //---------------------------------------------------------------------------------------------
    public static final int OPERATIONAL_DAY_CYCLES = 60;         // Длительность периода в циклах
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
    private double totalRevenue = 0;                             // Операционная выручка итого
    private double totalExpenses = 0;                            // Операционные расходы итого
    private double totalMargin = 0;                              // Операционная маржа итого
    private double totalMaintenanceCost = 0;                     // Расходы на операции итого
    private double totalAssetsBought = 0;                        // Приобретено активов на сумму
    private double totalAssetsSold = 0;                          // Продано активов на сумму
    private double currentCashBalance = 0;                       // Текущий остаток денег
    //---------------------------------------------------------------------------------------------
    private double periodRevenue = 0;                            // Операционная выручка за период
    private double periodExpenses = 0;                           // Операционные расходы за период
    private double periodMargin = 0;                             // Операционная маржа за период
    private double periodMaintenanceCost = 0;                    // Расходы на операции за период
    private double periodAssetsBought = 0;                       // Приобретено активов на сумму
    private double periodAssetsSold = 0;                         // Продано активов на сумму
    //---------------------------------------------------------------------------------------------
    private int[] productivity;                                  // Производительность за период
    private int[] manufacturedByPeriod;                          // Объем произведенных материалов за период
    private int[] manufacturedTotal;                             // Объем произведенных материалов всего
    private double[] soldCommoditiesSum;                         // Объем проданных материалов
    private int[] soldCommoditiesAmount;                         // Количество проданных материалов
    private double[] boughtCommoditiesSum;                       // Объем приобретенных материалов
    private int[] boughtCommoditiesAmount;                       // Количество приобретенных материалов
    //---------------------------------------------------------------------------------------------


    public Ledger(Production production) {
        this.production = production;
        manufacturedTotal = new int[Inventory.SKU_COUNT];
        manufacturedByPeriod = new int[Inventory.SKU_COUNT];
        productivity = new int[Inventory.SKU_COUNT];
        soldCommoditiesSum = new double[Inventory.SKU_COUNT];
        soldCommoditiesAmount = new int[Inventory.SKU_COUNT];
        boughtCommoditiesSum = new double[Inventory.SKU_COUNT];
        boughtCommoditiesAmount = new int[Inventory.SKU_COUNT];
        Arrays.fill(manufacturedTotal, 0);
        Arrays.fill(soldCommoditiesSum, 0);
        Arrays.fill(soldCommoditiesAmount, 0);
        Arrays.fill(boughtCommoditiesSum, 0);
        Arrays.fill(boughtCommoditiesAmount, 0);
    }


    /**
     * Вычисляет отчёт по периоду TODO Report
     */
    public void process() {
        currentCashBalance = production.getCashBalance();

        long currentCycle = production.getCurrentCycle();
        if (currentCycle > startingCycle + OPERATIONAL_DAY_CYCLES) {
            // Копируем количество произведенного в отчёт
            System.arraycopy(manufacturedByPeriod, 0, productivity, 0, Inventory.SKU_COUNT);
            // Зануляем массив для учта произведенного за период
            Arrays.fill(manufacturedByPeriod, 0);

            periodRevenue = 0;
            periodExpenses = 0;
            periodMargin = 0;
            periodMaintenanceCost = 0;
            periodAssetsBought = 0;
            periodAssetsSold = 0;

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

    public double getTotalMargin() {
        return totalMargin;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public double getTotalExpenses() {
        return totalExpenses;
    }

    public double getTotalMaintenanceCost() {
        return totalMaintenanceCost;
    }

    public double getPeriodRevenue() {
        return periodRevenue;
    }

    public double getPeriodExpenses() {
        return periodExpenses;
    }

    public double getPeriodMargin() {
        return periodMargin;
    }

    public double getPeriodMaintenanceCost() {
        return periodMaintenanceCost;
    }

    public double getPeriodAssetsBought() {
        return periodAssetsBought;
    }

    public double getPeriodAssetsSold() {
        return periodAssetsSold;
    }

    //--------------------------------------------------------------------------------------------

    public double getCommoditySoldSum(int commodity) {
        return soldCommoditiesSum[commodity];
    }

    public double getTotalAssetsBought() {
        return totalAssetsBought;
    }

    public double getTotalAssetsSold() {
        return totalAssetsSold;
    }

    public int getCommoditySoldAmount(int commodity) { return soldCommoditiesAmount[commodity]; }

    public double getCommodityBoughtSum(int commodity) {
        return boughtCommoditiesSum[commodity];
    }

    public int getCommodityBoughtAmount(int commodity) { return boughtCommoditiesAmount[commodity]; }

    public int getManufacturedAmount(int commodity) {
        return manufacturedTotal[commodity];
    }

    public int getProductivity(int commodity) {
        return productivity[commodity];
    }

    //--------------------------------------------------------------------------------------------

    /**
     * Зарегистрировать расходы
     * @param type
     * @param sum
     */
    public void registerExpense(int type, double sum) {
        if (type==EXPENSE_BLOCK_BOUGHT) {
            totalAssetsBought += sum;
            periodAssetsSold += sum;
            return;
        }

        if (type==EXPENSE_BLOCK_OPERATION) {
            periodMaintenanceCost += sum;
            totalMaintenanceCost += sum;
        }

        periodExpenses += sum;
        periodMargin = periodRevenue - periodExpenses;

        totalExpenses += sum;
        totalMargin = totalRevenue - totalExpenses;

    }

    /**
     * Зарегистрировать доход
     * @param type
     * @param sum
     */
    public void registerRevenue(int type, double sum) {
        if (type==REVENUE_BLOCK_SOLD) {
            totalAssetsSold += sum;
            periodAssetsSold += sum;
            return;
        }

        periodRevenue += sum;
        periodMargin = periodRevenue - periodExpenses;

        totalRevenue += sum;
        totalMargin = totalRevenue - totalExpenses;
    }


    /**
     * Зарегистрировать произведенный материал
     * @param commodity
     * @param quantity
     */
    public void registerCommodityManufactured(int commodity, int quantity) {
        manufacturedTotal[commodity] += quantity;
        manufacturedByPeriod[commodity] += quantity;
    }

    /**
     * Зарегистрировать проданный материал
     * @param commodity
     * @param quantity
     * @param price
     */
    public void registerCommoditySold(int commodity, int quantity, double price) {
        soldCommoditiesSum[commodity] += quantity * price;
        soldCommoditiesAmount[commodity] += quantity;
    }

    /**
     * Зарегистрировать купленный материал
     * @param commodity
     * @param quantity
     * @param price
     */
    public void registerCommodityBought(int commodity, int quantity, double price) {
        boughtCommoditiesSum[commodity] += quantity * price;
        boughtCommoditiesAmount[commodity] += quantity;
    }

}
