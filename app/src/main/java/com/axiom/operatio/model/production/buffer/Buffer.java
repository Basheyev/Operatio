package com.axiom.operatio.model.production.buffer;

import com.axiom.atom.engine.data.JSONSerializable;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.model.materials.Item;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Представляет собой мини-склад, который может хранить до 4 видов материалов
 * TODO Добавить экономику: цена хранения
 */
public class Buffer extends Block implements JSONSerializable {

    public static final int PRICE = 400;
    public static final int NO_KEEPING_UNIT = -1;      // Константа отсутствия такой ячейки хранения
    protected BufferKeepingUnit[] bufferKeepingUnit;   // Ячейки хранения материалов

    public Buffer(Production production, int capacity) {
        super(production, Block.NONE, capacity, Block.NONE, 1);
        price = PRICE;
        renderer = new BufferRenderer(this);
        // Сформировать список материалов который может хранить (null - любой)
        bufferKeepingUnit = new BufferKeepingUnit[4];
        for (int i=0; i<4; i++) {
            bufferKeepingUnit[i] = new BufferKeepingUnit();
            bufferKeepingUnit[i].material = null;
            bufferKeepingUnit[i].capacity = capacity / 4;
            bufferKeepingUnit[i].total = 0;
        }
    }


    public Buffer(Production production, JSONObject jsonObject, int capacity) {
        super(production, Block.NONE, capacity, Block.NONE, 1);
        price = PRICE;
        deserializeCommonFields(this, jsonObject);
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("bufferKeepingUnit");
            bufferKeepingUnit = new BufferKeepingUnit[4];
            for (int i=0; i<4; i++) {
                bufferKeepingUnit[i] = BufferKeepingUnit.deserialize(jsonArray.getJSONObject(i));
            }
            renderer = new BufferRenderer(this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Добавляет предмет во входную очередь буфера
     * @param item предмет
     * @return true - если блок принял предмет, false - если нет
     */
    public boolean push(Item item) {
        if (item==null) return false;
        if (state==BUSY) return false;
        if (input.size()>=inputCapacity) return false;

        // Узнаем есть ли ячейка под такой материал
        int materialBKU = getMaterialKeepingUnit(item.getMaterial());
        // Если её нет и создать её нельзя - уходим
        if (materialBKU == NO_KEEPING_UNIT) return false;

        BufferKeepingUnit bku = bufferKeepingUnit[materialBKU];
        // Если для добавления предмета в ячейке нет места уходим
        if (bku.total >= bku.capacity) return false;

        bku.total++;
        item.setOwner(production, this);
        input.add(item);
        return true;
    }

    @Override
    public Item peek() {
        return input.peek();
    }


    public Item peek(Material material) {
        if (input.size()==0) return null;
        for (int i=input.size()-1; i>=0; i--) {
            Item item = input.get(i);
            if (item.getMaterial().equals(material)) {
                return item;
            }
        }
        return null;
    }


    @Override
    public Item poll() {
        Item item = input.peek();
        if (item==null) return null;
        item = input.poll();
        if (item!=null) {
            int keepingUnitID = getMaterialKeepingUnit(item.getMaterial());
            bufferKeepingUnit[keepingUnitID].total--;
            return item;
        }
        return null;
    }


    public Item poll(Material material) {
        if (input.size()==0) return null;
        for (int i=input.size()-1; i>=0; i--) {
            Item item = input.get(i);
            if (item.getMaterial().equals(material)) {
                int keepingUnitID = getMaterialKeepingUnit(item.getMaterial());
                input.remove(item);
                bufferKeepingUnit[keepingUnitID].total--;
                return item;
            }
        }
        return null;
    }

    @Override
    public void process() {
       // do nothing
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

    //------------------------------------------------------------------------------------------

    public boolean setBufferKeepingUnit(int index, Material material, int capacity, int total) {
        if (index < 0 || index > 3 || material==null || capacity < 0 || total < 0) return false;
        bufferKeepingUnit[index].material = material;
        bufferKeepingUnit[index].capacity = capacity;
        bufferKeepingUnit[index].total = total;
        return true;
    }

    public Material getKeepingUnitMaterial(int id) {
        if (id < 0 || id > 3) return null;
        return bufferKeepingUnit[id].material;
    }

    public int getKeepingUnitTotal(int id) {
        if (id < 0 || id > 3) return NO_KEEPING_UNIT;
        return bufferKeepingUnit[id].total;
    }

    /**
     * Возвращает номер BKU (ячейки) хранения
     * @param material материал
     * @return возвращает номер ячейки или -1 если такой ячейки нет
     */
    private int getMaterialKeepingUnit(Material material) {
        for (int i=0; i<4; i++) {
            // Если такого материала нет и есть свободные ячейки - создаём такую ячейку
            if (bufferKeepingUnit[i].material==null) {
                bufferKeepingUnit[i].material = material;
                bufferKeepingUnit[i].total = 0;
                return i;
            // Если ячейка с таким материалом есть, то возвращаем её номер
            } else if (bufferKeepingUnit[i].material.equals(material)) {
                return i;
            }
        }
        // Говорим что такой ячейки нет
        return NO_KEEPING_UNIT;
    }

    @Override
    public JSONObject serialize() {
        JSONObject jsonObject = super.serialize();
        try {
            jsonObject.put("class", "Buffer");
            JSONArray jsonArray = new JSONArray();
            for (int i=0; i<4; i++) {
                jsonArray.put(bufferKeepingUnit[i].serialize());
            }
            jsonObject.put("bufferKeepingUnit", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return jsonObject;
    }



}
