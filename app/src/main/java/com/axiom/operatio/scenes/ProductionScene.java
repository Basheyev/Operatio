package com.axiom.operatio.scenes;

import android.view.MotionEvent;

import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.operatio.production.ProductionBuilder;
import com.axiom.operatio.production.block.Block;
import com.axiom.operatio.production.Production;
import com.axiom.operatio.production.block.Renderer;


public class ProductionScene extends GameScene {

    private Production production;

    private float cellWidth = 128;                  // Ширина клетки
    private float cellHeight = 128;                 // Высота клетки
    private long lastCycleTime;                     // Время последнего цикла (миллисекунды)
    private long cycleMilliseconds = 200;            // Длительносить цикла (миллисекунды)


    @Override
    public String getSceneName() {
        return "Production";
    }

    @Override
    public void startScene() {
        production = ProductionBuilder.createDemoProduction();
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
        int columns = production.getColumns();
        int rows = production.getRows();
        Block block;
        Renderer renderer;
        GraphicsRender.clear();
        for (int row=0; row < rows; row++) {
            for (int col=0; col < columns; col++) {
                block = production.getBlockAt(col, row);
                if (block!=null) {
                    renderer = block.getRenderer();
                    if (renderer != null) {
                        renderer.draw(camera,
                                col * cellWidth,
                                row * cellHeight,
                                cellWidth,
                                cellHeight);
                    }
                }
            }
        }
        block = null;
    }

    @Override
    public void postRender(Camera camera) {
        GraphicsRender.drawText("Hello world!".toCharArray(), 0,1000, 2);
    }

    @Override
    public void onMotion(MotionEvent event, float worldX, float worldY) {

    }
}
