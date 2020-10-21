package com.axiom.operatio.model.production.buffer;

import com.axiom.operatio.model.inventory.Inventory;
import com.axiom.operatio.model.materials.Item;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.block.Block;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Экспортер материалов на склад
 */
public class ExportBuffer extends Block {

    public ExportBuffer(Production production) {
        super(production, Block.NONE, 1, Block.NONE, 1);
        renderer = new ExportBufferRenderer(this);
    }

    public ExportBuffer(Production production, JSONObject jsonObject) {
        super(production, Block.NONE, 1, Block.NONE, 1);
        renderer = new ExportBufferRenderer(this);
        deserializeCommonFields(this, jsonObject);
    }


    @Override
    public boolean push(Item item) {
        Inventory inventory = Inventory.getInstance();
        return inventory.push(item);
    }

    @Override
    public Item peek() {
        return null;
    }

    @Override
    public Item poll() {
        return null;
    }

    @Override
    public void process() {

    }

    @Override
    public void setOutputDirection(int outDir) {
        super.setOutputDirection(NONE);
    }

    @Override
    public void setInputDirection(int inDir) {
        super.setInputDirection(NONE);
    }

    @Override
    public void setDirections(int inDir, int outDir) {
        super.setDirections(NONE, NONE);
    }

    @Override
    public int getItemsAmount() {
        return 0;
    }

    @Override
    public int getCapacity() {
        return 1;
    }

    @Override
    public void adjustFlowDirection() {

    }

    @Override
    public JSONObject serialize() {
        JSONObject jsonObject = super.serialize();
        try {
            jsonObject.put("class", "ImportBuffer");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return jsonObject;
    }
}
