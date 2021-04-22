package com.axiom.operatio.model.production.conveyor;

import com.axiom.operatio.model.common.JSONSerializable;
import com.axiom.operatio.model.production.block.BlockBuilder;
import com.axiom.operatio.model.production.buffer.Buffer;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.model.materials.Item;
import com.axiom.operatio.model.production.buffer.ExportBuffer;
import com.axiom.operatio.model.production.machine.Machine;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Модель конвейера
 */
public class Conveyor extends Block implements JSONSerializable {

    public static final double CYCLE_COST = 0.02f;

    public static final int DELIVERY_CYCLES = 5;
    public static final int PRICE = 100;
    public static final int MAX_CAPACITY = 4;
    private int deliveryCycles;
    private long lastInputCycle = 0;
    private float inputCycleTime;


    public Conveyor(Production production, int inDir, int outDir) {
        super(production, inDir, MAX_CAPACITY, outDir, MAX_CAPACITY);
        price = PRICE;
        this.deliveryCycles = DELIVERY_CYCLES;
        this.inputCycleTime = deliveryCycles / (float) MAX_CAPACITY;
        this.renderer = new ConveyorRenderer(this);
    }

    public Conveyor(Production production, JSONObject jsonObject, int inDir, int outDir) {
        super(production, inDir, MAX_CAPACITY, outDir, MAX_CAPACITY);
        price = PRICE;
        BlockBuilder.parseCommonFields(this, jsonObject);

        try {
            deliveryCycles = jsonObject.getInt("deliveryCycles");
            lastInputCycle = jsonObject.getLong("lastInputCycle");
            inputCycleTime = (float) jsonObject.getDouble("inputCycleTime");
            lastPollTime = jsonObject.getLong("lastPollTime");
            renderer = new ConveyorRenderer(this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setOutputDirection(int outDir) {
        super.setOutputDirection(outDir);
        ((ConveyorRenderer)renderer).adjustAnimation(inputDirection, outputDirection);
    }

    @Override
    public void setInputDirection(int inDir) {
        super.setInputDirection(inDir);
        ((ConveyorRenderer)renderer).adjustAnimation(inputDirection, outputDirection);
    }

    @Override
    public void setDirections(int inDir, int outDir) {
        super.setDirections(inDir, outDir);
        ((ConveyorRenderer)renderer).adjustAnimation(inputDirection, outputDirection);
    }

    @Override
    public boolean push(Item item) {
        // Если на конвейере уже максимальное количество предметов
        if (getItemsAmount() >= MAX_CAPACITY) {
            setState(BUSY);
            return false;
        }

        // Если не прошло необходимое время (циклов) уходим
        long currentCycle = production.getCurrentCycle();
        if ((currentCycle - lastInputCycle) < inputCycleTime) return false;

        lastInputCycle = currentCycle;
        return super.push(item);
    }


    @Override
    public void process() {

        // Если еще можем забрать предмет, забираем с входящего направления
        if (getItemsAmount() < MAX_CAPACITY) {
            setState(IDLE);
            grabItemsFromInputDirection();
        } else setState(BUSY);

        // Перемещаем на вывод все предметы время доставки которых подошло
        for (int i=0; i<input.size(); i++) {
            Item item = input.peek();
            if (item==null) break;

            long cyclesPassed = production.getCurrentCycle() - item.getCycleOwned();
            if (cyclesPassed >= deliveryCycles) {
                item = input.poll();  // Удалаем из входящей очереди
                output.add(item);     // Добавляем в выходящую очередь
                setState(IDLE);       // Состояние - IDLE (можем брать еще)
            }
        }

        // Если на выводе есть предметы и есть выходной блок отправляем один предмет в выходной блок
        Item item = output.peek();
        if (item!=null) {
            // Берём блок который находится по направлению вывода конвейера
            Block outputBlock = production.getBlockAt(this, outputDirection);
            // Если такой блок есть
            if (outputBlock != null) {
                // Если выходной блок буфер/экспорт/конвейер - заталкиваем сами
                if (outputBlock instanceof Buffer
                        || outputBlock instanceof ExportBuffer
                        || outputBlock instanceof Conveyor) {
                    // Если вход выходного блока смотрит на наш выход
                    int neightInputDirection = outputBlock.getInputDirection();
                    Block neighborInput = production.getBlockAt(outputBlock, neightInputDirection);
                    if (neightInputDirection==NONE || neighborInput==this) {
                        if (outputBlock.push(item)) {
                            output.remove(item);
                            lastPollTime = production.getClock();
                        }
                    }
                }
            }
        }

    }


    @Override
    protected boolean grabItemsFromInputDirection() {
        // если по основному направлению входа нет предметов, пробуем взять с боковых
        if (!super.grabItemsFromInputDirection()) {
            if (!isStraight()) return false;
            Block leftBlock = production.getBlockAt(this, getLeftDirection());
            Block rightBlock = production.getBlockAt(this, getRightDirection());
            if (leftBlock==null && rightBlock==null) return false;

            if (isJoinedConveyor(leftBlock)) {
                Item item = leftBlock.peek();
                if (item!=null && push(item)) {
                    leftBlock.poll();
                }
            }

            if (isJoinedConveyor(rightBlock)) {
                Item item = rightBlock.peek();
                if (item!=null && push(item)) {
                    rightBlock.poll();
                }
            }
        }
        return true;
    }


    protected boolean isJoinedConveyor(Block block) {
        if (block==null) return false;
        boolean isConveyorOrMachine = (block instanceof Conveyor) || (block instanceof Machine);
        boolean hasOutToThisBlock = production.getBlockAt(block, block.getOutputDirection()) == this;
        return isConveyorOrMachine && hasOutToThisBlock;
    }


    @Override
    public void clear() {
        input.clear();
        output.clear();
    }

    public int getDeliveryCycles() {
        return deliveryCycles;
    }


    @Override
    public double getCycleCost() {
        return CYCLE_COST;
    }

    public JSONObject toJSON() {
        JSONObject jsonObject = super.toJSON();
        try {
            jsonObject.put("class", "Conveyor");
            jsonObject.put("deliveryCycles", deliveryCycles);
            jsonObject.put("lastInputCycle", lastInputCycle);
            jsonObject.put("inputCycleTime", inputCycleTime);
            jsonObject.put("lastPollTime", lastPollTime);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return jsonObject;
    }

    @Override
    public String getDescription() {
        return "Moves items from input direction\nto output direction";
    }

}
