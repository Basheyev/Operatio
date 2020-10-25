package com.axiom.operatio.model.market;

import com.axiom.atom.engine.data.Channel;
import com.axiom.operatio.model.materials.Item;
import com.axiom.operatio.model.materials.Material;

/**
 *  TODO Сначала реализовать простую имитацию агенда который всё продаёт и всё покупает
 */
public class Agent {

    private static int agentsCounter = 0;
    private long cash = 300;
    private int agentID;

    private Channel<Item> items;

    public Agent() {
        agentsCounter++;
        agentID = agentsCounter;
        items = new Channel<>();
        switch (agentID) {
            case 1:
                for (int i=0; i<10; i++) items.add(new Item(Material.getMaterial(0)));
                break;
            case 2:

                break;
        }

    }

    public void process(Market market) {
        switch (agentID) {
            case 1:
                market.orderSell(this, 0, 16, 5);
                //market.orderBuy(this, 1, 10, 8);
                break;
            case 2:
                //market.orderSell(this,1, 100, 8);
                market.orderBuy(this, 0, 7, 5);
                break;
        }
    }

    public int getID() {
        return agentID;
    }

    public Item peek() {
        return items.peek();
    }

    public Item poll() {
        return items.poll();
    }

    public boolean push(Item item) {
        return items.add(item);
    }

    public void increaseCash(long sum) {
        cash += sum;
    }

    public boolean decreaseCash(long sum) {
        long nextBalance = cash - sum;
        if (nextBalance < 0) return false;
        cash -= sum;
        return true;
    }

}
