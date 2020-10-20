package com.axiom.operatio.model.materials;

import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.block.Block;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Содержит информацию о предмете
 */
public class Item {

    protected Material material;            // Материал
    protected Block owner;                  // Блок владелец
    protected long cycleOwned;              // Цикл производства (захват)
    protected long timeOwned;               // Время в миллисекундах (захват)

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

    public void setOwner(Block owner) {
        this.owner = owner;
        this.cycleOwned = Production.getCurrentCycle();
        this.timeOwned = Production.getClockMilliseconds();
    }

    //---------------------------------------------------------------------------

    public JSONObject serialize() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("material", material.getMaterialID());
            jsonObject.put("owner", owner.getID());
            jsonObject.put("cycleOwned", cycleOwned);
            jsonObject.put("timeOwned", timeOwned);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return jsonObject;
    }


}
