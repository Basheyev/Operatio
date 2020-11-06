package com.axiom.operatio.scenes.market;

import android.graphics.Color;
import android.view.MotionEvent;

import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.BatchRender;
import com.axiom.operatio.model.market.Market;


public class MarketScene extends GameScene {

    private boolean initialized = false;
    private Market market;
    private MarketPanel marketPanel;
    private long lastTime;

    @Override
    public String getSceneName() {
        return null;
    }

    @Override
    public void startScene() {
        if (!initialized) {
            market = new Market();
            marketPanel = new MarketPanel(null, market);
            getSceneWidget().addChild(marketPanel);
            initialized = true;
        }
    }

    @Override
    public void disposeScene() {

    }

    @Override
    public void changeScene() {

    }

    @Override
    public void updateScene(float deltaTime) {
        long now = System.currentTimeMillis();
        if (now - lastTime > 300) {
            market.process();
            marketPanel.updateValues();
            lastTime = now;
        }
    }

    @Override
    public void preRender(Camera camera) {

       GraphicsRender.setZOrder(0);
       GraphicsRender.setColor(Color.BLACK);
       GraphicsRender.drawRectangle(0,0,1920,1080);

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
