package com.axiom.operatio.model.ledger;

import com.axiom.operatio.model.common.JSONSerializable;
import com.axiom.operatio.model.inventory.Inventory;
import com.axiom.operatio.model.production.Production;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private double currentCashBalance = 0;                       // Текущий остаток денег
    //---------------------------------------------------------------------------------------------
    public static final int HISTORY_LENGTH = 32;                 // Максимальная длина истории
    private LedgerPeriod total;                                  // Данные за весь период
    private LedgerPeriod currentPeriod;                          // Данные текущего периода
    private LedgerPeriod lastPeriod;                             // Данные прошлого периода
    private LedgerPeriod[] history;                              // История прошлых периодов
    private int historyCounter = 0;                              // Фактическая длина истории в днях
    //---------------------------------------------------------------------------------------------
    private MaterialRecord[] materialRecords;              // Статистика по материалам
    //---------------------------------------------------------------------------------------------


    public Ledger(Production production) {
        this.production = production;

        materialRecords = new MaterialRecord[Inventory.SKU_COUNT];

        for (int i=0; i<Inventory.SKU_COUNT; i++) {
            materialRecords[i] = new MaterialRecord();
        }

        total = new LedgerPeriod();
        currentPeriod = new LedgerPeriod();
        lastPeriod = new LedgerPeriod();
        history = new LedgerPeriod[HISTORY_LENGTH];
        for (int i=0; i<HISTORY_LENGTH; i++) {
            history[i] = new LedgerPeriod();
        }

    }


    public Ledger(Production production, JSONObject jsonObject) {
        this(production);

        try {
            /*
            boolean validClass = jsonObject.getString("class").equals("Ledger");
             */
            startingCycle = jsonObject.getLong("startingCycle");
            total.revenue = jsonObject.getDouble("totalRevenue");
            total.expenses = jsonObject.getDouble("totalExpenses");
            total.margin = jsonObject.getDouble("totalMargin");

            total.maintenanceCost = jsonObject.getDouble("totalMaintenanceCost");
            total.assetsBought = jsonObject.getDouble("totalAssetsBought");
            total.assetsSold = jsonObject.getDouble("totalAssetsSold");
            currentCashBalance = jsonObject.getDouble("currentCashBalance");

            currentPeriod.revenue = jsonObject.getDouble("currentPeriodRevenue");
            currentPeriod.expenses = jsonObject.getDouble("currentPeriodExpenses");
            currentPeriod.margin = jsonObject.getDouble("currentPeriodMargin");
            currentPeriod.maintenanceCost = jsonObject.getDouble("periodMaintenanceCost");
            currentPeriod.assetsBought = jsonObject.getDouble("periodAssetsBought");
            currentPeriod.assetsSold = jsonObject.getDouble("periodAssetsSold");

            lastPeriod.revenue = jsonObject.getDouble("lastPeriodRevenue");
            lastPeriod.expenses = jsonObject.getDouble("lastPeriodExpenses");
            lastPeriod.margin = jsonObject.getDouble("lastPeriodMargin");

            historyCounter = jsonObject.getInt("historyCounter");
            JSONArray jHistoryRevenue = jsonObject.getJSONArray("historyRevenue");
            JSONArray jHistoryExpenses = jsonObject.getJSONArray("historyExpenses");
            JSONArray jHistoryCashBalance = jsonObject.getJSONArray("historyCashBalance");
            for (int i = historyCounter-1; i >= 0; i--) {
                history[i].revenue = jHistoryRevenue.getDouble(i);
                history[i].expenses = jHistoryExpenses.getDouble(i);
                history[i].cashBalance = jHistoryCashBalance.getDouble(i);
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

                materialRecords[i].productivity = jProductivity.getInt(i);
                materialRecords[i].manufacturedByPeriod = jManufacturedByPeriod.getInt(i);
                materialRecords[i].manufacturedTotal = jManufacturedTotal.getInt(i);

                materialRecords[i].soldCommoditiesSum = jSoldCommoditiesSum.getDouble(i);
                materialRecords[i].soldCommoditiesAmount = jSoldCommoditiesAmount.getInt(i);
                materialRecords[i].soldCommoditiesCounter = jSoldCommoditiesCounter.getInt(i);
                materialRecords[i].soldCommoditiesByPeriod = jSoldCommoditiesByPeriod.getInt(i);

                materialRecords[i].boughtCommoditiesSum = jBoughtCommoditiesSum.getDouble(i);
                materialRecords[i].boughtCommoditiesAmount = jBoughtCommoditiesAmount.getInt(i);
                materialRecords[i].boughtCommoditiesCounter = jBoughtCommoditiesCounter.getInt(i);
                materialRecords[i].boughtCommoditiesByPeriod = jBoughtCommoditiesByPeriod.getInt(i);

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
            calculateMaterialsPeriod();
            calculateHistory();
            startingCycle = currentCycle;
        }
    }



    private void calculateMaterialsPeriod() {

        for (int i = 0; i< materialRecords.length; i++) {
            MaterialRecord materialRecord = materialRecords[i];
            materialRecord.productivity = materialRecord.manufacturedByPeriod;
            materialRecord.manufacturedByPeriod = 0;
            materialRecord.boughtCommoditiesByPeriod = materialRecord.boughtCommoditiesCounter;
            materialRecord.boughtCommoditiesCounter = 0;
            materialRecord.soldCommoditiesByPeriod = materialRecord.soldCommoditiesCounter;
            materialRecord.soldCommoditiesCounter = 0;
        }

    }


    private void calculateHistory() {

        lastPeriod.margin = currentPeriod.margin;
        history[historyCounter].revenue = lastPeriod.revenue = currentPeriod.revenue;
        history[historyCounter].expenses = lastPeriod.expenses = currentPeriod.expenses;
        history[historyCounter].cashBalance = currentCashBalance;
        historyCounter++;

        if (historyCounter >= HISTORY_LENGTH) {
            for (int i=1; i< HISTORY_LENGTH; i++) {
                history[i-1].revenue = history[i].revenue;
                history[i-1].expenses = history[i].expenses;
                history[i-1].cashBalance = history[i].cashBalance;
            }
            historyCounter--;
        }

        currentPeriod.revenue = 0;
        currentPeriod.expenses = 0;
        currentPeriod.margin = 0;
        currentPeriod.maintenanceCost = 0;
        currentPeriod.assetsBought = 0;
        currentPeriod.assetsSold = 0;
    }



    public int getHistoryCounter() {
        return historyCounter;
    }

    public LedgerPeriod[] getHistory() {
        return history;
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
        return total.margin;
    }

    public double getTotalRevenue() {
        return total.revenue;
    }

    public double getTotalExpenses() {
        return total.expenses;
    }

    public double getTotalMaintenanceCost() {
        return total.maintenanceCost;
    }

    public double getLastPeriodRevenue() {
        return lastPeriod.revenue;
    }

    public double getLastPeriodExpenses() {
        return lastPeriod.expenses;
    }

    public double getLastPeriodMargin() {
        return lastPeriod.margin;
    }

    public double getPeriodMaintenanceCost() {
        return currentPeriod.maintenanceCost;
    }

    public double getPeriodAssetsBought() {
        return currentPeriod.assetsBought;
    }

    public double getPeriodAssetsSold() {
        return currentPeriod.assetsSold;
    }

    //--------------------------------------------------------------------------------------------

    public double getCommoditySoldSum(int commodity) {
        return materialRecords[commodity].soldCommoditiesSum;
    }

    public double getTotalAssetsBought() {
        return total.assetsBought;
    }

    public double getTotalAssetsSold() {
        return total.assetsSold;
    }

    public int getCommoditySoldByPeriod(int commodity) {
        return materialRecords[commodity].soldCommoditiesByPeriod;
    }

    public int getCommoditySoldAmount(int commodity) {
        return materialRecords[commodity].soldCommoditiesAmount;
    }

    public double getCommodityBoughtSum(int commodity) {
        return materialRecords[commodity].boughtCommoditiesSum;
    }

    public int getCommodityBoughtByPeriod(int commodity) {
        return materialRecords[commodity].boughtCommoditiesByPeriod;
    }

    public int getCommodityBoughtAmount(int commodity) {
        return materialRecords[commodity].boughtCommoditiesAmount;
    }

    public int getManufacturedAmount(int commodity) {
        return materialRecords[commodity].manufacturedTotal;
    }

    public int getProductivity(int commodity) {
        return materialRecords[commodity].productivity;
    }

    //--------------------------------------------------------------------------------------------

    /**
     * Зарегистрировать расходы
     * @param type
     * @param sum
     */
    public void registerExpense(int type, double sum) {
        if (type==EXPENSE_BLOCK_BOUGHT) {
            total.assetsBought += sum;
            currentPeriod.assetsSold += sum;
           // return;
        }

        if (type==EXPENSE_BLOCK_OPERATION) {
            currentPeriod.maintenanceCost += sum;
            total.maintenanceCost += sum;
        }

        currentPeriod.expenses += sum;
        currentPeriod.margin = currentPeriod.revenue - currentPeriod.expenses;

        total.expenses += sum;
        total.margin = total.revenue - total.expenses;

    }

    /**
     * Зарегистрировать доход
     * @param type
     * @param sum
     */
    public void registerRevenue(int type, double sum) {
        if (type==REVENUE_BLOCK_SOLD) {
            total.assetsSold += sum;
            currentPeriod.assetsSold += sum;
        //    return;
        }

        currentPeriod.revenue += sum;
        currentPeriod.margin = currentPeriod.revenue - currentPeriod.expenses;

        total.revenue += sum;
        total.margin = total.revenue - total.expenses;
    }


    /**
     * Зарегистрировать произведенный материал
     * @param commodity
     * @param quantity
     */
    public void registerCommodityManufactured(int commodity, int quantity) {
        materialRecords[commodity].manufacturedTotal += quantity;
        materialRecords[commodity].manufacturedByPeriod += quantity;
    }

    /**
     * Зарегистрировать проданный материал
     * @param commodity
     * @param quantity
     * @param price
     */
    public void registerCommoditySold(int commodity, int quantity, double price) {
        materialRecords[commodity].soldCommoditiesSum += quantity * price;
        materialRecords[commodity].soldCommoditiesAmount += quantity;
        materialRecords[commodity].soldCommoditiesCounter += quantity;
    }

    /**
     * Зарегистрировать купленный материал
     * @param commodity
     * @param quantity
     * @param price
     */
    public void registerCommodityBought(int commodity, int quantity, double price) {
        materialRecords[commodity].boughtCommoditiesSum += quantity * price;
        materialRecords[commodity].boughtCommoditiesAmount += quantity;
        materialRecords[commodity].boughtCommoditiesCounter += quantity;
    }


    @Override
    public JSONObject toJSON() {
        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("class", "Ledger");

            jsonObject.put("startingCycle", startingCycle);
            jsonObject.put("totalRevenue", total.revenue);
            jsonObject.put("totalExpenses", total.expenses);
            jsonObject.put("totalMargin", total.margin);

            jsonObject.put("totalMaintenanceCost", total.maintenanceCost);
            jsonObject.put("totalAssetsBought", total.assetsBought);
            jsonObject.put("totalAssetsSold", total.assetsSold);
            jsonObject.put("currentCashBalance", currentCashBalance);

            jsonObject.put("currentPeriodRevenue", currentPeriod.revenue);
            jsonObject.put("currentPeriodExpenses", currentPeriod.expenses);
            jsonObject.put("currentPeriodMargin", currentPeriod.margin);

            jsonObject.put("lastPeriodRevenue",lastPeriod.revenue);
            jsonObject.put("lastPeriodExpenses", lastPeriod.expenses);
            jsonObject.put("lastPeriodMargin", lastPeriod.margin);
            jsonObject.put("periodMaintenanceCost", currentPeriod.maintenanceCost);
            jsonObject.put("periodAssetsBought", currentPeriod.assetsBought);
            jsonObject.put("periodAssetsSold", currentPeriod.assetsSold);

            JSONArray jHistoryRevenue = new JSONArray();
            JSONArray jHistoryExpenses = new JSONArray();
            JSONArray jHistoryCashBalance = new JSONArray();
            for (int i=0; i<historyCounter; i++) {
                jHistoryRevenue.put(history[i].revenue);
                jHistoryExpenses.put(history[i].expenses);
                jHistoryCashBalance.put(history[i].cashBalance);
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
                jProductivity.put(materialRecords[i].productivity);
                jManufacturedByPeriod.put(materialRecords[i].manufacturedByPeriod);
                jManufacturedTotal.put(materialRecords[i].manufacturedTotal);

                jSoldCommoditiesSum.put(materialRecords[i].soldCommoditiesSum);
                jSoldCommoditiesAmount.put(materialRecords[i].soldCommoditiesAmount);
                jSoldCommoditiesCounter.put(materialRecords[i].soldCommoditiesCounter);
                jSoldCommoditiesByPeriod.put(materialRecords[i].soldCommoditiesByPeriod);

                jBoughtCommoditiesSum.put(materialRecords[i].boughtCommoditiesSum);
                jBoughtCommoditiesAmount.put(materialRecords[i].boughtCommoditiesAmount);
                jBoughtCommoditiesCounter.put(materialRecords[i].boughtCommoditiesCounter);
                jBoughtCommoditiesByPeriod.put(materialRecords[i].boughtCommoditiesByPeriod);
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
