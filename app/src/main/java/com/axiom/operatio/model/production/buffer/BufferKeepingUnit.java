package com.axiom.operatio.model.production.buffer;

import com.axiom.atom.engine.data.JSONSerializable;
import com.axiom.operatio.model.materials.Item;
import com.axiom.operatio.model.materials.Material;

import org.json.JSONException;
import org.json.JSONObject;

public class BufferKeepingUnit implements JSONSerializable {
    public Material material;
    public int capacity;
    public int total;

    public JSONObject serialize() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("material", material!=null ? material.getMaterialID() : -1);
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
