package com.axiom.operatio.model.market;

import android.util.Log;

import com.axiom.atom.engine.data.Channel;
import com.axiom.atom.engine.data.JSONSerializable;
import com.axiom.operatio.model.materials.Item;

import org.json.JSONObject;

import java.util.ArrayList;
/**
 * Модель рынка
 */
public class Market implements JSONSerializable {

    public static final int AGENTS_COUNT = 64;
    public static final int ORDERS_COUNT = 1024;
    public static final int MAX_ITEMS_PER_DEAL = 999;

    private ArrayList<Agent> agents;
    private ArrayList<Order> orders;
    private Channel<Item> deal;

    public Market() {
        agents = new ArrayList<>(AGENTS_COUNT);
        orders = new ArrayList<>(ORDERS_COUNT);
        deal = new Channel<Item>(MAX_ITEMS_PER_DEAL);
    }


    public void process() {
        processAgents();
        processOrders();
        clearClosedOrders();
    }


    protected void processAgents() {
        // Действие каждого агента
        for (int i=0; i<agents.size(); i++) {
            Agent agent = agents.get(i);
            if (agent!=null) agent.process(this);
        }
    }

    protected void processOrders() {
        // Обработка пар ордеров
        for (int i=0; i<orders.size(); i++) {
            for (int j=0; j<orders.size(); j++) {
                if (i != j) {                                   // Если это не один и тот же ордер
                    Order buy = orders.get(i);
                    Order sell = orders.get(j);
                    if (buy.type==Order.BUY                     // Если buy - ордер покупки
                        && sell.type==Order.SELL                // sell - ордер продажи
                        && buy.agent != sell.agent              // это не один и тот же агент
                        && buy.materialID==sell.materialID      // по одной и той же позиции
                        && buy.price >= sell.price) {           // цена покупки >= цены покупки
                        processDeal(buy,sell);                  // Обработать сделку
                    }
                }
            }
        }
        // Ордера обработаны
    }


    /**
     * Совершить сделку
     * @param buy заказ на покупку
     * @param sell заказ на продажу
     */
    protected void processDeal(Order buy, Order sell) {
        //----------------------------------------------------------------------------------------
        // Проверяем готовность продавца продать
        //----------------------------------------------------------------------------------------
        long quantity = Math.min(buy.quantity, sell.quantity); // Доступное количество для сделки
        long price = sell.price;                               // Цена - цена продажи
        deal.clear();                                          // Готовим буфер для товаров сделки
        for (int i=0; i<quantity; i++) {                       // Забираем у продавца товар
            if (sell.agent.peek()==null) break;                // Если продавец не может отдать
            deal.add(sell.agent.poll());                       // Забираем у продавца товар
        }
        //----------------------------------------------------------------------------------------
        // Проверяем готовность покупателя
        //----------------------------------------------------------------------------------------
        int sellerCanSell = deal.size();                       // Фактическое количество на продажу
        long dealSum = price * sellerCanSell;                  // Считаем сумму сделки
        if (!buy.agent.decreaseCash(dealSum)) {                // Списываем у покупателя деньги
            for (int i=0; i<sellerCanSell; i++) {              // если денег не достаточно
                sell.agent.push(deal.poll());                  // возвращаем товар продавцу
            }
            return;                                            // Отменяем сделку
        }
        //----------------------------------------------------------------------------------------
        // Закрываем сделку - отправляем товар покупатели и делаем взаиморасчёты
        //----------------------------------------------------------------------------------------
        int amount;
        for (amount=0; amount<sellerCanSell; amount++) {
            Item item = deal.poll();
            boolean retrieved = buy.agent.push(item);         // Пробуем поставить товар покупателю
            if (!retrieved) {                                 // Если поставить товар не получилось
                long gap = dealSum - (amount+1) * price;      // Делаем перерасчёт покупателю
                buy.agent.increaseCash(gap);                  // Возвращаем деньги
                sell.agent.push(item);                        // Остаток товаров возвращаем продавцу
                for (int j=amount+1; j<sellerCanSell; j++) {
                    sell.agent.push(deal.poll());
                }
                buy.quantity -= amount;                       // Уменьшаем в заявке купленное
                sell.quantity -= amount;                      // Уменьшаем в заявке проданное
                sell.agent.increaseCash(amount * price); // Зачислаем деньги продавцу
                return;
            }
        }
        //----------------------------------------------------------------------------------------
        // Если всё прошло хорошо
        //----------------------------------------------------------------------------------------
        buy.quantity -= amount;                              // Уменьшаем в заявке купленное
        sell.quantity -= amount;                             // Уменьшаем в заявке проданное
        sell.agent.increaseCash(dealSum);                    // Зачислаем деньги продавцу

        if (buy.quantity==0) buy.type = Order.CLOSED;        // Если заявка выполнена - закрываем
        if (sell.quantity==0) sell.type = Order.CLOSED;      // Если заявка выполнена - закрываем

        if (amount>0) {
            String s = buy.agent.getID() + " bought from " + sell.agent.getID() +
                    " material=" + buy.materialID + " quantity=" + amount + " price=" + price;
            Log.i("MARKET DEAL", s);
        }
    }


    public void clearClosedOrders() {
        for (int i=orders.size()-1; i>=0; i--) {
            Order order = orders.get(i);
            if (order.type==Order.CLOSED) orders.remove(order);
        }
    }


    public Order orderBuy(Agent agent, int materialID, int quantity, int price) {
        if (agent==null || materialID<0 || quantity <= 0 || price <= 0) return null;
        Order order = new Order();
        order.type = Order.BUY;
        order.agent = agent;
        order.materialID = materialID;
        order.quantity = quantity;
        order.price = price;
        return orders.add(order) ? order : null;
    }

    public Order orderSell(Agent agent, int materialID, int quantity, int price) {
        if (agent==null || materialID<0 || quantity <= 0 || price <= 0) return null;
        Order order = new Order();
        order.type = Order.SELL;
        order.agent = agent;
        order.materialID = materialID;
        order.quantity = quantity;
        order.price = price;
        return orders.add(order) ? order : null;
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
