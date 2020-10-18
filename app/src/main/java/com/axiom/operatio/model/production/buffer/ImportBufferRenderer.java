package com.axiom.operatio.model.production.buffer;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.operatio.model.production.block.BlockRenderer;

public class ImportBufferRenderer extends BlockRenderer {

    protected ImportBuffer importBuffer;
    protected Sprite sprite;


    public ImportBufferRenderer(ImportBuffer importBuffer) {
        this.importBuffer = importBuffer;
        sprite = new Sprite(SceneManager.getResources(), R.drawable.blocks,8,11);
        sprite.zOrder = 1;
    }

    public void draw(Camera camera, float x, float y, float width, float height) {
        sprite.setActiveFrame(65);
        sprite.draw(camera,x,y, width, height);
    }
}
