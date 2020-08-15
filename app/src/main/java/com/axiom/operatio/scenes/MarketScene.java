package com.axiom.operatio.scenes;

import android.view.MotionEvent;

import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Batcher;
import com.axiom.operatio.modelold.market.MarketModel;
import com.axiom.operatio.renderers.BarChart;

public class MarketScene extends GameScene {

    public BarChart chart;
    public float[] values;
    public float time = 0;
    public long lastPlot;

    @Override
    public String getSceneName() {
        return "Market Chart";
    }

    @Override
    public void startScene() {
        MarketModel market = MarketModel.getInstance();
        if (values==null) {
            values = new float[50];
            for (int i=0; i<values.length; i++) {
                values[i] = market.demandFunction(time);
                time += 0.1f;
            }
        }
        if (chart ==null) {
            chart = new BarChart(getResources(), values);
        }
    }

    @Override
    public void disposeScene() {

    }

    @Override
    public void updateScene(float deltaTime) {

    }

    @Override
    public void preRender(Camera camera) {
        MarketModel market = MarketModel.getInstance();
        GraphicsRender.clear();

        // Move values left
        long now = System.currentTimeMillis();
        if (now - (lastPlot + 30) > 0) {
            for (int i = 0; i < values.length - 1; i++) {
                values[i] = values[i + 1];
            }
            values[values.length - 1] = market.demandFunction(time);
            lastPlot = now;
        }

        chart.draw(camera,0,0, Camera.SCREEN_WIDTH,600);
        time += 0.1f;

    }

    @Override
    public void postRender(Camera camera) {
        GraphicsRender.drawText(
                ("FPS:" + GraphicsRender.getFPS() +
                        " QUADS=" + Batcher.getEntriesCount() +
                        " CALLS=" + Batcher.getDrawCallsCount() +
                        " MS:" + GraphicsRender.getRenderTime()).toCharArray(),
                camera.getMinX()+50, camera.getMinY()+1040, 2);

        GraphicsRender.setZOrder(0);
    }

    @Override
    public void onMotion(MotionEvent event, float worldX, float worldY) {

    }
}
