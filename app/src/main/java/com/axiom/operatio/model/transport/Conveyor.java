package com.axiom.operatio.model.transport;

import com.axiom.atom.engine.data.Channel;
import com.axiom.operatio.model.buffer.Buffer;
import com.axiom.operatio.model.Production;
import com.axiom.operatio.model.block.Block;
import com.axiom.operatio.model.materials.Item;

public class Conveyor extends Block {

    public static final int MAX_CAPACITY = 4;
    protected int deliveryCycles;
    private long lastInputCycle = 0;
    private float inputCycleTime;

    public Conveyor(Production production, int inDir, int outDir, int deliveryCycles) {
        super(production, inDir, MAX_CAPACITY, outDir, MAX_CAPACITY);
        this.deliveryCycles = deliveryCycles;
        this.inputCycleTime = deliveryCycles / (float) MAX_CAPACITY;
        this.renderer = new ConveyorRenderer(this);
    }


    @Override
    public boolean push(Item item) {
        // Если на конвейере уже максимальное количество предметов
        if (getItemsAmount() >= MAX_CAPACITY) {
            state = BUSY;
            return false;
        }

        // Если не прошло необходимое время (циклов) уходим
        long currentCycle = Production.getCurrentCycle();
        if ((currentCycle - lastInputCycle) < inputCycleTime) return false;

        lastInputCycle = currentCycle;
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

            long cyclesPassed = Production.getCurrentCycle() - item.getCycleOwned();
            if (cyclesPassed >= deliveryCycles) {
                item = input.poll();  // Удалаем из входящей очереди
                output.add(item);     // Добавляем в выходящую очередь
                state = IDLE;         // Состояние - IDLE (можем брать еще)

                // Если приёмник буфер или конвейер - затолкать самостоятельно
                Block outputBlock = production.getBlockAt(this,outputDirection);
                if (outputBlock!=null) {
                    if (outputBlock instanceof Buffer || outputBlock instanceof Conveyor) {
                       if (outputBlock.push(item)) output.remove(item);
                    }
                }


            }
        }

    }


    public int getDeliveryCycles() {
        return deliveryCycles;
    }


    public int getTotalCapacity() {
        return inputCapacity + outputCapacity;
    }


    public Channel<Item> getInputQueue() {
        return input;
    }

    public Channel<Item> getOutputQueue() {
        return output;
    }

}
