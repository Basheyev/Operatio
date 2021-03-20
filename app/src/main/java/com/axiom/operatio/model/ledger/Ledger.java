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
    //---------------------------------------------------------------------------------------------
    public static final int HISTORY_LENGTH = 32;                 // Максимальная длина истории
    private LedgerPeriod total;                                  // Данные за весь период
    private LedgerPeriod currentPeriod;                          // Данные текущего периода
    private LedgerPeriod lastPeriod;                             // Данные прошлого периода
    private LedgerPeriod[] history;                              // История прошлых периодов
    private int historyCounter = 0;                              // Фактическая длина истории в днях
    //---------------------------------------------------------------------------------------------
    private MaterialRecord[] materialRecords;                    // Статистика по материалам
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
            startingCycle = jsonObject.getLong("startingCycle");
            total = new LedgerPeriod(jsonObject.getJSONObject("total"));
            currentPeriod = new LedgerPeriod(jsonObject.getJSONObject("currentPeriod"));
            lastPeriod = new LedgerPeriod(jsonObject.getJSONObject("lastPeriod"));
            JSONArray jsonHistory = jsonObject.getJSONArray("history");
            historyCounter = jsonHistory.length();
            if (historyCounter > HISTORY_LENGTH) historyCounter = HISTORY_LENGTH - 1;
            for (int i=0; i<historyCounter; i++) {
                history[i] = new LedgerPeriod(jsonHistory.getJSONObject(i));
            }
            JSONArray jsonMaterialRecords = jsonObject.getJSONArray("materialRecords");
            for (int i=0; i< Inventory.SKU_COUNT; i++) {
                materialRecords[i] = new MaterialRecord(jsonMaterialRecords.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * Вычисляет отчёт по периоду
     */
    public void process() {
        currentPeriod.cashBalance = production.getCashBalance();
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
            materialRecord.boughtAmountByPeriod = materialRecord.boughtAmountCounter;
            materialRecord.boughtAmountCounter = 0;
            materialRecord.soldAmountByPeriod = materialRecord.soldAmountCounter;
            materialRecord.soldAmountCounter = 0;
        }

    }


    private void calculateHistory() {
        lastPeriod.copy(currentPeriod);
        history[historyCounter].copy(lastPeriod);
        historyCounter++;
        if (historyCounter >= HISTORY_LENGTH) {
            for (int i=1; i< HISTORY_LENGTH; i++) history[i-1].copy(history[i]);
            historyCounter--;
        }
        currentPeriod.clear();
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
        return currentPeriod.cashBalance;
    }

    public LedgerPeriod getCurrentPeriod() {
        return currentPeriod;
    }

    public LedgerPeriod getLastPeriod() {
        return lastPeriod;
    }


    public LedgerPeriod getTotal() {
        return total;
    }

    public MaterialRecord getMaterialRecord(int materialID) {
        if (materialID < 0 || materialID >= materialRecords.length) return null;
        return materialRecords[materialID];
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
        }

        currentPeriod.revenue += sum;
        currentPeriod.margin = currentPeriod.revenue - currentPeriod.expenses;

        total.revenue += sum;
        total.margin = total.revenue - total.expenses;
    }


    /**
     * Зарегистрировать произведенный материал
     * @param materialID
     * @param quantity
     */
    public void registerCommodityManufactured(int materialID, int quantity) {
        materialRecords[materialID].manufacturedTotal += quantity;
        materialRecords[materialID].manufacturedByPeriod += quantity;
    }

    /**
     * Зарегистрировать проданный материал
     * @param materialID
     * @param quantity
     * @param price
     */
    public void registerCommoditySold(int materialID, int quantity, double price) {
        materialRecords[materialID].soldSumTotal += quantity * price;
        materialRecords[materialID].soldAmountTotal += quantity;
        materialRecords[materialID].soldAmountCounter += quantity;
    }

    /**
     * Зарегистрировать купленный материал
     * @param materialID
     * @param quantity
     * @param price
     */
    public void registerCommodityBought(int materialID, int quantity, double price) {
        materialRecords[materialID].boughtSumTotal += quantity * price;
        materialRecords[materialID].boughtAmountTotal += quantity;
        materialRecords[materialID].boughtAmountCounter += quantity;
    }


    @Override
    public JSONObject toJSON() {
        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("class", "Ledger");
            jsonObject.put("startingCycle", startingCycle);

            jsonObject.put("total", total.toJSON());
            jsonObject.put("currentPeriod", currentPeriod.toJSON());
            jsonObject.put("lastPeriod", lastPeriod.toJSON());

            JSONArray jsonHistory = new JSONArray();
            for (int i=0; i<historyCounter; i++) {
                jsonHistory.put(history[i].toJSON());
            }
            jsonObject.put("history", jsonHistory);

            JSONArray jsonMaterialRecords = new JSONArray();
            for (int i=0; i< Inventory.SKU_COUNT; i++) {
                jsonMaterialRecords.put(materialRecords[i].toJSON());
            }
            jsonObject.put("materialRecords", jsonMaterialRecords);

            return jsonObject;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
