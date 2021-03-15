package com.axiom.operatio.model.materials;

import com.axiom.operatio.utils.JSONSerializable;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.block.Block;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Содержит информацию о предмете
 */
public class Item implements JSONSerializable {

    protected Material material;            // Материал
    protected Block owner;                  // Блок владелец
    protected long cycleOwned;              // Цикл производства (захват)
    protected long timeOwned;               // Время в миллисекундах (захват)
    protected long cost;                    // Стоимость
    //----------------------------------------------------------------------------

    public Item(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }

    public Block getOwner() {
        return owner;
    }

    public long getCycleOwned() {
        return cycleOwned;
    }

    public long getTimeOwned() { return timeOwned; }

    public void setOwner(Production production, Block owner) {
        this.owner = owner;
        this.cycleOwned = production.getCurrentCycle();
        this.timeOwned = production.getClockMilliseconds();
    }

    //---------------------------------------------------------------------------

    public JSONObject serialize() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("material", material.getMaterialID());
            jsonObject.put("cycleOwned", cycleOwned);
            jsonObject.put("timeOwned", timeOwned);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return jsonObject;
    }


    public static Item deserialize(JSONObject jsonObject, Block owner) {
        try {
            int materialID = jsonObject.getInt("material");
            Material material = Material.getMaterial(materialID);
            Item item = new Item(material);
            item.owner = owner;
            item.cycleOwned = jsonObject.getLong("cycleOwned");
            item.timeOwned = jsonObject.getLong("timeOwned");
            return item;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


}
