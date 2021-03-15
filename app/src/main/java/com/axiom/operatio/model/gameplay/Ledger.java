package com.axiom.operatio.model.gameplay;

import com.axiom.operatio.utils.JSONSerializable;
import com.axiom.operatio.model.inventory.Inventory;
import com.axiom.operatio.model.production.Production;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Главный журнал регистрации всех производственных и финансовых событий
 */
public class Ledger implements JSONSerializable {
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
    private double currentPeriodRevenue = 0;                     // Операционная выручка за период
    private double currentPeriodExpenses = 0;                    // Операционные расходы за период
    private double currentPeriodMargin = 0;                      // Операционная маржа за период
    //---------------------------------------------------------------------------------------------
    private double lastPeriodRevenue = 0;
    private double lastPeriodExpenses = 0;
    private double lastPeriodMargin = 0;
    private double periodMaintenanceCost = 0;                    // Расходы на операции за период
    private double periodAssetsBought = 0;                       // Приобретено активов на сумму
    private double periodAssetsSold = 0;                         // Продано активов на сумму
    //---------------------------------------------------------------------------------------------
    public static final int HISTORY_LENGTH = 32;                 // Максимальная длина истории
    private int historyCounter = 0;                              // Фактическая длина истории в днях
    private double[] historyRevenue;                             // Операционная выручка за период
    private double[] historyExpenses;                            // Операционные расходы за период
    private double[] historyCashBalance;                         // Операционная маржа за период
    //---------------------------------------------------------------------------------------------
    private int[] productivity;                                  // Производительность за период
    private int[] manufacturedByPeriod;                          // Объем произведенных материалов за период
    private int[] manufacturedTotal;                             // Объем произведенных материалов всего
    private double[] soldCommoditiesSum;                         // Объем проданных материалов
    private int[] soldCommoditiesAmount;                         // Количество проданных материалов
    private int[] soldCommoditiesByPeriod;
    private int[] soldCommoditiesCounter;
    private double[] boughtCommoditiesSum;                       // Объем приобретенных материалов
    private int[] boughtCommoditiesAmount;                       // Количество приобретенных материалов
    private int[] boughtCommoditiesByPeriod;
    private int[] boughtCommoditiesCounter;
    //---------------------------------------------------------------------------------------------


    public Ledger(Production production) {
        this.production = production;
        manufacturedTotal = new int[Inventory.SKU_COUNT];
        manufacturedByPeriod = new int[Inventory.SKU_COUNT];
        productivity = new int[Inventory.SKU_COUNT];

        soldCommoditiesSum = new double[Inventory.SKU_COUNT];
        soldCommoditiesAmount = new int[Inventory.SKU_COUNT];
        soldCommoditiesByPeriod  = new int[Inventory.SKU_COUNT];
        soldCommoditiesCounter = new int[Inventory.SKU_COUNT];

        boughtCommoditiesSum = new double[Inventory.SKU_COUNT];
        boughtCommoditiesAmount = new int[Inventory.SKU_COUNT];
        boughtCommoditiesByPeriod = new int[Inventory.SKU_COUNT];
        boughtCommoditiesCounter = new int[Inventory.SKU_COUNT];

        Arrays.fill(manufacturedTotal, 0);

        Arrays.fill(soldCommoditiesSum, 0);
        Arrays.fill(soldCommoditiesAmount, 0);
        Arrays.fill(soldCommoditiesByPeriod, 0);
        Arrays.fill(soldCommoditiesCounter, 0);

        Arrays.fill(boughtCommoditiesSum, 0);
        Arrays.fill(boughtCommoditiesAmount, 0);
        Arrays.fill(boughtCommoditiesByPeriod, 0);
        Arrays.fill(boughtCommoditiesCounter, 0);

        historyRevenue = new double[HISTORY_LENGTH];
        historyExpenses = new double[HISTORY_LENGTH];
        historyCashBalance = new double[HISTORY_LENGTH];

    }


