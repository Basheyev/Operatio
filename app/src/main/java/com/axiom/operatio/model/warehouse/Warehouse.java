package com.axiom.operatio.model.warehouse;

import com.axiom.atom.engine.data.Channel;
import com.axiom.operatio.model.materials.Item;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.buffer.BufferKeepingUnit;

import java.util.ArrayList;

/**
 * Модель склада материалов
 */
public class Warehouse {

    protected static Warehouse warehouse;
    protected static boolean initialized = false;
    protected ArrayList<Channel<Item>> stockKeepingUnit;


    public static Warehouse getInstance() {
        if (!initialized) warehouse = new Warehouse();
        return warehouse;
    }

    private Warehouse() {
        int materialsAmount = Material.getMaterialsAmount();
        stockKeepingUnit = new ArrayList<Channel<Item>>();
        Material[] materials = Material.getMaterials();
        for (int i=0; i<materialsAmount; i++) {
            Channel<Item> sku = new Channel<Item>(100);
            for (int j=0; j<100; j++) sku.add(new Item(materials[i]));
            stockKeepingUnit.add(sku);
        }
        initialized = true;
    }

    /**
     * Положить предмет на склад
     * @param item предмет
     * @return true если положили, false если места нет
     */
    public boolean put(Item item) {
        if (item==null) return false;
        int ID = item.getMaterial().getMaterialID();
        return stockKeepingUnit.get(ID).add(item);
    }

    /**
     * Забрать предмет требуемого материала со склада
     * @param material тип материала
     * @return предмет указанного материала
     */
    public Item get(Material material) {
        if (material==null) return null;
        int ID = material.getMaterialID();
        return stockKeepingUnit.get(ID).poll();
    }


    public int getBalance(Material material) {
        if (material==null) return 0;
        int ID = material.getMaterialID();
        return stockKeepingUnit.get(ID).size();
    }


    public void process() {
        
    }

}
