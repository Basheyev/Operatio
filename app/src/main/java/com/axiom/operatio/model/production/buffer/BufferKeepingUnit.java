package com.axiom.operatio.model.production.buffer;

import com.axiom.operatio.model.materials.Material;

import org.json.JSONException;
import org.json.JSONObject;

public class BufferKeepingUnit {
    public Material material;
    public int capacity;
    public int total;

    public JSONObject serialize() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("material", material!=null ? material.getMaterialID() : null);
            jsonObject.put("capacity", capacity);
            jsonObject.put("total", total);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return jsonObject;
    }
}
