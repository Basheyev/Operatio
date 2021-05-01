package com.axiom.operatio.model.inventory;

import com.axiom.atom.engine.data.structures.Channel;
import com.axiom.operatio.model.common.JSONSerializable;
import com.axiom.operatio.model.ledger.Ledger;
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

    public static final int MAX_SKU_CAPACITY = 999;
    public static final int DEFAULT_QUANTITY = 20;

    public static final int AMOUNT_BIT_SHIFT = 16;

    public static final int AUTO_NONE = 0;          //     0000
    public static final int AUTO_BUY = 1;           //     0001
    public static final int AUTO_SELL = 2;          //     0010

    protected Production production;
    protected ArrayList<Channel<Item>> stockKeepingUnit;
    private int[] contractParameters;
    private long previousDay;

    public Inventory(Production production) {
        this.production = production;
        int materialsAmount = Material.getMaterialsAmount();
        stockKeepingUnit = new ArrayList<>(Material.COUNT);
        for (int i=0; i<materialsAmount; i++) {
            Channel<Item> sku = new Channel<Item>(MAX_SKU_CAPACITY);
            stockKeepingUnit.add(sku);
        }
        contractParameters = new int[Material.COUNT];
        Arrays.fill(contractParameters, AUTO_NONE);
        previousDay = production.getCurrentCycle() / Ledger.OPERATIONAL_DAY_CYCLES;
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
            contractParameters = new int[Material.COUNT];
            Arrays.fill(contractParameters, AUTO_NONE);
            if (autoState!=null) {
                for (int i = autoState.length() - 1; i >= 0; i--) {
                    contractParameters[i] = autoState.getInt(i);
                    if (getContractQuantity(i)==0) setContractQuantity(i, DEFAULT_QUANTITY);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        previousDay = production.getCurrentCycle() / Ledger.OPERATIONAL_DAY_CYCLES;
    }


    /**
     * Положить предмет на склад
     * @param item предмет
     * @return true если положили, false если места нет
     */
    public boolean push(Item item) {
        if (item==null) return false;
        int ID = item.getMaterial().getID();
        boolean stored = stockKeepingUnit.get(ID).add(item);
        return stored;
    }

    /**
     * Вернуть предмет требуемого материала со склада, но не забирать
     * @param material тип материала
     * @return предмет указанного материала
     */
    public Item peek(Material material) {
        if (material==null) return null;
        int ID = material.getID();
        return stockKeepingUnit.get(ID).peek();
    }

    /**
     * Забрать предмет требуемого материала со склада
     * @param material тип материала
     * @return предмет указанного материала
     */
    public Item poll(Material material) {
        if (material==null) return null;
        int ID = material.getID();
        Item item = stockKeepingUnit.get(ID).poll();
        return item;
    }


    /**
     * Возвращает остатки по позиции на складе
     * @param material тип материала
     * @return количество единиц (остатки)
     */
    public int getBalance(Material material) {
        if (material==null) return 0;
        int ID = material.getID();
        return stockKeepingUnit.get(ID).size();
    }

    public void signPurchaseContract(int materialID, boolean signed) {
        if (signed) {
            contractParameters[materialID] |= AUTO_BUY;
        } else {
            contractParameters[materialID] &= ~AUTO_BUY;
        }
    }

    public boolean isPurchaseContract(int materialID) {
        return (contractParameters[materialID] & AUTO_BUY) > 0;
    }

    public void signSalesContract(int materialID, boolean signed) {
        if (signed) {
            contractParameters[materialID] |= AUTO_SELL;
        } else {
            contractParameters[materialID] &= ~AUTO_SELL;
        }
    }

    public boolean isSalesContract(int sku) {
        return (contractParameters[sku] & AUTO_SELL) > 0;
    }

    public int setContractQuantity(int materialID, int quantity) {
        int current = contractParameters[materialID] & 0x0F;     // Очистим параметры от количества
        int amount = (quantity & 0xFFFF) << AMOUNT_BIT_SHIFT;    // Запишем количество в верхние биты
        contractParameters[materialID] = amount | current;       // Записываем итоговое значение
        return getContractQuantity(materialID);
    }


    public int getContractQuantity(int materialID) {
        return (contractParameters[materialID] >> AMOUNT_BIT_SHIFT) & 0xFFFF;
    }


    public void process() {
        Market market = production.getMarket();

        long currentDay = production.getCurrentCycle() / Ledger.OPERATIONAL_DAY_CYCLES;

        if (currentDay <= previousDay) return;

        for (int i = 0; i<Material.COUNT; i++) {
            int contractQuantity = getContractQuantity(i);
            if (isPurchaseContract(i)) {
                market.buyOrder(this, i, contractQuantity);
            } else if (isSalesContract(i)) {
                market.sellOrder(this, i, contractQuantity);
                // todo если не хватает, то неисполнение контракта
            }
        }

        previousDay = currentDay;

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
                skuAutoState.put(contractParameters[i]);
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
