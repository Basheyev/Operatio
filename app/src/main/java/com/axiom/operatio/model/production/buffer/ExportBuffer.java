package com.axiom.operatio.model.production.buffer;

import com.axiom.operatio.model.common.JSONSerializable;
import com.axiom.operatio.model.inventory.Inventory;
import com.axiom.operatio.model.materials.Item;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.model.production.block.BlockBuilder;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Экспортер материалов на склад
 */
public class ExportBuffer extends Block implements JSONSerializable {

    public static final double CYCLE_COST = 0.01d;
    public static final int PRICE = 1000;

    public ExportBuffer(Production production) {
        super(production, Block.NONE, 1, Block.NONE, 1);
        price = PRICE;
        renderer = new ExportBufferRenderer(this);
    }

    public ExportBuffer(Production production, JSONObject jsonObject) {
        super(production, Block.NONE, 1, Block.NONE, 1);
        price = PRICE;
        BlockBuilder.parseCommonFields(this, jsonObject);
        renderer = new ExportBufferRenderer(this);
    }


    @Override
    public boolean push(Item item) {
        Inventory inventory = production.getInventory();
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
    public double getCycleCost() {
        return CYCLE_COST;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jsonObject = super.toJSON();
        try {
            jsonObject.put("class", "ExportBuffer");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return jsonObject;
    }
}
