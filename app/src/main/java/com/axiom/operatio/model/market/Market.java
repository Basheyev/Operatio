package com.axiom.operatio.model.market;

import com.axiom.operatio.model.materials.Material;

import java.io.Serializable;

/**
 * Модель рынка
 */
public class Market implements Serializable {

    // TODO Buy Order / Sell Order - правила покупки и продажи
    protected static Market market;
    protected static boolean initialized;

    public static Market getInstance() {
        if (!initialized) market = new Market();
        return market;
    }

    private Market() {
        initialized = true;
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
