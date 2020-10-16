package com.axiom.operatio.model.market;

import com.axiom.operatio.model.materials.Item;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.warehouse.Warehouse;

/**
 * Модель рынка
 */
public class Market {

    // TODO Buy Order / Sell Order

    public Market(Warehouse warehouse) {

    }

    public boolean sellOrder(Material material, int quantity, long price) {
        return false;
    }

    public boolean buyOrder(Material material, int quantity, long price) {
        return false;
    }

    public void process() {

    }


}
