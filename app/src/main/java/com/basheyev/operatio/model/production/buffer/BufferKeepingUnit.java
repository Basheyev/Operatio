package com.basheyev.operatio.model.production.buffer;

import com.basheyev.operatio.model.common.JSONSerializable;
import com.basheyev.operatio.model.materials.Material;

import org.json.JSONException;
import org.json.JSONObject;

public class BufferKeepingUnit implements JSONSerializable {
    public Material material;
    public int capacity;
    public int total;

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("material", material!=null ? material.getID() : -1);
            jsonObject.put("capacity", capacity);
            jsonObject.put("total", total);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return jsonObject;
    }

    public static BufferKeepingUnit deserialize(JSONObject jsonObject) {
        try {
            BufferKeepingUnit bufferKeepingUnit = new BufferKeepingUnit();
            int materialID = jsonObject.getInt("material");
            bufferKeepingUnit.material = (materialID>=0) ? Material.getMaterial(materialID) : null;
            bufferKeepingUnit.capacity = jsonObject.getInt("capacity");
            bufferKeepingUnit.total = jsonObject.getInt("total");
            return bufferKeepingUnit;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

}
