package com.axiom.operatio.model.production.inserter;

import com.axiom.atom.engine.data.Channel;
import com.axiom.operatio.model.common.JSONSerializable;
import com.axiom.operatio.model.materials.Item;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.model.production.block.BlockBuilder;
import com.axiom.operatio.model.production.conveyor.Conveyor;

import org.json.JSONException;
import org.json.JSONObject;

public class Inserter extends Block implements JSONSerializable {

    public static final double CYCLE_COST = 0.02f;
    public static final int CYCLES_PER_90_DEGREES = 2;
    public static final int MAX_CAPACITY = 1;
    public static final int PRICE = 200;

    public Inserter(Production production, int inDir, int outDir) {
        super(production, inDir, MAX_CAPACITY, outDir, MAX_CAPACITY);
        this.renderer = new InserterRenderer(this);
        this.price = PRICE;
    }

    public Inserter(Production production, JSONObject jsonObject, int inDir, int outDir) {
        super(production, inDir, MAX_CAPACITY, outDir, MAX_CAPACITY);
        BlockBuilder.parseCommonFields(this, jsonObject);
        this.renderer = new InserterRenderer(this);
        this.price = PRICE;
    }

    @Override
    public void process() {
        // Если еще можем забрать предмет, забираем с входящего направления
        if (getItemsAmount() < MAX_CAPACITY) {
            setState(IDLE);
            grabItemsFromInputDirection();
        } else setState(BUSY);


        Item item = input.peek();
        if (item==null) return;
        long cyclesPassed = production.getCurrentCycle() - item.getCycleOwned();

        long deliveryCycles = getDeliveryCycles();
        // если прошло время циклов
        if (cyclesPassed >= deliveryCycles) {
            // Берём блок который находится по направлению вывода конвейера
            Block outputBlock = production.getBlockAt(this, outputDirection);
            // Если есть выходной блок
            if (outputBlock != null) {
                // Блок может принять материал - удаляем из входящей очереди
                if (outputBlock.push(item)) input.remove(item); // Удалаем из входящей очереди
            }
        }

    }


    protected long getDeliveryCycles() {
        if (inputDirection==RIGHT && outputDirection==LEFT) return CYCLES_PER_90_DEGREES * 2; else
        if (inputDirection==LEFT && outputDirection==RIGHT) return CYCLES_PER_90_DEGREES * 2; else
        if (inputDirection==UP && outputDirection==DOWN) return CYCLES_PER_90_DEGREES * 2; else
        if (inputDirection==DOWN && outputDirection==UP) return CYCLES_PER_90_DEGREES * 2;
        else return CYCLES_PER_90_DEGREES;
    }


    protected void grabItemsFromInputDirection() {
        Block inputBlock = production.getBlockAt(this, inputDirection);
        if (inputBlock==null) return;        // Если на входящем направление ничего нет
        Item item = inputBlock.peek();       // Пытаемся взять предмет из блока входа
        if (item == null) {                  // Если ничего нет и это не конвейер уходим
            if (!(inputBlock instanceof Conveyor)) return;
            Channel<Item> inputQueue = inputBlock.getInputQueue();
            item = inputQueue.peek();
            if (item == null) return;
            if (!push(item)) return;        // Если не получилось добавить к себе уходим
            inputQueue.poll();
            return;
        }
        if (!push(item)) return;             // Если не получилось добавить к себе уходим
        inputBlock.poll();                   // Если получилось - удаляем из блока входа
    }



    @Override
    public double getCycleCost() {
        return CYCLE_COST;
    }

    @Override
    public String getDescription() {
        return "Inserter retrieves items from input\ndirection and inserts to output direction";
    }


    @Override
    public JSONObject toJSON() {
        JSONObject jsonObject = super.toJSON();
        try {
            jsonObject.put("class", "Inserter");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return jsonObject;
    }
}
