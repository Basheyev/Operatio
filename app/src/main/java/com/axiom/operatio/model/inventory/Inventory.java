package com.axiom.operatio.model.inventory;

import com.axiom.operatio.model.common.JSONSerializable;
import com.axiom.operatio.model.ledger.Ledger;
import com.axiom.operatio.model.materials.Item;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.Production;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.axiom.operatio.model.inventory.StockKeepingUnit.DEFAULT_QUANTITY;

/**
 * Модель склада материалов
 */
public class Inventory implements JSONSerializable {

    protected Production production;
    protected ArrayList<StockKeepingUnit> stockKeepingUnit;
    private long previousDay;

    public Inventory(Production production) {
        this.production = production;
        createStockKeepingUnits();
        previousDay = production.getCurrentDay();
    }

    public Inventory(Production production, JSONObject jsonObject) {
        try {
            this.production = production;
            createStockKeepingUnits();
            JSONArray jsonArray = jsonObject.getJSONArray("stockKeepingUnit");
            Material[] materials = Material.getMaterials();
            for (int i = jsonArray.length() - 1; i >= 0; i--) {
                StockKeepingUnit sku = stockKeepingUnit.get(i);
                int skuBalance = jsonArray.getInt(i);
                for (int j=0; j<skuBalance; j++) {
                    sku.push(new Item(materials[i]));
                }
            }
            JSONArray autoState = jsonObject.optJSONArray("autoAction");
            if (autoState!=null) {
                for (int i = autoState.length() - 1; i >= 0; i--) {
                    StockKeepingUnit sku = stockKeepingUnit.get(i);
                    sku.setContractParameters(autoState.getInt(i));
                    if (sku.getContractQuantity()==0) sku.setContractQuantity(DEFAULT_QUANTITY);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        previousDay = production.getCurrentDay();
    }


    private void createStockKeepingUnits() {
        int materialsAmount = Material.getMaterialsAmount();
        stockKeepingUnit = new ArrayList<>();
        for (int i=0; i<materialsAmount; i++) {
            stockKeepingUnit.add(new StockKeepingUnit(i));
        }
    }


    /**
     * Положить предмет на склад
     * @param item предмет
     * @return true если положили, false если места нет
     */
    public boolean push(Item item) {
        if (item==null) return false;
        int ID = item.getMaterial().getID();
        return stockKeepingUnit.get(ID).push(item);
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
        return stockKeepingUnit.get(ID).poll();
    }


    /**
     * Возвращает остатки по позиции на складе
     * @param material тип материала
     * @return количество единиц (остатки)
     */
    public int getBalance(Material material) {
        if (material==null) return 0;
        int ID = material.getID();
        return stockKeepingUnit.get(ID).getBalance();
    }


    public int setContractQuantity(int materialID, int quantity) {
        StockKeepingUnit sku = stockKeepingUnit.get(materialID);
        return sku.setContractQuantity(quantity);
    }


    public int getContractQuantity(int materialID) {
        StockKeepingUnit sku = stockKeepingUnit.get(materialID);
        return sku.getContractQuantity();
    }


    public void signPurchaseContract(int materialID, boolean signed) {
        StockKeepingUnit sku = stockKeepingUnit.get(materialID);
        sku.signPurchaseContract(signed);
    }

    public boolean hasPurchaseContract(int materialID) {
        StockKeepingUnit sku = stockKeepingUnit.get(materialID);
        return sku.hasPurchaseContract();
    }

    public void signSalesContract(int materialID, boolean signed) {
        StockKeepingUnit sku = stockKeepingUnit.get(materialID);
        sku.signSalesContract(signed);
    }

    public boolean hasSalesContract(int materialID) {
        StockKeepingUnit sku = stockKeepingUnit.get(materialID);
        return sku.hasSalesContract();
    }

    public int getDailyBalance(int materialID) {
        StockKeepingUnit sku = stockKeepingUnit.get(materialID);
        return sku.getDailyBalance();
    }

    public int getDailyOutOfStock(int materialID) {
        StockKeepingUnit sku = stockKeepingUnit.get(materialID);
        return sku.getDailyOutOfStock();
    }

    public void process() {
        Market market = production.getMarket();

        long currentDay = production.getCurrentCycle() / Ledger.OPERATIONAL_DAY_CYCLES;

        if (currentDay <= previousDay) return;

        for (int i = 0; i<Material.COUNT; i++) {
            StockKeepingUnit sku = stockKeepingUnit.get(i);
            int contractQuantity = sku.getContractQuantity();
            if (sku.hasPurchaseContract()) {
                market.buyOrder(this, i, contractQuantity);
            } else if (sku.hasSalesContract()) {
                market.sellOrder(this, i, contractQuantity);
                // todo если не хватает, то неисполнение контракта
            }

            sku.closeDailyStatistics();
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
            sum += stockKeepingUnit.get(i).getBalance() * production.getMarket().getValue(i);
        }
        return sum;
    }


    public JSONObject toJSON() {
        try {
            JSONObject jsonObject = new JSONObject();
            JSONArray skuBalanceJson = new JSONArray();
            JSONArray skuAutoState = new JSONArray();
            for (int i=0; i<stockKeepingUnit.size(); i++) {
                StockKeepingUnit sku = stockKeepingUnit.get(i);
                skuBalanceJson.put(sku.getBalance());
                skuAutoState.put(sku.getContractParameters());
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
