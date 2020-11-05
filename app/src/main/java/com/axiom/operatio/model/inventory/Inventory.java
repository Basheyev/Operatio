package com.axiom.operatio.model.inventory;

import com.axiom.atom.engine.data.Channel;
import com.axiom.atom.engine.data.JSONSerializable;
import com.axiom.operatio.model.materials.Item;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.Production;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Модель склада материалов
 * TODO Добавить экономику: цена хранения
 */
public class Inventory implements JSONSerializable {

    public static final int MAX_SKU_CAPACITY = 999;

    protected ArrayList<Channel<Item>> stockKeepingUnit;


    public Inventory() {
        int materialsAmount = Material.getMaterialsAmount();
        stockKeepingUnit = new ArrayList<Channel<Item>>();
        for (int i=0; i<materialsAmount; i++) {
            Channel<Item> sku = new Channel<Item>(MAX_SKU_CAPACITY);
            stockKeepingUnit.add(sku);
        }
        Material[] materials = Material.getMaterials();
        for (int i=0; i<materialsAmount; i++) {
            Channel<Item> sku = stockKeepingUnit.get(i);
            if (!materials[i].getName().equals("reserved") && i < 8) {
                for (int j = 0; j < 300; j++) {
                    sku.add(new Item(materials[i]));
                }
            }
        }

    }


    public Inventory(JSONObject jsonObject) {
        try {
            int materialsAmount = Material.getMaterialsAmount();
            stockKeepingUnit = new ArrayList<Channel<Item>>();
            for (int i=0; i<materialsAmount; i++) {
                Channel<Item> sku = new Channel<Item>(MAX_SKU_CAPACITY);
                stockKeepingUnit.add(sku);
            }
            JSONArray jsonArray = jsonObject.getJSONArray("stockKeepingUnit");
            Material[] materials = Material.getMaterials();
            for (int i = jsonArray.length() - 1; i >= 0; i--) {
                Channel<Item> sku = stockKeepingUnit.get(i);
                int skuBalance = jsonArray.getInt(i);
                for (int j=0; j<skuBalance; j++) {
                    sku.add(new Item(materials[i]));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * Положить предмет на склад
     * @param item предмет
     * @return true если положили, false если места нет
     */
    public boolean push(Item item) {
        if (item==null) return false;
        int ID = item.getMaterial().getMaterialID();
        return stockKeepingUnit.get(ID).add(item);
    }

    /**
     * Вернуть предмет требуемого материала со склада, но не забирать
     * @param material тип материала
     * @return предмет указанного материала
     */
    public Item peek(Material material) {
        if (material==null) return null;
        int ID = material.getMaterialID();
        return stockKeepingUnit.get(ID).peek();
    }

    /**
     * Забрать предмет требуемого материала со склада
     * @param material тип материала
     * @return предмет указанного материала
     */
    public Item poll(Material material) {
        if (material==null) return null;
        int ID = material.getMaterialID();
        return stockKeepingUnit.get(ID).poll();
    }


    /**
     * Возвращает остатки по позиции на складе
     * @param material тип материала
     * @return количество единиц (остатки)
     */
    public int getBalance(Material material) {
        if (material==null) return 0;
        int ID = material.getMaterialID();
        return stockKeepingUnit.get(ID).size();
    }


    public void process() {
        
    }

    public JSONObject serialize() {
        try {
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            for (int i=0; i<stockKeepingUnit.size(); i++) {
                jsonArray.put(stockKeepingUnit.get(i).size());
            }
            jsonObject.put("class", "Inventory");
            jsonObject.put("stockKeepingUnit", jsonArray);
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
