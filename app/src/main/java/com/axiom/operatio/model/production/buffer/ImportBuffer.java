package com.axiom.operatio.model.production.buffer;

import com.axiom.operatio.model.materials.Item;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.block.Block;

/**
 * Импортер материалов со склада на производство
 */
public class ImportBuffer extends Block {

    protected Material importMaterial;

    // TODO Реализовать импортер со склада
    //

    public ImportBuffer(Production production, int inDir, int inCapacity, int outDir, int outCapacity) {
        super(production, inDir, inCapacity, outDir, outCapacity);
    }

    @Override
    public boolean push(Item item) {
        return false;
    }

    @Override
    public Item peek() {
        return super.peek();
    }

    @Override
    public Item poll() {
        return super.poll();
    }

    @Override
    public void process() {

    }

}
