package com.axiom.operatio.production.buffer;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.operatio.production.block.Renderer;

public class BufferRenderer implements Renderer {

    protected Buffer buffer;
    protected Sprite sprite;


    public BufferRenderer(Buffer buffer) {
        this.buffer = buffer;
        sprite = new Sprite(SceneManager.getResources(), R.drawable.buffer_texture,4,4);
        sprite.zOrder = 1;
    }

    public void draw(Camera camera, float x, float y, float width, float height) {
        float load = (float) buffer.getItemsAmount() / ((float) buffer.getCapacity());
        int frame = (int) Math.ceil(load * 8);  // всего кадров 8, поэтому нормируем вермя на кадры
        sprite.setActiveFrame(frame);
        sprite.draw(camera,x,y, width, height);
       /* String bf1 = ""+ buffer.getItemsAmount();
        GraphicsRender.setZOrder(10);
        GraphicsRender.drawText(bf1.toCharArray(), x + width / 2 ,y + height / 2,1);*/
    }

}
