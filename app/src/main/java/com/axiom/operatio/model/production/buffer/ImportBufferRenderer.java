package com.axiom.operatio.model.production.buffer;

import android.content.res.Resources;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.model.production.block.BlockRenderer;
import com.axiom.operatio.model.production.inserter.Inserter;

import static com.axiom.operatio.model.production.ProductionRenderer.Z_ORDER_JOINTS;
import static com.axiom.operatio.model.production.ProductionRenderer.Z_ORDER_MACHINES;
import static com.axiom.operatio.model.production.block.Block.DOWN;
import static com.axiom.operatio.model.production.block.Block.LEFT;
import static com.axiom.operatio.model.production.block.Block.RIGHT;
import static com.axiom.operatio.model.production.block.Block.UP;

public class ImportBufferRenderer extends BlockRenderer {

    protected static Sprite buffersFrames = null;
    protected ImportBuffer importBuffer;
    protected Sprite sprite;
    protected Sprite halfConveyor;


    public ImportBufferRenderer(ImportBuffer importBuffer) {
        this.importBuffer = importBuffer;
        if (buffersFrames == null) {
            Resources resources = SceneManager.getResources();
            buffersFrames = new Sprite(resources, R.drawable.blocks, 8, 16);
        }
        sprite = buffersFrames.getAsSprite(65);
        sprite.setZOrder(Z_ORDER_MACHINES);
        halfConveyor = buffersFrames.getAsSprite(64);
        halfConveyor.setZOrder(Z_ORDER_JOINTS);
    }

    public void draw(Camera camera, float x, float y, float width, float height) {
        drawJoints(camera, x, y, width, height);
        sprite.draw(camera,x,y, width, height);
    }


    private void drawJoints(Camera camera, float x, float y, float width, float height) {
        boolean left = hasOutputTo(LEFT);
        boolean right = hasOutputTo(RIGHT);
        boolean up = hasOutputTo(UP);
        boolean down = hasOutputTo(DOWN);
        if (left) {
            halfConveyor.setRotation(0);
            halfConveyor.draw(camera,x,y, width, height);
        }
        if (right) {
            halfConveyor.setRotation((float)Math.PI);
            halfConveyor.draw(camera,x,y, width, height);
        }
        if (up) {
            halfConveyor.setRotation((float) -Math.PI/2);
            halfConveyor.draw(camera,x,y, width, height);
        }
        if (down) {
            halfConveyor.setRotation((float) Math.PI/2);
            halfConveyor.draw(camera,x,y, width, height);
        }

    }


    private boolean hasOutputTo(int direction) {
        Production prod = importBuffer.getProduction();
        Block neighbour = prod.getBlockAt(importBuffer, direction);
        if (neighbour==null) return false;
        Block neighbourInput = prod.getBlockAt(neighbour, neighbour.getInputDirection());
        return neighbourInput == importBuffer && !(neighbour instanceof Inserter);
    }


    @Override
    public void setAnimationSpeed(float speed) {
        int activeAnimation = sprite.getActiveAnimation();
        Sprite.Animation animation = sprite.getAnimation(activeAnimation);
        if (animation!=null) animation.speed = speed;
    }
}
