package com.axiom.operatio.production.buffer;

import com.axiom.operatio.production.Production;
import com.axiom.operatio.production.block.Block;
import com.axiom.operatio.production.materials.Item;

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


}
