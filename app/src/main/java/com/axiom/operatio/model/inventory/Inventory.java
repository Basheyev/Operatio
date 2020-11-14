package com.axiom.operatio.model.inventory;

import com.axiom.atom.engine.data.Channel;
import com.axiom.atom.engine.data.JSONSerializable;
import com.axiom.operatio.model.market.Market;
import com.axiom.operatio.model.materials.Item;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.Production;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Модель склада материалов
 */
public class Inventory implements JSONSerializable {

    public static final int SKU_COUNT = 64;
    public static final int MAX_SKU_CAPACITY = 999;
    public static final int BATCH_SIZE = 20;

    protected ArrayList<Channel<Item>> stockKeepingUnit;
    private boolean[] isAutoBuy, isAutoSell; // todo serialize auto buy/sell states;

    public Inventory() {
        int materialsAmount = Material.getMaterialsAmount();
        stockKeepingUnit = new ArrayList<>(SKU_COUNT);
        for (int i=0; i<materialsAmount; i++) {
            Channel<Item> sku = new Channel<Item>(MAX_SKU_CAPACITY);
            stockKeepingUnit.add(sku);
        }
        isAutoBuy = new boolean[SKU_COUNT];
        isAutoSell = new boolean[SKU_COUNT];
        Arrays.fill(isAutoBuy, false);
        Arrays.fill(isAutoSell, false);
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
            isAutoBuy = new boolean[SKU_COUNT];
            isAutoSell = new boolean[SKU_COUNT];
            Arrays.fill(isAutoBuy, false);
            Arrays.fill(isAutoSell, false);
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

    public void setAutoBuy(int sku, boolean state) {
        isAutoBuy[sku] = state;
    }

    public boolean getAutoBuy(int sku) {
        return isAutoBuy[sku];
    }

    public void setAutoSell(int sku, boolean state) {
        isAutoSell[sku] = state;
    }

    public boolean getAutoSell(int sku) {
        return isAutoSell[sku];
    }

    public void process(Production production) {
        Market market = production.getMarket();

        for (int i=0; i<SKU_COUNT; i++) {

            // TODO Добавить экономику: цена хранения

            if (isAutoBuy[i]) {
                if (stockKeepingUnit.get(i).size() <= BATCH_SIZE) {
                    market.buyOrder(this, i, BATCH_SIZE);
                }
            } else if (isAutoSell[i]) {
                if (stockKeepingUnit.get(i).size() > BATCH_SIZE) {
                    market.sellOrder(this, i, BATCH_SIZE);
                }
            }
        }

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
