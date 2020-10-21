package com.axiom.operatio.model.production.buffer;

import com.axiom.operatio.model.materials.Item;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.model.inventory.Inventory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Импортер материалов со склада на производство
 */
public class ImportBuffer extends Block {

    protected Material importMaterial;

    public ImportBuffer(Production production, Material material) {
        super(production, Block.NONE, 1, Block.NONE, 4);
        renderer = new ImportBufferRenderer(this);
        importMaterial = material;

    }

    public ImportBuffer(Production production, JSONObject jsonObject) {
        super(production, Block.NONE, 1, Block.NONE, 4);
        deserializeCommonFields(this, jsonObject);
        renderer = new ImportBufferRenderer(this);
        try {
            importMaterial = Material.getMaterial(jsonObject.getInt("material"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean setImportMaterial(Material material) {
        if (material==null) return false;
        importMaterial = material;
        return true;
    }

    public Material getImportMaterial() {
        return importMaterial;
    }

    @Override
    public Item peek() {
        Inventory inventory = Inventory.getInstance();
        return inventory.peek(importMaterial);
    }

    @Override
    public Item poll() {
        Inventory inventory = Inventory.getInstance();
        return inventory.poll(importMaterial);
    }


    @Override
    public boolean push(Item item) {
        return false;
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
    public JSONObject serialize() {
        JSONObject jsonObject = super.serialize();
        try {
            jsonObject.put("class", "ImportBuffer");
            jsonObject.put("material", importMaterial.getMaterialID());
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return jsonObject;
    }

}
