package com.axiom.operatio.model.ledger;

import com.axiom.operatio.model.common.JSONSerializable;

import org.json.JSONException;
import org.json.JSONObject;

public class MaterialRecord implements JSONSerializable {

    protected int productivity = 0;             // Производительность за период
    protected int manufacturedByPeriod = 0;     // Объем произведенных материалов за период
    protected int manufacturedTotal = 0;        // Объем произведенных материалов всего

    protected double soldSumTotal = 0;          // Объем проданных материалов
    protected int soldAmountTotal = 0;          // Количество проданных материалов
    protected int soldAmountByPeriod = 0;       // Сумма проданных материалов
    protected int soldAmountCounter = 0;        // Счётчик проданных материалов

    protected double boughtSumTotal = 0;        // Объем приобретенных материалов
    protected int boughtAmountTotal = 0;        // Количество приобретенных материалов
    protected int boughtAmountByPeriod = 0;     // Закуплено материалов за период
    protected int boughtAmountCounter = 0;      // Счётчик закупленных материалов


    public MaterialRecord() {

    }


    public MaterialRecord(JSONObject jsonObject) throws JSONException {

        productivity = jsonObject.getInt("productivity");
        manufacturedByPeriod = jsonObject.getInt("manufacturedByPeriod");
        manufacturedTotal = jsonObject.getInt("manufacturedTotal");

        soldSumTotal = jsonObject.getDouble("soldSumTotal");
        soldAmountTotal = jsonObject.getInt("soldAmountTotal");
        soldAmountCounter = jsonObject.getInt("soldAmountCounter");
        soldAmountByPeriod = jsonObject.getInt("soldAmountByPeriod");

        boughtSumTotal = jsonObject.getDouble("boughtSumTotal");
        boughtAmountTotal = jsonObject.getInt("boughtAmountTotal");
        boughtAmountCounter = jsonObject.getInt("boughtAmountCounter");
        boughtAmountByPeriod = jsonObject.getInt("boughtAmountByPeriod");

    }


    @Override
    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("productivity", productivity);
            jsonObject.put("manufacturedByPeriod", manufacturedByPeriod);
            jsonObject.put("manufacturedTotal", manufacturedTotal);

            jsonObject.put("soldSumTotal", soldSumTotal);
            jsonObject.put("soldAmountTotal", soldAmountTotal);
            jsonObject.put("soldAmountCounter", soldAmountCounter);
            jsonObject.put("soldAmountByPeriod", soldAmountByPeriod);

            jsonObject.put("boughtSumTotal", boughtSumTotal);
            jsonObject.put("boughtAmountTotal", boughtAmountTotal);
            jsonObject.put("boughtAmountCounter", boughtAmountCounter);
            jsonObject.put("boughtAmountByPeriod", boughtAmountByPeriod);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }



    public int getManufacturedTotal() {
        return manufacturedTotal;
    }


    public int getProductivity() {
        return productivity;
    }


    public int getSoldAmountTotal() {
        return soldAmountTotal;
    }


    public int getSoldAmountByPeriod() {
        return soldAmountByPeriod;
    }

    public int getBoughtAmountByPeriod() {
        return boughtAmountByPeriod;
    }
}
