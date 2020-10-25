package com.axiom.operatio.model.market;

import org.json.JSONException;
import org.json.JSONObject;

public class Order {

    public static final int BUY = 1;
    public static final int SELL = 2;

    public int agent;                       // ID агента
    public int type;                        // Тип: BUY/SELL
    public int materialID;                  // ID материала
    public int quantity;                    // Количество
    public int price;                       // Предельная цена

    public JSONObject serialize() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("agent", materialID);
            jsonObject.put("type", materialID);
            jsonObject.put("material", materialID);
            jsonObject.put("quantity", quantity);
            jsonObject.put("price", price);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return jsonObject;
    }


}
