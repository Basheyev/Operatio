package com.axiom.operatio.model.buffer;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.operatio.model.block.BlockRenderer;

public class BufferRenderer extends BlockRenderer {

    protected Buffer buffer;
    protected Sprite sprite;


    public BufferRenderer(Buffer buffer) {
        this.buffer = buffer;
        sprite = new Sprite(SceneManager.getResources(), R.drawable.buffer_texture,4,4);
        sprite.zOrder = 1;
    }

    public void draw(Camera camera, float x, float y, float width, float height) {
        float load = (float) buffer.getItemsAmount() / ((float) buffer.getCapacity());
        int frame = (int) Math.ceil(load * 8);  // всего кадров 8, поэтому нормируем время на кадры
        sprite.setActiveFrame(frame);
        sprite.draw(camera,x,y, width, height);
    }

}
