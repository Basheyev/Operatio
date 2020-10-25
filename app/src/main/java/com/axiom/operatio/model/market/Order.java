package com.axiom.operatio.model.market;

import com.axiom.atom.engine.data.JSONSerializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Order implements JSONSerializable {

    public static final int BUY = 1;
    public static final int SELL = 2;

    public int type;                        // Тип: BUY/SELL
    public int agentID;                     // ID агента
    public int materialID;                  // ID материала
    public int quantity;                    // Количество
    public int price;                       // Предельная цена

    public JSONObject serialize() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", materialID);
            jsonObject.put("agent", agentID);
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