    public Ledger(Production production, JSONObject jsonObject) {
        this(production);

        try {
            /*
            boolean validClass = jsonObject.getString("class").equals("Ledger");
             */
            startingCycle = jsonObject.getLong("startingCycle");
            totalRevenue = jsonObject.getDouble("totalRevenue");
            totalExpenses = jsonObject.getDouble("totalExpenses");
            totalMargin = jsonObject.getDouble("totalMargin");

            totalMaintenanceCost = jsonObject.getDouble("totalMaintenanceCost");
            totalAssetsBought = jsonObject.getDouble("totalAssetsBought");
            totalAssetsSold = jsonObject.getDouble("totalAssetsSold");
            currentCashBalance = jsonObject.getDouble("currentCashBalance");

            currentPeriodRevenue = jsonObject.getDouble("currentPeriodRevenue");
            currentPeriodExpenses = jsonObject.getDouble("currentPeriodExpenses");
            currentPeriodMargin = jsonObject.getDouble("currentPeriodMargin");

            lastPeriodRevenue = jsonObject.getDouble("lastPeriodRevenue");
            lastPeriodExpenses = jsonObject.getDouble("lastPeriodExpenses");
            lastPeriodMargin = jsonObject.getDouble("lastPeriodMargin");
            periodMaintenanceCost = jsonObject.getDouble("periodMaintenanceCost");
            periodAssetsBought = jsonObject.getDouble("periodAssetsBought");
            periodAssetsSold = jsonObject.getDouble("periodAssetsSold");

            historyCounter = jsonObject.getInt("historyCounter");
            JSONArray jHistoryRevenue = jsonObject.getJSONArray("historyRevenue");
            JSONArray jHistoryExpenses = jsonObject.getJSONArray("historyExpenses");
            JSONArray jHistoryCashBalance = jsonObject.getJSONArray("historyCashBalance");
            for (int i = historyCounter-1; i >= 0; i--) {
                historyRevenue[i] = jHistoryRevenue.getDouble(i);
                historyExpenses[i] = jHistoryExpenses.getDouble(i);
                historyCashBalance[i] = jHistoryCashBalance.getDouble(i);
            }

            JSONArray jProductivity = jsonObject.getJSONArray("productivity");
            JSONArray jManufacturedByPeriod = jsonObject.getJSONArray("manufacturedByPeriod");
            JSONArray jManufacturedTotal = jsonObject.getJSONArray("manufacturedTotal");
            JSONArray jSoldCommoditiesSum = jsonObject.getJSONArray("soldCommoditiesSum");
            JSONArray jSoldCommoditiesAmount = jsonObject.getJSONArray("soldCommoditiesAmount");
            JSONArray jSoldCommoditiesCounter = jsonObject.getJSONArray("soldCommoditiesCounter");
            JSONArray jSoldCommoditiesByPeriod = jsonObject.getJSONArray("soldCommoditiesByPeriod");

            JSONArray jBoughtCommoditiesSum = jsonObject.getJSONArray("boughtCommoditiesSum");
            JSONArray jBoughtCommoditiesAmount = jsonObject.getJSONArray("boughtCommoditiesAmount");
            JSONArray jBoughtCommoditiesCounter = jsonObject.getJSONArray("boughtCommoditiesCounter");
            JSONArray jBoughtCommoditiesByPeriod = jsonObject.getJSONArray("boughtCommoditiesByPeriod");

            for (int i = Inventory.SKU_COUNT-1; i >= 0; i--) {
                productivity[i] = jProductivity.getInt(i);
                manufacturedByPeriod[i] = jManufacturedByPeriod.getInt(i);
                manufacturedTotal[i] = jManufacturedTotal.getInt(i);
                soldCommoditiesSum[i] = jSoldCommoditiesSum.getDouble(i);
                soldCommoditiesAmount[i] = jSoldCommoditiesAmount.getInt(i);
                soldCommoditiesCounter[i] = jSoldCommoditiesCounter.getInt(i);
                soldCommoditiesByPeriod[i] = jSoldCommoditiesByPeriod.getInt(i);

                boughtCommoditiesSum[i] = jBoughtCommoditiesSum.getDouble(i);
                boughtCommoditiesAmount[i] = jBoughtCommoditiesAmount.getInt(i);
                boughtCommoditiesCounter[i] = jBoughtCommoditiesCounter.getInt(i);
                boughtCommoditiesByPeriod[i] = jBoughtCommoditiesByPeriod.getInt(i);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    /**
     * Вычисляет отчёт по периоду
     */
    public void process() {
        currentCashBalance = production.getCashBalance();

        long currentCycle = production.getCurrentCycle();
        if (currentCycle > startingCycle + OPERATIONAL_DAY_CYCLES) {

            // Копируем количество произведенного в отчёт
            System.arraycopy(manufacturedByPeriod, 0, productivity, 0, Inventory.SKU_COUNT);
            // Зануляем массив для учета произведенного за период
            Arrays.fill(manufacturedByPeriod, 0);

            // Копируем количество закупленного в отчёт
            System.arraycopy(boughtCommoditiesCounter, 0, boughtCommoditiesByPeriod, 0, Inventory.SKU_COUNT);
            // Зануляем массив для учета закупленного за период
            Arrays.fill(boughtCommoditiesCounter, 0);

            // Копируем количество проданного в отчёт
            System.arraycopy(soldCommoditiesCounter, 0, soldCommoditiesByPeriod, 0, Inventory.SKU_COUNT);
            // Зануляем массив для учета проданного за период
            Arrays.fill(soldCommoditiesCounter, 0);

            lastPeriodMargin = currentPeriodMargin;
            historyRevenue[historyCounter] = lastPeriodRevenue = currentPeriodRevenue;
            historyExpenses[historyCounter] = lastPeriodExpenses = currentPeriodExpenses;
            historyCashBalance[historyCounter] = currentCashBalance;
            historyCounter++;

            if (historyCounter >= HISTORY_LENGTH) {
                System.arraycopy(historyRevenue, 1, historyRevenue, 0, HISTORY_LENGTH - 1);
                System.arraycopy(historyExpenses, 1, historyExpenses, 0, HISTORY_LENGTH - 1);
                System.arraycopy(historyCashBalance, 1, historyCashBalance, 0, HISTORY_LENGTH - 1);
                historyCounter--;
            }

            currentPeriodRevenue = 0;
            currentPeriodExpenses = 0;
            currentPeriodMargin = 0;
            periodMaintenanceCost = 0;
            periodAssetsBought = 0;
            periodAssetsSold = 0;

            startingCycle = currentCycle;
        }

    }

    public int getHistoryCounter() {
        return historyCounter;
    }

    public double[] getHistoryRevenue() {
        return historyRevenue;
    }

    public double[] getHistoryExpenses() {
        return historyExpenses;
    }

    public double[] getHistoryCashBalance() {
        return historyCashBalance;
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

    public double getLastPeriodRevenue() {
        return lastPeriodRevenue;
    }

    public double getLastPeriodExpenses() {
        return lastPeriodExpenses;
    }

    public double getLastPeriodMargin() {
        return lastPeriodMargin;
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

    public int getCommoditySoldByPeriod(int commodity) {
        return soldCommoditiesByPeriod[commodity];
    }

    public int getCommoditySoldAmount(int commodity) {
        return soldCommoditiesAmount[commodity];
    }

    public double getCommodityBoughtSum(int commodity) {
        return boughtCommoditiesSum[commodity];
    }

    public int getCommodityBoughtByPeriod(int commodity) {
        return boughtCommoditiesByPeriod[commodity];
    }

    public int getCommodityBoughtAmount(int commodity) {
        return boughtCommoditiesAmount[commodity];
    }

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
           // return;
        }

        if (type==EXPENSE_BLOCK_OPERATION) {
            periodMaintenanceCost += sum;
            totalMaintenanceCost += sum;
        }

        currentPeriodExpenses += sum;
        currentPeriodMargin = currentPeriodRevenue - currentPeriodExpenses;

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
        //    return;
        }

        currentPeriodRevenue += sum;
        currentPeriodMargin = currentPeriodRevenue - currentPeriodExpenses;

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
        soldCommoditiesCounter[commodity] += quantity;
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
        boughtCommoditiesCounter[commodity] += quantity;
    }


    @Override
    public JSONObject serialize() {
        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("class", "Ledger");

            jsonObject.put("startingCycle", startingCycle);
            jsonObject.put("totalRevenue", totalRevenue);
            jsonObject.put("totalExpenses", totalExpenses);
            jsonObject.put("totalMargin", totalMargin);

            jsonObject.put("totalMaintenanceCost", totalMaintenanceCost);
            jsonObject.put("totalAssetsBought", totalAssetsBought);
            jsonObject.put("totalAssetsSold", totalAssetsSold);
            jsonObject.put("currentCashBalance", currentCashBalance);

            jsonObject.put("currentPeriodRevenue", currentPeriodRevenue);
            jsonObject.put("currentPeriodExpenses", currentPeriodExpenses);
            jsonObject.put("currentPeriodMargin", currentPeriodMargin);

            jsonObject.put("lastPeriodRevenue",lastPeriodRevenue);
            jsonObject.put("lastPeriodExpenses", lastPeriodExpenses);
            jsonObject.put("lastPeriodMargin", lastPeriodMargin);
            jsonObject.put("periodMaintenanceCost", periodMaintenanceCost);
            jsonObject.put("periodAssetsBought", periodAssetsBought);
            jsonObject.put("periodAssetsSold", periodAssetsSold);

            JSONArray jHistoryRevenue = new JSONArray();
            JSONArray jHistoryExpenses = new JSONArray();
            JSONArray jHistoryCashBalance = new JSONArray();
            for (int i=0; i<historyCounter; i++) {
                jHistoryRevenue.put(historyRevenue[i]);
                jHistoryExpenses.put(historyExpenses[i]);
                jHistoryCashBalance.put(historyCashBalance[i]);
            }
            jsonObject.put("historyCounter", historyCounter);
            jsonObject.put("historyRevenue", jHistoryRevenue);
            jsonObject.put("historyExpenses", jHistoryExpenses);
            jsonObject.put("historyCashBalance", jHistoryCashBalance);

            JSONArray jProductivity = new JSONArray();
            JSONArray jManufacturedByPeriod = new JSONArray();
            JSONArray jManufacturedTotal = new JSONArray();
            JSONArray jSoldCommoditiesSum = new JSONArray();
            JSONArray jSoldCommoditiesAmount = new JSONArray();
            JSONArray jSoldCommoditiesCounter = new JSONArray();
            JSONArray jSoldCommoditiesByPeriod = new JSONArray();
            JSONArray jBoughtCommoditiesSum = new JSONArray();
            JSONArray jBoughtCommoditiesAmount = new JSONArray();
            JSONArray jBoughtCommoditiesCounter = new JSONArray();
            JSONArray jBoughtCommoditiesByPeriod = new JSONArray();

            for (int i=0; i < Inventory.SKU_COUNT; i++) {
                jProductivity.put(productivity[i]);
                jManufacturedByPeriod.put(manufacturedByPeriod[i]);
                jManufacturedTotal.put(manufacturedTotal[i]);

                jSoldCommoditiesSum.put(soldCommoditiesSum[i]);
                jSoldCommoditiesAmount.put(soldCommoditiesAmount[i]);
                jSoldCommoditiesCounter.put(soldCommoditiesCounter[i]);
                jSoldCommoditiesByPeriod.put(soldCommoditiesByPeriod[i]);

                jBoughtCommoditiesSum.put(boughtCommoditiesSum[i]);
                jBoughtCommoditiesAmount.put(boughtCommoditiesAmount[i]);
                jBoughtCommoditiesCounter.put(boughtCommoditiesCounter[i]);
                jBoughtCommoditiesByPeriod.put(boughtCommoditiesByPeriod[i]);
            }
            jsonObject.put("productivity", jProductivity);
            jsonObject.put("manufacturedByPeriod", jManufacturedByPeriod);
            jsonObject.put("manufacturedTotal", jManufacturedTotal);

            jsonObject.put("soldCommoditiesSum", jSoldCommoditiesSum);
            jsonObject.put("soldCommoditiesAmount", jSoldCommoditiesAmount);
            jsonObject.put("soldCommoditiesCounter", jSoldCommoditiesCounter);
            jsonObject.put("soldCommoditiesByPeriod", jSoldCommoditiesByPeriod);

            jsonObject.put("boughtCommoditiesSum", jBoughtCommoditiesSum);
            jsonObject.put("boughtCommoditiesAmount", jBoughtCommoditiesAmount);
            jsonObject.put("boughtCommoditiesCounter", jBoughtCommoditiesCounter);
            jsonObject.put("boughtCommoditiesByPeriod", jBoughtCommoditiesByPeriod);

            return jsonObject;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
