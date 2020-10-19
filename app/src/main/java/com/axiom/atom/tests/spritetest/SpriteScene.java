package com.axiom.atom.tests.spritetest;

import android.view.MotionEvent;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.BatchRender;
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
        background = new Sprite(getResources(), R.drawable.background);
    }

    @Override
    public void changeScene() {

    }

    @Override
    public void updateScene(float deltaTime) {

    }

    private float step = 0.005f;

    @Override
    public void preRender(Camera camera) {
        float x,y, scale;
        background.zOrder = 0;
        background.draw(camera,0,0,Camera.WIDTH,Camera.HEIGHT);
        box.zOrder = 1;
        for (int i=0; i<768; i++) {
            x = (float) Math.random() * Camera.WIDTH;
            y = (float) Math.random() * Camera.HEIGHT;
            scale = 1 + (float) Math.random() * 9;
            box.setRotation(box.getRotation() + 0.01f);
            box.draw(camera,x,y,scale);
        }
        actor.zOrder = 2;
        actor.setRotation(actor.getRotation() + 0.01f);
        float alpha = actor.getAlpha() - step;
        if (alpha<0 || alpha>1) {
            step = -step;
        }
        actor.setAlpha(alpha);
        actor.draw(camera, 960, 540, 10);
    }

    protected StringBuffer fps = new StringBuffer(1024);

    @Override
    public void postRender(Camera camera) {
        float x = camera.getX();
        float y = camera.getY();
        fps.delete(0, fps.length());
        fps.append("FPS:").append(GraphicsRender.getFPS())
                .append(" Quads:").append(BatchRender.getEntriesCount())
                .append(" Calls:").append(BatchRender.getDrawCallsCount())
                .append(" Time:").append(GraphicsRender.getRenderTime())
                .append("ms\nand Other characters that prints here ")
                .append("\nLorem ipsum dolor sit amet, consectetur adipiscing elit,\n" +
                        "sed do eiusmod tempor incididunt ut labore et dolore magna\n" +
                        "aliqua. Ut enim ad minim veniam, quis nostrud exercitation\n" +
                        "ullamco laboris nisi ut aliquip ex ea commodo consequat.\n" +
                        "Duis aute irure dolor in reprehenderit in voluptate velit\n" +
                        "esse cillum dolore eu fugiat nulla pariatur. Excepteur sint\n" +
                        "occaecat cupidatat non proident, sunt in culpa qui officia\n" +
                        "deserunt mollit anim id est laborum."
                );
        GraphicsRender.setZOrder(100);
        GraphicsRender.setColor(0,0,0,1);
        GraphicsRender.drawText(fps, x - 900,y + 460, 2f);
    }

    @Override
    public void onMotion(MotionEvent event, float worldX, float worldY) {

    }

    @Override
    public void disposeScene() {

    }
}
