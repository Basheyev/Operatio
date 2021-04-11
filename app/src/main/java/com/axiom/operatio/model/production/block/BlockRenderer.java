package com.axiom.operatio.model.production.block;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.operatio.model.production.Production;

/**
 * Рендер блока
 */
public abstract class BlockRenderer {

    private static final int INPUT_SPRITE_INDEX = 89;
    private static final int OUTPUT_SPRITE_INDEX = 88;
    private static Sprite directions = null;

    public abstract void draw(Camera camera, float x, float y, float width, float height);

    public void drawInOut(Camera camera, int inDir, int outDir,
                          float x, float y, float width, float height, int zOrder) {

        if (directions == null) {
            loadSprites();
            return;
        }

        float inputRotation = directionToRadians(inDir);
        float outputRotation = directionToRadians(outDir);

        directions.setAlpha(0.625f);
        directions.setZOrder(zOrder + 5);
        directions.setActiveFrame(INPUT_SPRITE_INDEX);
        directions.setRotation(inputRotation);
        directions.draw(camera, x, y, width, height, null);

        directions.setZOrder(zOrder + 5);
        directions.setActiveFrame(OUTPUT_SPRITE_INDEX);
        directions.setRotation(outputRotation);
        directions.draw(camera, x, y, width, height, null);
    }


    private float directionToRadians(int direction) {
        float angle = 0;
        switch (direction) {
            case Block.LEFT:  angle = 0; break;
            case Block.UP:    angle = (float) -(Math.PI / 2); break;
            case Block.RIGHT: angle = (float) -(Math.PI); break;
            case Block.DOWN:  angle = (float) (Math.PI / 2); break;
        }
        return angle;
    }


    private static void loadSprites() {
        directions = new Sprite(SceneManager.getResources(), R.drawable.blocks, 8, 16);
    }

    public abstract void setAnimationSpeed(float speed);
}
