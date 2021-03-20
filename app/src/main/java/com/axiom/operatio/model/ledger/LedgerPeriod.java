package com.axiom.operatio.model.ledger;

import com.axiom.operatio.model.common.JSONSerializable;

import org.json.JSONException;
import org.json.JSONObject;

public class LedgerPeriod implements JSONSerializable {

    protected double revenue;                             // Операционная выручка за период
    protected double expenses;                            // Операционные расходы за период
    protected double margin;                              // Маржа за период
    protected double cashBalance;                         // Операционная маржа за период
    protected double maintenanceCost;                     // Расходы на обслуживание
    protected double assetsBought;                        // Приобретено активов
    protected double assetsSold;                          // Продано активов


    public LedgerPeriod() {
        clear();
    }


    public LedgerPeriod(JSONObject jsonObject) throws JSONException {
        cashBalance = jsonObject.getDouble("currentCashBalance");
        revenue = jsonObject.getDouble("currentPeriodRevenue");
        expenses = jsonObject.getDouble("currentPeriodExpenses");
        margin = jsonObject.getDouble("currentPeriodMargin");
        maintenanceCost = jsonObject.getDouble("periodMaintenanceCost");
        assetsBought = jsonObject.getDouble("periodAssetsBought");
        assetsSold = jsonObject.getDouble("periodAssetsSold");
    }


    public void clear() {
        revenue = 0;
        expenses = 0;
        margin = 0;
        cashBalance = 0;
        maintenanceCost = 0;
        assetsBought = 0;
        assetsSold = 0;
    }


    public void copy(LedgerPeriod from) {
        revenue = from.revenue;
        expenses = from.expenses;
        margin = from.margin;
        cashBalance = from.cashBalance;
        maintenanceCost = from.maintenanceCost;
        assetsBought = from.assetsBought;
        assetsSold = from.assetsSold;
    }

    public double getRevenue() {
        return revenue;
    }

    public double getExpenses() {
        return expenses;
    }

    public double getMargin() {
        return margin;
    }


    @Override
    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("currentPeriodRevenue", revenue);
            jsonObject.put("currentPeriodExpenses", expenses);
            jsonObject.put("currentPeriodMargin", margin);
            jsonObject.put("currentCashBalance", cashBalance);
            jsonObject.put("periodMaintenanceCost", maintenanceCost);
            jsonObject.put("periodAssetsBought", assetsBought);
            jsonObject.put("periodAssetsSold", assetsSold);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

}
