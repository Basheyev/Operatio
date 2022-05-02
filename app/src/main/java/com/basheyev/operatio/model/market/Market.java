package com.basheyev.operatio.model.market;

import com.basheyev.operatio.model.common.JSONSerializable;
import com.basheyev.operatio.model.inventory.Inventory;
import com.basheyev.operatio.model.ledger.Ledger;
import com.basheyev.operatio.model.materials.Item;
import com.basheyev.operatio.model.materials.Material;
import com.basheyev.operatio.model.production.Production;

import org.json.JSONObject;

/**
 * Модель рынка - симуляция экономических циклов и колебаний
 */
public class Market implements JSONSerializable {

    public static final int HISTORY_LENGTH = Ledger.HISTORY_LENGTH * 3;

    private final CommodityMarket[] commodityMarket;
    private final Production production;
    protected long lastCycleTime;            // Время последнего цикла (миллисекунды)

    public Market(Production production) {
        this.production = production;
        commodityMarket = new CommodityMarket[Material.COUNT];
        Material material;
        for (int i=0; i<Material.COUNT; i++) {
            material = Material.getMaterial(i);
            commodityMarket[i] = new CommodityMarket(material.getPrice(), 1000, 1000);
        }
    }


    public synchronized void process() {
        long cycleMilliseconds = production.getCycleMilliseconds() * 3;  // Длительносить цикла (миллисекунды)
        long now = production.getClock();
        if (now - lastCycleTime < cycleMilliseconds) return;
        for (int i = 0; i < Material.COUNT; i++) commodityMarket[i].process();
        lastCycleTime = now;
    }

    public synchronized double getDemand(int commodity) {
        return commodityMarket[commodity].getDemand();
    }

    public synchronized double getSupply(int commodity) {
        return commodityMarket[commodity].getSupply();
    }

    public synchronized double getValue(int commodity) {
        return commodityMarket[commodity].getMarketValue();
    }

    public synchronized double getFaceValue(int commodity) {
        return commodityMarket[commodity].getFaceValue();
    }


    public synchronized void buyOrder(Inventory inventory, int commodity, int amount) {
        double commodityPrice = getValue(commodity);
        int quantity = 0;
        int expenseType = Ledger.EXPENSE_MATERIAL_BOUGHT;
        for (int i=0; i < amount; i++) {
            if (!production.getLedger().creditCashBalance(expenseType, commodityPrice)) break;
            inventory.push(new Item(Material.getMaterial(commodity)));
            quantity++;
        }
        if (quantity > 0) {
            production.getLedger().materialBought(commodity, quantity, commodityPrice);
        }
    }

    public synchronized int sellOrder(Inventory inventory, int commodity, int amount) {
        Item item;
        double commodityPrice = getValue(commodity);
        int quantity = 0;
        int incomeType = Ledger.REVENUE_MATERIAL_SOLD;
        Material material = Material.getMaterial(commodity);
        if (inventory.getBalance(material)==0) return 0;
        for (int i=0; i < amount; i++) {
            item = inventory.peek(material);
            if (item==null) break;
            inventory.poll(material);
            production.getLedger().debitCashBalance(incomeType, commodityPrice);
            quantity++;
        }
        if (quantity > 0) {
            production.getLedger().materialSold(commodity, quantity, commodityPrice);
        }
        return quantity;
    }

    public synchronized int getHistoryLength(int commodity) {
        return commodityMarket[commodity].getHistoryLength();
    }

    public synchronized double getHistoryMaxValue(int commodity) {
        return commodityMarket[commodity].getHistoryMaxValue();
    }

    public synchronized void getHistoryValues(int commodity, double[] destination) {
        commodityMarket[commodity].getHistoryValues(destination);
    }

    @Override
    public JSONObject toJSON() {
        return null; // Не сериализуем, так как практического смысла прошлые данные не несут
    }

}
