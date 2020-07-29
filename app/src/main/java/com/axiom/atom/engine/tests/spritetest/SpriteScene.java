package com.axiom.atom.engine.tests.spritetest;

import android.view.MotionEvent;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Batcher;
import com.axiom.atom.engine.graphics.renderers.Sprite;

public class SpriteScene extends GameScene {

    Sprite box, actor, background;

    @Override
    public String getSceneName() {
        return "Sprite test";
    }

    @Override
    public void startScene() {
        box = new Sprite(getResources(), R.drawable.material);
        actor = new Sprite(getResources(), R.drawable.player, 6, 3);
        actor.setActiveAnimation(actor.addAnimation( 0, 16,6, true));
        background = new Sprite(getResources(), R.drawable.bitmap);
    }

    @Override
    public void updateScene(float deltaTime) {

    }

    @Override
    public void preRender(Camera camera) {
        float x,y, scale;
        background.zOrder = 0;
        background.draw(camera,0,0,Camera.SCREEN_WIDTH,Camera.SCREEN_HEIGHT);
        box.zOrder = 1;
        for (int i=0; i<1000; i++) {
            x = (float) Math.random() * Camera.SCREEN_WIDTH;
            y = (float) Math.random() * Camera.SCREEN_HEIGHT;
            scale = 1 + (float) Math.random() * 9;
            box.draw(camera,x,y,scale);
        }
        actor.zOrder = 2;
        actor.draw(camera, 960, 540, 10);
    }

    @Override
    public void postRender(Camera camera) {
        GraphicsRender.setZOrder(100);
        GraphicsRender.drawText(
                ("FPS:" + GraphicsRender.getFPS() +
                        " CALLS=" + Batcher.getDrawCallsCount() +
                        " QUADS=" + Batcher.getEntriesCount() +
                        " MS:" + GraphicsRender.getRenderTime()).toCharArray(),
                camera.x1+50, camera.y1+1040, 2);
    }

    @Override
    public void onMotion(MotionEvent event, float worldX, float worldY) {

    }

    @Override
    public void dispose() {

    }
}
