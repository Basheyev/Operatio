package com.basheyev.operatio.model.production.buffer;

import com.basheyev.operatio.model.common.JSONSerializable;
import com.basheyev.operatio.model.inventory.Inventory;
import com.basheyev.operatio.model.materials.Item;
import com.basheyev.operatio.model.materials.Material;
import com.basheyev.operatio.model.production.Production;
import com.basheyev.operatio.model.production.block.Block;
import com.basheyev.operatio.model.production.block.BlockBuilder;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Экспортер материалов на склад
 */
public class ExportBuffer extends Block implements JSONSerializable {

    public static final double CYCLE_COST = 0.01d;
    public static final int PRICE = 1000;
    public static final int NO_KEEPING_UNIT = -1;

    private BufferKeepingUnit[] currentStats;
    private BufferKeepingUnit[] exportStats;

    public ExportBuffer(Production production) {
        super(production, Block.NONE, 1, Block.NONE, 1);
        initializeStatsBuffers();
        price = PRICE;
        renderer = new ExportBufferRenderer(this);
    }

    public ExportBuffer(Production production, JSONObject jsonObject) {
        super(production, Block.NONE, 1, Block.NONE, 1);
        initializeStatsBuffers();
        price = PRICE;
        BlockBuilder.parseCommonFields(this, jsonObject);
        renderer = new ExportBufferRenderer(this);
    }

    private void initializeStatsBuffers() {
        currentStats = new BufferKeepingUnit[4];
        exportStats = new BufferKeepingUnit[4];
        for (int i=0; i<4; i++) {
            currentStats[i] = new BufferKeepingUnit();
            exportStats[i] = new BufferKeepingUnit();
        }
    }


    @Override
    public boolean push(Item item) {
        Inventory inventory = production.getInventory();
        boolean pushed = inventory.push(item);
        if (pushed) {
            item.resetOwner();
            int bkuIndex = getMaterialKeepingUnit(item.getMaterial());
            if (bkuIndex==NO_KEEPING_UNIT) return true;
            currentStats[bkuIndex].total++;
        }
        return pushed;
    }

    /**
     * Возвращает номер BKU (ячейки) учёта
     * @param material материал
     * @return возвращает номер ячейки или -1 если такой ячейки нет
     */
    private int getMaterialKeepingUnit(Material material) {
        for (int i=0; i<4; i++) {
            // Если такого материала нет и есть свободные ячейки - создаём такую ячейку
            if (currentStats[i].material==null) {
                currentStats[i].material = material;
                currentStats[i].total = 0;
                return i;
                // Если ячейка с таким материалом есть, то возвращаем её номер
            } else if (currentStats[i].material.equals(material)) {
                return i;
            }
        }
        // Говорим что такой ячейки нет
        return -1;
    }


    public Material getKeepingUnitMaterial(int id) {
        if (id < 0 || id > 3) return null;
        return exportStats[id].material;
    }

    public int getKeepingUnitTotal(int id) {
        if (id < 0 || id > 3) return NO_KEEPING_UNIT;
        return exportStats[id].total;
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
        for (int i=0; i<4; i++) {
            exportStats[i].total = currentStats[i].total;
            currentStats[i].total = 0;
            exportStats[i].material = currentStats[i].material;
            currentStats[i].material = null;
        }
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

    @Override
    public String getDescription() {
        return "Sends items to the warehouse";
    }
}
