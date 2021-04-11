package com.axiom.operatio.model.production.buffer;

import android.content.res.Resources;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.operatio.model.production.block.BlockRenderer;

public class ImportBufferRenderer extends BlockRenderer {

    protected static Sprite buffersFrames = null;
    protected ImportBuffer importBuffer;
    protected Sprite sprite;


    public ImportBufferRenderer(ImportBuffer importBuffer) {
        this.importBuffer = importBuffer;
        if (buffersFrames == null) {
            Resources resources = SceneManager.getResources();
            buffersFrames = new Sprite(resources, R.drawable.blocks, 8, 16);
        }
        sprite = buffersFrames.getAsSprite(65);
        sprite.setZOrder(7);
    }

    public void draw(Camera camera, float x, float y, float width, float height) {
        sprite.draw(camera,x,y, width, height);
    }

    @Override
    public void setAnimationSpeed(float speed) {
        int activeAnimation = sprite.getActiveAnimation();
        Sprite.Animation animation = sprite.getAnimation(activeAnimation);
        if (animation!=null) animation.speed = speed;
    }
}
