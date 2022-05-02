package com.basheyev.operatio.model.gameplay;


import com.basheyev.operatio.model.ledger.Ledger;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Игровое условие для миссий
 */
public class GameCondition {

    public static final int MANUFACTURED_AMOUNT = 1;           // Произведенное количество
    public static final int MANUFACTURE_PRODUCTIVITY = 2;      // Продуктивность за день
    public static final int SOLD_AMOUNT = 3;                   // Проданное количество всего
    public static final int REVENUE_PER_DAY = 4;               // Выручка в день
    public static final int CASH_BALANCE = 5;                  // Остаток денег
    public static final int OPERATING_MARGIN = 6;              // Операционная маржинальность
    public static final int VALUATION = 7;                     // Капитализация

    protected int indicatorType;                               // Тип показателя
    protected int materialID;                                  // ID материала
    protected double value;                                    // Значение


    public GameCondition(JSONObject condition) throws JSONException {
        this.indicatorType = condition.getInt("indicatorType");
        this.materialID = condition.getInt("materialID");
        this.value = condition.getDouble("value");
    }


    public boolean check(Ledger ledger) {
        switch (indicatorType) {
            case MANUFACTURED_AMOUNT: return checkManufacturedAmount(ledger);
            case MANUFACTURE_PRODUCTIVITY: return checkManufacturedProductivity(ledger);
            case SOLD_AMOUNT: return checkSoldAmount(ledger);
            case REVENUE_PER_DAY: return checkRevenuePerDay(ledger);
            case CASH_BALANCE: return checkCashBalance(ledger);
            case OPERATING_MARGIN: return checkOperatingMargin(ledger);
            case VALUATION: return checkValuation(ledger);
        }
        return false;
    }


    private boolean checkManufacturedAmount(Ledger ledger) {
        return ledger.getMaterialRecord(materialID).getManufacturedTotal() >= value;
    }

    private boolean checkManufacturedProductivity(Ledger ledger) {
        return ledger.getMaterialRecord(materialID).getProductivity() >= value;
    }

    public boolean checkSoldAmount(Ledger ledger) {
        return ledger.getMaterialRecord(materialID).getSoldAmountTotal() >= value;
    }

    public boolean checkRevenuePerDay(Ledger ledger) {
        return ledger.getLastPeriod().getRevenue() >= value;
    }

    public boolean checkCashBalance(Ledger ledger) {
        return ledger.getCurrentCashBalance() >= value;
    }

    public boolean checkOperatingMargin(Ledger ledger) {
        return ledger.getTotal().getMargin() >= value;
    }

    public boolean checkValuation(Ledger ledger) {
        return ledger.getValuation() >= value;
    }

}
