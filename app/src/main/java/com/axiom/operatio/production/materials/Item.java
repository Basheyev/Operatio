package com.axiom.operatio.production.materials;

import com.axiom.operatio.production.blocks.Block;

/**
 * Содержит информацию о предмете
 */
public class Item {

    protected Material material;
    protected Block owner;
    protected long timeOwned;

    //----------------------------------------------------------------------------
    public Item(Material material) {
        this.material = material;
    }


    public Material getMaterial() {
        return material;
    }

    public Block getOwner() {
        return owner;
    }

    public void setOwner(Block owner, long timeOwned) {
        this.owner = owner;
        this.timeOwned = timeOwned;
    }

}
