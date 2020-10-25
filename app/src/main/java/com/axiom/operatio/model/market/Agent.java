package com.axiom.operatio.model.market;

import com.axiom.operatio.model.materials.Item;

public class Agent {

    private Market market;

    public Agent(Market market) {
        this.market = market;
    }

    public void process() {

    }

    public int getID() {
        return 0;
    }

    public Item peek() {
        return null;
    }

    public Item poll() {
        return null;
    }

    public void push(Item item) {

    }

}
