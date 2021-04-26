package com.axiom.operatio.model.production.buffer;

import android.content.res.Resources;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.operatio.model.production.block.BlockRenderer;

import static com.axiom.operatio.model.production.ProductionRenderer.Z_ORDER_JOINTS;

public class BufferRenderer extends BlockRenderer {

    protected static Sprite buffersFrames = null;

    protected Buffer buffer;
    protected Sprite sprite;

    public BufferRenderer(Buffer buffer) {
        this.buffer = buffer;
        if (buffersFrames == null) {
            Resources resources = SceneManager.getResources();
            buffersFrames = new Sprite(resources, R.drawable.blocks, 8, 16);
        }
        sprite = buffersFrames.getAsSprite(72, 79);
        sprite.setZOrder(Z_ORDER_JOINTS);
    }

    public void draw(Camera camera, float x, float y, float width, float height) {
        float load = (float) buffer.getItemsAmount() / ((float) buffer.getCapacity());
        int frame = (int) Math.ceil(load * 8);  // всего кадров 8, поэтому нормируем время на кадры
        sprite.setActiveFrame(frame);
        sprite.draw(camera,x,y, width, height);
    }

    @Override
    public void setAnimationSpeed(float speed) {
        int activeAnimation = sprite.getActiveAnimation();
        Sprite.Animation animation = sprite.getAnimation(activeAnimation);
        if (animation!=null) animation.speed = speed;
    }
}
