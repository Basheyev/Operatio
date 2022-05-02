package com.basheyev.operatio.model.production.buffer;


import android.content.res.Resources;

import com.basheyev.atom.R;
import com.basheyev.atom.engine.core.SceneManager;
import com.basheyev.atom.engine.graphics.gles2d.Camera;
import com.basheyev.atom.engine.graphics.renderers.Sprite;
import com.basheyev.operatio.model.production.Production;
import com.basheyev.operatio.model.production.block.Block;
import com.basheyev.operatio.model.production.block.BlockRenderer;
import com.basheyev.operatio.model.production.inserter.Inserter;

import static com.basheyev.operatio.model.production.ProductionRenderer.Z_ORDER_JOINTS;
import static com.basheyev.operatio.model.production.ProductionRenderer.Z_ORDER_MACHINES;
import static com.basheyev.operatio.model.production.block.Block.DOWN;
import static com.basheyev.operatio.model.production.block.Block.LEFT;
import static com.basheyev.operatio.model.production.block.Block.RIGHT;
import static com.basheyev.operatio.model.production.block.Block.UP;

public class ExportBufferRenderer extends BlockRenderer {

    protected static Sprite buffersFrames = null;
    protected ExportBuffer exportBuffer;
    protected Sprite sprite;
    protected Sprite halfConveyor;

    public ExportBufferRenderer(ExportBuffer exportBuffer) {
        this.exportBuffer = exportBuffer;
        if (buffersFrames == null) {
            Resources resources = SceneManager.getResources();
            buffersFrames = new Sprite(resources, R.drawable.blocks, 8, 16);
        }
        sprite = buffersFrames.getAsSprite(66);
        sprite.setZOrder(Z_ORDER_MACHINES);
        halfConveyor = buffersFrames.getAsSprite(64);
        halfConveyor.setZOrder(Z_ORDER_JOINTS);
    }

    public void draw(Camera camera, float x, float y, float width, float height) {
        drawJoints(camera, x, y, width, height);
        sprite.draw(camera,x,y, width, height);
    }


    private void drawJoints(Camera camera, float x, float y, float width, float height) {
        boolean left = hasInputFrom(LEFT);
        boolean right = hasInputFrom(RIGHT);
        boolean up = hasInputFrom(UP);
        boolean down = hasInputFrom(DOWN);
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


    private boolean hasInputFrom(int direction) {
        Production prod = exportBuffer.getProduction();
        Block neighbour = prod.getBlockAt(exportBuffer, direction);
        if (neighbour==null) return false;
        Block neighbourInput = prod.getBlockAt(neighbour, neighbour.getOutputDirection());
        return neighbourInput == exportBuffer && !(neighbour instanceof Inserter);
    }



    @Override
    public void setAnimationSpeed(float speed) {
        int activeAnimation = sprite.getActiveAnimation();
        Sprite.Animation animation = sprite.getAnimation(activeAnimation);
        if (animation!=null) animation.speed = speed;
    }

}


