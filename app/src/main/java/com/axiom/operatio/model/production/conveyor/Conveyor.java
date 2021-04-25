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

    public static final String MSG_READY = "Ready";
    public static final String MSG_BUSY = "Output is busy";

    public static final double CYCLE_COST = 0.02f;
    public static final int SPEED_1 = 5;
    public static final int SPEED_2 = 4;
    public static final int SPEED_3 = 3;
    public static final int PRICE = 100;
    public static final int MAX_CAPACITY = 4;

    private int deliveryCycles;
    private long lastInputCycle = 0;
    private float inputCycleTime;


    public Conveyor(Production production, int inDir, int outDir) {
        super(production, inDir, MAX_CAPACITY, outDir, MAX_CAPACITY);
        price = PRICE;
        this.deliveryCycles = SPEED_1;
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
            setState(BUSY, MSG_BUSY);
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
            setState(IDLE, MSG_READY);
            grabItemsFromInput();
        } else setState(BUSY, MSG_BUSY);

        // Перемещаем на вывод все предметы время доставки которых подошло
        for (int i=0; i<input.size(); i++) {
            Item item = input.peek();
            if (item == null) break;

            long cyclesPassed = production.getCurrentCycle() - item.getCycleOwned();
            if (cyclesPassed >= deliveryCycles && output.size()==0) {
                item = input.poll();         // Удалаем из входящей очереди
                output.add(item);            // Добавляем в выходящую очередь
                setState(IDLE, MSG_READY);   // Состояние - IDLE (можем брать еще)
            }
        }

        pushToOutput();
    }


    @Override
    protected boolean grabItemsFromInput() {
        // если по основному направлению входа нет предметов и направление прямое пробуем боковые
        if (!super.grabItemsFromInput() || isStraight()) {
            Block leftBlock = production.getBlockAt(this, getLeftDirection());
            Block rightBlock = production.getBlockAt(this, getRightDirection());
            Item leftBlockItem = null;
            Item rightBlockItem = null;
            if (isJoinedConveyor(leftBlock)) leftBlockItem = leftBlock.peek();
            if (isJoinedConveyor(rightBlock)) rightBlockItem = rightBlock.peek();
            if (leftBlockItem==null && rightBlockItem==null) return false;

            int priority;
            if (leftBlockItem != null && rightBlockItem == null) priority = LEFT; else
            if (leftBlockItem == null && rightBlockItem != null) priority = RIGHT; else {
                if (leftBlockItem.getTimeOwned() > rightBlockItem.getTimeOwned())
                    priority = LEFT;
                else priority = RIGHT;
            }

            if (priority==LEFT) {
                if (push(leftBlockItem)) leftBlock.poll();
                if (push(rightBlockItem)) rightBlock.poll();
            } else {
                if (push(rightBlockItem)) rightBlock.poll();
                if (push(leftBlockItem)) leftBlock.poll();
            }

        }
        return true;
    }



    protected void pushToOutput() {

        // Если на выводе есть предмет
        Item item = output.peek();
        if (item == null) return;

        // Если есть блок по направлению выхода
        Block block = production.getBlockAt(this, outputDirection);
        if (block == null) return;

        boolean isBuffer = block instanceof Buffer || block instanceof ExportBuffer;
        boolean isConveyor = block instanceof Conveyor;

        if (isBuffer || isConveyor) {
            // Если вход выходного блока смотрит на наш выход
            int neighbourInpDir = block.getInputDirection();
            Block neighborInput = production.getBlockAt(block, neighbourInpDir);
            if (neighbourInpDir==NONE || neighborInput==this) {
                if (block.push(item)) {
                    output.poll();
                    lastPollTime = production.getClock();
                }
            }
        }
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


    public int getSpeed() {
        switch (deliveryCycles) {
            case SPEED_1: return 1;
            case SPEED_2: return 2;
            case SPEED_3: return 3;
            default: return 1;
        }
    }

    public void setSpeed(int speed) {
        switch (speed) {
            case 1: deliveryCycles = SPEED_1; break;
            case 2: deliveryCycles = SPEED_2; break;
            case 3: deliveryCycles = SPEED_3; break;
            default: return;
        }
        renderer.setAnimationSpeed(speed);
        inputCycleTime = deliveryCycles / (float) MAX_CAPACITY;
    }

    @Override
    public double getCycleCost() {
        switch (deliveryCycles) {
            case SPEED_1: return CYCLE_COST;
            case SPEED_2: return CYCLE_COST * 1.5;
            case SPEED_3: return CYCLE_COST * 3;
        }
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
