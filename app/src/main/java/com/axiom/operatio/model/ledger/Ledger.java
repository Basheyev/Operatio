package com.axiom.operatio.model.ledger;

import com.axiom.operatio.model.common.JSONSerializable;
import com.axiom.operatio.model.materials.Material;
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
    public static final int EXPENSE_UNKNOWN = 0x0000;            // Неизвестный расход
    public static final int EXPENSE_BLOCK_BOUGHT    = 0x0001;    // Расходы на покупку блока
    public static final int EXPENSE_BLOCK_OPERATION = 0x0002;    // Расходы на операцию блока
    public static final int EXPENSE_MATERIAL_BOUGHT = 0x0003;    // Расходы на покупку материала
    public static final int EXPENSE_TILE_BOUGHT = 0x0004;        // Расходы на покупку земли
    public static final int EXPENSE_RECIPE_BOUGHT   = 0x0005;    // Расходы на покупку технологии
    public static final int EXPENSE_DIVIDENDS       = 0x000F;    // Выплаты дивидендов

    public static final int REVENUE_UNKNOWN         = 0x0000;    // Неизвестный доход
    public static final int REVENUE_BLOCK_SOLD      = 0x1001;    // Выручка от продажи блока
    public static final int REVENUE_MATERIAL_SOLD   = 0x1002;    // Выручка от продажи материала
    //---------------------------------------------------------------------------------------------
    private Production production;                               // Производство
    private double dividendsPayed;                               // Выплачено дивидендов
    private double cashBalance;                                  // Текущий остаток денег
    private long startingCycle = 0;                              // Цикл производства - начала учёта
    //---------------------------------------------------------------------------------------------
    public static final int HISTORY_LENGTH = 30;                 // Максимальная длина истории
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
        materialRecords = new MaterialRecord[Material.COUNT];
        for (int i = 0; i<Material.COUNT; i++) {
            materialRecords[i] = new MaterialRecord();
        }
        cashBalance = 0;
        dividendsPayed = 0;
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
            cashBalance = jsonObject.getDouble("cashBalance");
            try {
                dividendsPayed = jsonObject.getDouble("dividendsPayed");
            } catch (JSONException e) {
                dividendsPayed = 0;
            }

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
            for (int i = 0; i< Material.COUNT; i++) {
                materialRecords[i] = new MaterialRecord(jsonMaterialRecords.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public synchronized JSONObject toJSON() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("class", "Ledger");
            jsonObject.put("startingCycle", startingCycle);
            jsonObject.put("cashBalance", cashBalance);
            jsonObject.put("dividendsPayed", dividendsPayed);
            jsonObject.put("total", total.toJSON());
            jsonObject.put("currentPeriod", currentPeriod.toJSON());
            jsonObject.put("lastPeriod", lastPeriod.toJSON());
            JSONArray jsonHistory = new JSONArray();
            for (int i=0; i<historyCounter; i++) {
                jsonHistory.put(history[i].toJSON());
            }
            jsonObject.put("history", jsonHistory);
            JSONArray jsonMaterialRecords = new JSONArray();
            for (int i = 0; i< Material.COUNT; i++) {
                jsonMaterialRecords.put(materialRecords[i].toJSON());
            }
            jsonObject.put("materialRecords", jsonMaterialRecords);
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Вычисляет отчёт по периоду
     */
    public synchronized void process() {

        currentPeriod.cashBalance = getCashBalance();

        long currentCycle = production.getCurrentCycle();
        if (currentCycle > startingCycle + OPERATIONAL_DAY_CYCLES) {
            calculateMaterialsPeriod();
            calculateHistory();
            startingCycle = currentCycle;
        }
    }



    private synchronized void calculateMaterialsPeriod() {
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


    private synchronized void calculateHistory() {
        lastPeriod.copy(currentPeriod);
        history[historyCounter].copy(lastPeriod);
        historyCounter++;
        if (historyCounter >= HISTORY_LENGTH) {
            for (int i=1; i< HISTORY_LENGTH; i++) history[i-1].copy(history[i]);
            historyCounter--;
        }
        currentPeriod.clear();
    }



    public synchronized int getHistoryCounter() {
        return historyCounter;
    }

    public synchronized LedgerPeriod[] getHistory() {
        return history;
    }

    public synchronized double getHistoryAverageRevenue() {
        double revenueSum = 0;
        for (int i=0; i<historyCounter; i++) revenueSum += history[i].revenue;
        return historyCounter > 0 ? revenueSum / historyCounter : 0;
    }

    public synchronized double getHistoryAverageMargin() {
        double marginSum = 0;
        for (int i=0; i<historyCounter; i++) marginSum += history[i].margin;
        return historyCounter > 0 ? marginSum / historyCounter : 0;
    }

    /**
     * Капитализация компании
     * @return
     */
    public synchronized double getValuation() {
        // Посчитать средний денежный поток за прошлый период
        double T = 12;                                                         // Период 12 мес
        double WACC = 0.10;                                                    // Стоимость капитала 10%
        double cashFlow = getHistoryCashFlow();                                // Денежный поток
        double assetsValue = production.getAssetsValuation();                  // Активы
        double inventoryValue = production.getInventory().getValuation();      // Материалы на складе
        double workInProgressValue = production.getWorkInProgressValuation();  // Материалы в цеху
        double cash = getCashBalance();                                        // Остатки денег
        double value = (cashFlow * T) / Math.pow(1 + WACC, T);
        value += assetsValue + inventoryValue + workInProgressValue;
        value = value + cash;

        return value;  // Капитализация
    }


    private synchronized double getHistoryCashFlow() {
        double historyCashFlow = 0;
        for (int i=0; i<historyCounter; i++) {
            historyCashFlow += history[i].margin;
        }
        return historyCashFlow;
    }


    public synchronized double getCurrentCashBalance() {
        return currentPeriod.cashBalance;
    }

    public synchronized LedgerPeriod getCurrentPeriod() {
        return currentPeriod;
    }

    public synchronized LedgerPeriod getLastPeriod() {
        return lastPeriod;
    }


    public synchronized LedgerPeriod getTotal() {
        return total;
    }

    public synchronized MaterialRecord getMaterialRecord(int materialID) {
        if (materialID < 0 || materialID >= materialRecords.length) return null;
        return materialRecords[materialID];
    }

    //--------------------------------------------------------------------------------------------

    /**
     * Зарегистрировать расходы
     * @param type
     * @param sum
     */
    private synchronized void registerExpense(int type, double sum) {
        if (type==EXPENSE_BLOCK_BOUGHT || type== EXPENSE_TILE_BOUGHT || type==EXPENSE_RECIPE_BOUGHT) {
            currentPeriod.assetsBought += sum;
            total.assetsBought += sum;
            total.expenses += sum;
            total.margin = total.revenue - total.expenses;
            return;
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
    private synchronized void registerRevenue(int type, double sum) {
        if (type==REVENUE_UNKNOWN) return;

        if (type==REVENUE_BLOCK_SOLD) {
            total.assetsSold += sum;
            currentPeriod.assetsSold += sum;
            total.revenue += sum;
            total.margin = total.revenue - total.expenses;
            return;
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
    public synchronized void materialManufactured(int materialID, int quantity) {
        materialRecords[materialID].manufacturedTotal += quantity;
        materialRecords[materialID].manufacturedByPeriod += quantity;
    }

    /**
     * Зарегистрировать проданный материал
     * @param materialID
     * @param quantity
     * @param price
     */
    public synchronized void materialSold(int materialID, int quantity, double price) {
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
    public synchronized void materialBought(int materialID, int quantity, double price) {
        materialRecords[materialID].boughtSumTotal += quantity * price;
        materialRecords[materialID].boughtAmountTotal += quantity;
        materialRecords[materialID].boughtAmountCounter += quantity;
    }


    public synchronized boolean payDividends(float percent) {
        if (percent <=0 || percent > 100) return false;
        double dividendsSum = cashBalance * (percent / 100.0f);
        cashBalance -= dividendsSum;
        dividendsPayed += dividendsSum;
        return true;
    }

    public synchronized double getCashBalance() {
        return cashBalance;
    }

    public synchronized double getDividendsPayed() {
        return dividendsPayed;
    }

    public synchronized boolean creditCashBalance(int type, double value) {
        double result = cashBalance - value;
        if (result < 0) return false;
        registerExpense(type, value);
        cashBalance = result;
        return true;
    }

    public synchronized double debitCashBalance(int type, double value) {
        cashBalance += value;
        registerRevenue(type, value);
        return cashBalance;
    }


}
