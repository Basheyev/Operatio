package com.axiom.operatio.scenes.market;

import android.graphics.Color;
import android.opengl.GLES20;
import android.view.MotionEvent;

import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.BatchRender;
import com.axiom.atom.engine.graphics.renderers.Line;


public class MarketScene extends GameScene {
    @Override
    public String getSceneName() {
        return null;
    }

    @Override
    public void startScene() {

    }

    @Override
    public void disposeScene() {

    }

    @Override
    public void changeScene() {

    }

    @Override
    public void updateScene(float deltaTime) {

    }

    @Override
    public void preRender(Camera camera) {

       GraphicsRender.setZOrder(0);
       GraphicsRender.setColor(Color.DKGRAY);
       GraphicsRender.drawRectangle(0,0,1920,1080);
       GraphicsRender.setZOrder(1);
        GraphicsRender.setColor(Color.GREEN);
        float x, y, oldX = 0, oldY = 540;
        for (int i=-20; i<1920; i+=10) {
            x = i;
            y = ((float) Math.sin(i / 100.0f)) * 300 + 540;
            GraphicsRender.drawLine(oldX, oldY, x, y);
            oldX = x;
            oldY = y;
        }
        GraphicsRender.drawLine(-100, -100, -10, -10);
    }

    protected StringBuffer fps = new StringBuffer(100);

    @Override
    public void postRender(Camera camera) {
        fps.delete(0, fps.length());
        fps.append("FPS:").append(GraphicsRender.getFPS())
                .append(" Quads:").append(BatchRender.getEntriesCount())
                .append(" Calls:").append(BatchRender.getDrawCallsCount())
                .append(" Time:").append(GraphicsRender.getRenderTime())
                .append("ms");
        float x = camera.getMinX();
        float y = camera.getMinY();
        GraphicsRender.setZOrder(2000);
        GraphicsRender.setColor(1,1,1,1);
        GraphicsRender.drawText(fps, x + 600,y + 20, 2f);
    }

    @Override
    public void onMotion(MotionEvent event, float worldX, float worldY) {

    }
}
