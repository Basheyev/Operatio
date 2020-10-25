package com.axiom.operatio.model.market;

import com.axiom.atom.engine.data.Channel;
import com.axiom.atom.engine.data.JSONSerializable;
import com.axiom.operatio.model.materials.Material;

import org.json.JSONObject;

/**
 * Модель рынка
 * TODO Buy Order / Sell Order - правила покупки и продажи
 */
public class Market implements JSONSerializable {

    public static final int AGENTS_COUNT = 64;
    public static final int ORDERS_COUNT = 1024;

    private Channel<Agent> agents;
    private Channel<Order> orders;

    public Market() {
        agents = new Channel<>(AGENTS_COUNT);
        orders = new Channel<>(ORDERS_COUNT);
    }


    public void process() {

        for (int i=0; i<agents.size(); i++) {
            Agent agent = agents.get(i);
            if (agent!=null) agent.process();
        }

        processOrders();

    }


    protected void processOrders() {


    }


    public void clearOrders() {
        orders.clear();
    }

    public Order orderBuy(Agent agent, Material material, int quantity, int price) {
        if (agent==null || material==null || quantity <= 0 || price <= 0) return null;
        Order order = new Order();
        order.agent = agent.getID();
        order.type = Order.BUY;
        order.materialID = material.getMaterialID();
        order.quantity = quantity;
        order.price = price;
        return orders.add(order) ? order : null;
    }

    public Order orderSell(Agent agent, Material material, int quantity, int price) {
        if (agent==null || material==null || quantity <= 0 || price <= 0) return null;
        Order order = new Order();
        order.agent = agent.getID();
        order.type = Order.SELL;
        order.materialID = material.getMaterialID();
        order.quantity = quantity;
        order.price = price;
        return orders.add(order) ? order : null;
    }

    public boolean deleteOrder(Order order) {
        return orders.remove(order);
    }


    public boolean addAgent(Agent agent) {
        return agents.add(agent);
    }

    public boolean removeAgent(Agent agent) {
        return agents.remove(agent);
    }


    @Override
    public JSONObject serialize() {
        return null;
    }
}
