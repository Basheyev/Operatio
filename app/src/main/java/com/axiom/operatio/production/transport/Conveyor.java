package com.axiom.operatio.production.transport;

import com.axiom.operatio.production.Production;
import com.axiom.operatio.production.blocks.Block;
import com.axiom.operatio.production.materials.Item;

public class Conveyor extends Block {


    public static final int MAX_CAPACITY = 4;
    protected int deliveryCycles;


    public Conveyor(Production production, int inDir, int outDir, int deliveryCycles) {
        super(production, inDir, MAX_CAPACITY, outDir, MAX_CAPACITY);
        this.deliveryCycles = deliveryCycles;
    }


    @Override
    public boolean push(Item item) {
        // Если на конвейере уже максимальное количество предметов
        if (getItemsAmount() >= MAX_CAPACITY) {
            state = BUSY;
            return false;
        }
        // TODO Если не прошло необходимое время (циклы) уходим


        return super.push(item);
    }



    @Override
    public void process() {

        // Если еще можем забрать предмет, забираем с входящего направления
        if (getItemsAmount() < MAX_CAPACITY) {
            state = IDLE;
            getItemFromInputDirection();
        } else state = BUSY;

        // Перемещаем на вывод все предметы время доставки которых подошло
        for (int i=0; i<input.size(); i++) {
            Item item = input.peek();
            if (item==null) break;
            long cyclesPassed = Production.getCurrentCycle() - item.getTimeOwned();
            if (cyclesPassed > deliveryCycles) {
                item = input.poll();  // Удалаем из входящей очереди
                output.add(item);     // Добавляем в выходящую очередь
                state = IDLE;         // Состояние - IDLE (можем брать еще)
            }
        }

    }


}
