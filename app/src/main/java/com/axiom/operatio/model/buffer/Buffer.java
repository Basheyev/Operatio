package com.axiom.operatio.model.buffer;

import com.axiom.operatio.model.Production;
import com.axiom.operatio.model.block.Block;
import com.axiom.operatio.model.materials.Item;

public class Buffer extends Block {


    public Buffer(Production production, int capacity) {
        super(production, Block.NONE, capacity, Block.NONE, 1);
        this.renderer = new BufferRenderer(this);
    }


    @Override
    public Item peek() {
        return input.peek();
    }

    @Override
    public Item poll() {
        Item item = input.peek();
        if (item==null) return null;
        return input.poll();
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

}
