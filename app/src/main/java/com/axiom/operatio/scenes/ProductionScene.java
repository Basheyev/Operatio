package com.axiom.operatio.scenes;

import android.view.MotionEvent;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Batcher;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.input.Input;
import com.axiom.operatio.production.ProductionBuilder;
import com.axiom.operatio.production.ProductionRenderer;
import com.axiom.operatio.production.block.Block;
import com.axiom.operatio.production.Production;
import com.axiom.operatio.production.block.Renderer;


public class ProductionScene extends GameScene {

    private Production production;
    private ProductionRenderer productionRenderer;

    private float cellWidth = 192;                  // Ширина клетки
    private float cellHeight = 192;                 // Высота клетки
    private long lastCycleTime;                     // Время последнего цикла (миллисекунды)
    private long cycleMilliseconds = 100;            // Длительносить цикла (миллисекунды)


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
    public void updateScene(float deltaTime) {
        long now = System.currentTimeMillis();
        if (now - lastCycleTime > cycleMilliseconds) {
            production.cycle();
            lastCycleTime = now;
        }
    }

    @Override
    public void preRender(Camera camera) {
        productionRenderer.draw(camera,0,0,1920,1080);
    }

    @Override
    public void postRender(Camera camera) {
        String fps = "FPS:" + GraphicsRender.getFPS() +
                " QUADS:" + Batcher.getEntriesCount() +
                " CALLS:" + Batcher.getDrawCallsCount();
        GraphicsRender.drawText(fps.toCharArray(), 50,1000, 2);
    }

    @Override
    public void onMotion(MotionEvent event, float worldX, float worldY) {
        Camera camera = Camera.getInstance();
        float x = camera.getX();
        float y = camera.getY();
        camera.lookAt(x + Input.xAxis, y + Input.yAxis);

    }
}
