package com.axiom.operatio.scenes;

import android.view.MotionEvent;

import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.BatchRender;
import com.axiom.atom.engine.input.Input;
import com.axiom.operatio.production.ProductionBuilder;
import com.axiom.operatio.production.ProductionRenderer;
import com.axiom.operatio.production.Production;


public class ProductionScene extends GameScene {

    private Production production;
    private ProductionRenderer productionRenderer;

    private float cellWidth = 128;                  // Ширина клетки
    private float cellHeight = 128;                 // Высота клетки
    private long lastCycleTime;                     // Время последнего цикла (миллисекунды)
    private static long cycleMilliseconds = 300;    // Длительносить цикла (миллисекунды)


    @Override
    public String getSceneName() {
        return "Production";
    }

    @Override
    public void startScene() {
        production = ProductionBuilder.createDemoProduction();
        productionRenderer = new ProductionRenderer(production, cellWidth, cellHeight);
    }

    @Override
    public void disposeScene() {
        production.clearBlocks();
    }

    @Override
    public void updateScene(float deltaTimeNs) {
        long now = System.currentTimeMillis();
        if (now - lastCycleTime > cycleMilliseconds) {
            production.cycle();
            lastCycleTime = now;
        }

        Camera camera = Camera.getInstance();
        float x = camera.getX();
        float y = camera.getY();
        camera.lookAt(x + Input.xAxis, y + Input.yAxis);
    }

    @Override
    public void preRender(Camera camera) {
        productionRenderer.draw(camera,0,0,1920,1080);
    }

    @Override
    public void postRender(Camera camera) {
        float x = camera.getX();
        float y = camera.getY();
        String fps = "FPS:" + GraphicsRender.getFPS() +
                " QUADS:" + BatchRender.getEntriesCount() +
                " CALLS:" + BatchRender.getDrawCallsCount();
        GraphicsRender.drawText(fps.toCharArray(), x - 900,y + 500, 2);
    }

    @Override
    public void onMotion(MotionEvent event, float worldX, float worldY) {

    }

    public static long getCycleTimeMs() {
        return cycleMilliseconds;
    }
}
