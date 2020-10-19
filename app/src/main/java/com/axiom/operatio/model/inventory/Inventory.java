package com.axiom.operatio.model.inventory;

import com.axiom.atom.engine.data.Channel;
import com.axiom.operatio.model.materials.Item;
import com.axiom.operatio.model.materials.Material;

import java.util.ArrayList;

/**
 * Модель склада материалов
 */
public class Inventory {

    protected static Inventory inventory;
    protected static boolean initialized = false;
    protected ArrayList<Channel<Item>> stockKeepingUnit;


    public static Inventory getInstance() {
        if (!initialized) inventory = new Inventory();
        return inventory;
    }

    private Inventory() {
        int materialsAmount = Material.getMaterialsAmount();
        stockKeepingUnit = new ArrayList<Channel<Item>>();
        Material[] materials = Material.getMaterials();
        for (int i=0; i<materialsAmount; i++) {
            Channel<Item> sku = new Channel<Item>(500);
            stockKeepingUnit.add(sku);
            if (!materials[i].getName().equals("reserved"))
            for (int j=0; j<500; j++) sku.add(new Item(materials[i]));
        }
        initialized = true;
    }

    /**
     * Положить предмет на склад
     * @param item предмет
     * @return true если положили, false если места нет
     */
    public boolean push(Item item) {
        if (item==null) return false;
        int ID = item.getMaterial().getMaterialID();
        return stockKeepingUnit.get(ID).add(item);
    }

    /**
     * Вернуть предмет требуемого материала со склада, но не забирать
     * @param material тип материала
     * @return предмет указанного материала
     */
    public Item peek(Material material) {
        if (material==null) return null;
        int ID = material.getMaterialID();
        return stockKeepingUnit.get(ID).peek();
    }

    /**
     * Забрать предмет требуемого материала со склада
     * @param material тип материала
     * @return предмет указанного материала
     */
    public Item poll(Material material) {
        if (material==null) return null;
        int ID = material.getMaterialID();
        return stockKeepingUnit.get(ID).poll();
    }


    /**
     * Возвращает остатки по позиции на складе
     * @param material тип материала
     * @return количество единиц (остатки)
     */
    public int getBalance(Material material) {
        if (material==null) return 0;
        int ID = material.getMaterialID();
        return stockKeepingUnit.get(ID).size();
    }


    public void process() {
        
    }

}
