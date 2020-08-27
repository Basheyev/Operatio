package com.axiom.operatio.production;

import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.operatio.production.block.Renderer;

public class ProductionRenderer implements Renderer {

    protected Production production;

    public ProductionRenderer(Production production) {
        this.production = production;
    }

    @Override
    public void draw(Camera camera, float x, float y, float width, float height) {



    }

}
