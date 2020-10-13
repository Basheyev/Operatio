package com.axiom.operatio.model.production.buffer;

import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.model.materials.Item;

// TODO 1. Может хранить до 4 видов материалов
// TODO 2. Добавить типы материалов и пропорции хранения в буфере (чтобы сборщик мог работать ритмично)
// TODO 3. Добавить возможность брать из буфера необходимый тип материала (для сборщика)

/**
 * Представляет собой мини-склад, который может хранить до 4 видов материалов
 */
public class Buffer extends Block {

    public static final int NO_SKU = -1;             // Константа отсутствия такой ячейки хранения
    protected StockKeepingUnit[] stockKeepingUnit;   // Ячейки хранения материалов

    public Buffer(Production production, int capacity) {
        super(production, Block.NONE, capacity, Block.NONE, 1);
        renderer = new BufferRenderer(this);
        // Сформировать список материалов который может хранить (null - любой)
        stockKeepingUnit = new StockKeepingUnit[4];
        for (int i=0; i<4; i++) {
            stockKeepingUnit[i] = new StockKeepingUnit();
            stockKeepingUnit[i].material = null;
            stockKeepingUnit[i].capacity = capacity / 4;
            stockKeepingUnit[i].total = 0;
        }
    }

    /**
     * Добавляет предмет во входную очередь буфера
     * @param item предмет
     * @return true - если блок принял предмет, false - если нет
     */
    public boolean push(Item item) {
        if (item==null) return false;
        if (state==BUSY) return false;
        if (input.size()>=inputCapacity) return false;

        // Узнаем есть ли ячейка под такой материал
        int materialSKU = getMaterialSKU(item.getMaterial());
        // Если её нет и создать её нельзя - уходим
        if (materialSKU == NO_SKU) return false;

        StockKeepingUnit sku = stockKeepingUnit[materialSKU];
        // Если для добавления предмета в ячейке нет места уходим
        if (sku.total >= sku.capacity) return false;

        sku.total++;
        item.setOwner(this);
        input.add(item);
        return true;
    }

    @Override
    public Item peek() {
        return input.peek();
    }

    @Override
    public Item poll() {
        Item item = input.peek();
        if (item==null) return null;
        item = input.poll();
        if (item!=null) {
            int skuID = getMaterialSKU(item.getMaterial());
            stockKeepingUnit[skuID].total--;
            return item;
        }
        return null;
    }


    public Item peek(Material material) {
        if (input.size()==0) return null;
        for (int i=input.size()-1; i>=0; i--) {
            Item item = input.get(i);
            if (item.getMaterial().equals(material)) {
                return item;
            }
        }
        return null;
    }

    public Item poll(Material material) {
        if (input.size()==0) return null;
        for (int i=input.size()-1; i>=0; i--) {
            Item item = input.get(i);
            if (item.getMaterial().equals(material)) {
                int skuID = getMaterialSKU(item.getMaterial());
                stockKeepingUnit[skuID].total--;
                input.remove(item);
                return item;
            }
        }
        return null;
    }

    @Override
    public void process() {
       // do nothing
    }

    @Override
    public void setOutputDirection(int outDir) {
        super.setOutputDirection(NONE);
    }

    @Override
    public void setInputDirection(int inDir) {
        super.setInputDirection(NONE);
    }

    @Override
    public void setDirections(int inDir, int outDir) {
        super.setDirections(NONE, NONE);
    }

    //------------------------------------------------------------------------------------------

    public Material getSKUMaterial(int id) {
        if (id < 0 || id > 3) return null;
        return stockKeepingUnit[id].material;
    }

    public int getSKUTotal(int id) {
        if (id < 0 || id > 3) return NO_SKU;
        return stockKeepingUnit[id].total;
    }

    /**
     * Возвращает номер SKU (ячейки) хранения
     * @param material материал
     * @return возвращает номер ячейки или -1 если такой ячейки нет
     */
    private int getMaterialSKU(Material material) {
        for (int i=0; i<4; i++) {
            // Если такого материала нет и есть свободные ячейки - создаём такую ячейку
            if (stockKeepingUnit[i].material==null) {
                stockKeepingUnit[i].material = material;
                return i;
            // Если ячейка с таким материалом есть, то возвращаем её номер
            } else if (stockKeepingUnit[i].material.equals(material)) {
                return i;
            }
        }
        // Говорим что такой ячейки нет
        return -1;
    }

}
