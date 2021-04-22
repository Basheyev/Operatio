package com.axiom.operatio.model.production.inserter;

import com.axiom.atom.engine.data.Channel;
import com.axiom.operatio.model.common.JSONSerializable;
import com.axiom.operatio.model.materials.Item;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.model.production.block.BlockBuilder;
import com.axiom.operatio.model.production.conveyor.Conveyor;
import com.axiom.operatio.model.production.conveyor.ConveyorRenderer;

import org.json.JSONException;
import org.json.JSONObject;

public class Inserter extends Block implements JSONSerializable {

    public static final double CYCLE_COST = 0.02f;
    public static final int CYCLES_PER_90_DEGREES = 2;
    public static final int MAX_CAPACITY = 1;
    public static final int PRICE = 200;

    private Material targetMaterial = null;

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
        try {
            int matID = jsonObject.getInt("targetMaterial");
            targetMaterial = matID==-1 ? null : Material.getMaterial(matID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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


    public void setTargetMaterial(Material material) {
        targetMaterial = material;
        clear();
    }


    public Material getTargetMaterial() {
        return targetMaterial;
    }


    protected long getDeliveryCycles() {
        if (inputDirection==RIGHT && outputDirection==LEFT) return CYCLES_PER_90_DEGREES * 2; else
        if (inputDirection==LEFT && outputDirection==RIGHT) return CYCLES_PER_90_DEGREES * 2; else
        if (inputDirection==UP && outputDirection==DOWN) return CYCLES_PER_90_DEGREES * 2; else
        if (inputDirection==DOWN && outputDirection==UP) return CYCLES_PER_90_DEGREES * 2;
        else return CYCLES_PER_90_DEGREES;
    }


    protected boolean grabItemsFromInputDirection() {
        Block inputBlock = production.getBlockAt(this, inputDirection);
        if (inputBlock==null) return false;     // Если на входящем направление ничего нет

        if (inputBlock instanceof Conveyor) {
            Channel<Item> inputQueue = inputBlock.getInputQueue();
            Item item = inputQueue.peek();
            if (item == null) {
                item = inputBlock.peek();
                if (item==null) return false;
                if (targetMaterial != null && item.getMaterial() != targetMaterial) return false;
                if (!push(item)) return false;       // Если не получилось добавить к себе уходим
                inputBlock.poll();                   // Если получилось - удаляем из блока входа
                return true;
            }
            if (targetMaterial != null && item.getMaterial() != targetMaterial) return false;
            if (!push(item)) return false;        // Если не получилось добавить к себе уходим
            inputQueue.poll();
        } else {
            Item item = inputBlock.peek();        // Пытаемся взять предмет из блока входа
            if (targetMaterial != null && item.getMaterial() != targetMaterial) return false;
            if (!push(item)) return false;             // Если не получилось добавить к себе уходим
            inputBlock.poll();                   // Если получилось - удаляем из блока входа
        }
        return true;
    }


    @Override
    public void clear() {
        input.clear();
        output.clear();
        setState(IDLE);
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
            int matID = targetMaterial!=null ? targetMaterial.getID() : -1;
            jsonObject.put("class", "Inserter");
            jsonObject.put("targetMaterial", matID);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return jsonObject;
    }
}
