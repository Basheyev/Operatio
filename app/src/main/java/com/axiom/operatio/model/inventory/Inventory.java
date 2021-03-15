package com.axiom.operatio.model.inventory;

import com.axiom.atom.engine.data.Channel;
import com.axiom.operatio.model.common.JSONSerializable;
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

    public static final int AUTO_NONE = 0;          //     0000
    public static final int AUTO_BUY = 1;           //     0001
    public static final int AUTO_SELL = 2;          //     0010

    protected Production production;
    protected ArrayList<Channel<Item>> stockKeepingUnit;
    private int[] autoAction;

    public Inventory(Production production) {
        this.production = production;
        int materialsAmount = Material.getMaterialsAmount();
        stockKeepingUnit = new ArrayList<>(SKU_COUNT);
        for (int i=0; i<materialsAmount; i++) {
            Channel<Item> sku = new Channel<Item>(MAX_SKU_CAPACITY);
            stockKeepingUnit.add(sku);
        }
        autoAction = new int[SKU_COUNT];
        Arrays.fill(autoAction, AUTO_NONE);
    }

    public Inventory(Production production, JSONObject jsonObject) {
        try {
            this.production = production;
            int materialsAmount = Material.getMaterialsAmount();
            stockKeepingUnit = new ArrayList<>();
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
            JSONArray autoState = jsonObject.optJSONArray("autoAction");
            autoAction = new int[SKU_COUNT];
            Arrays.fill(autoAction, AUTO_NONE);
            if (autoState!=null) {
                for (int i = autoState.length() - 1; i >= 0; i--) {
                    autoAction[i] = autoState.getInt(i);
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
        boolean stored = stockKeepingUnit.get(ID).add(item);
        if (stored) {
           // Ledger ledger = production.getLedger();
           // ledger.registerCommodityPushed(ID, 1);
        }
        return stored;
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
        Item item = stockKeepingUnit.get(ID).poll();
        if (item!=null) {
         //   Ledger ledger = production.getLedger();
         //   ledger.registerCommodityPolled(ID, 1);
        }
        return item;
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
        if (state) {
            autoAction[sku] |= AUTO_BUY;
        } else {
            autoAction[sku] &= ~AUTO_BUY;
        }
    }

    public boolean isAutoBuy(int sku) {
        return (autoAction[sku] & AUTO_BUY) > 0;
    }

    public void setAutoSell(int sku, boolean state) {
        if (state) {
            autoAction[sku] |= AUTO_SELL;
        } else {
            autoAction[sku] &= ~AUTO_SELL;
        }
    }

    public boolean isAutoSell(int sku) {
        return (autoAction[sku] & AUTO_SELL) > 0;
    }

    public void process() {
        Market market = production.getMarket();

        for (int i=0; i<SKU_COUNT; i++) {
            if (isAutoBuy(i)) {
                if (stockKeepingUnit.get(i).size() <= BATCH_SIZE) {
                    market.buyOrder(this, i, BATCH_SIZE);
                }
            } else if (isAutoSell(i)) {
                if (stockKeepingUnit.get(i).size() > BATCH_SIZE) {
                    market.sellOrder(this, i, BATCH_SIZE);
                }
            }
        }

    }

    /**
     * Возвращает стоимость хранимых материалов
     * @return стоимость всех хранимых материалов
     */
    public double getValuation() {
        double sum = 0;
        for (int i=0; i<stockKeepingUnit.size(); i++) {
            sum += stockKeepingUnit.get(i).size() * production.getMarket().getValue(i);
        }
        return sum;
    }


    public JSONObject toJSON() {
        try {
            JSONObject jsonObject = new JSONObject();
            JSONArray skuBalanceJson = new JSONArray();
            JSONArray skuAutoState = new JSONArray();
            for (int i=0; i<stockKeepingUnit.size(); i++) {
                skuBalanceJson.put(stockKeepingUnit.get(i).size());
                skuAutoState.put(autoAction[i]);
            }
            jsonObject.put("class", "Inventory");
            jsonObject.put("stockKeepingUnit", skuBalanceJson);
            jsonObject.put("autoAction", skuAutoState);
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
