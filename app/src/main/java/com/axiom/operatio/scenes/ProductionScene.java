package com.axiom.operatio.scenes;

import android.view.DragEvent;
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
    private boolean dragging = false;
    private float cursorX, cursorY;
    private long lastCycleTime;                     // Время последнего цикла (миллисекунды)
    private static long cycleMilliseconds = 300;    // Длительносить цикла (миллисекунды)


    @Override
    public String getSceneName() {
        return "Production";
    }

    @Override
    public void startScene() {
        //Input.enabled = false;
        production = ProductionBuilder.createDemoProduction();
        productionRenderer = new ProductionRenderer(production, cellWidth, cellHeight);
        UIBuilder.buildUI(getResources(), getSceneWidget());
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
    }

    @Override
    public void preRender(Camera camera) {
        productionRenderer.draw(camera,0,0,1920,1080);
    }

    @Override
    public void postRender(Camera camera) {
        float x = camera.getX();
        float y = camera.getY();
        GraphicsRender.setZOrder(2000);
     //   GraphicsRender.setColor(0,0,0,1);
     //   GraphicsRender.drawRectangle(cursorX-10,cursorY-10, 20, 20,null);
        String fps = "FPS:" + GraphicsRender.getFPS() +
                " QUADS:" + BatchRender.getEntriesCount() +
                " CALLS:" + BatchRender.getDrawCallsCount();
        GraphicsRender.drawText(fps.toCharArray(), x - 700,y + 500, 2);
    }

    @Override
    public void onMotion(MotionEvent event, float worldX, float worldY) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                dragging = true;
                cursorX = worldX;
                cursorY = worldY;
                break;
            case MotionEvent.ACTION_MOVE:
                if (dragging) {
                    Camera camera = Camera.getInstance();
                    float x = camera.getX() + (cursorX - worldX);
                    float y = camera.getY() + (cursorY - worldY);
                    // Проверка границ карты
                    if (x - Camera.WIDTH / 2 < 0) x = Camera.WIDTH / 2;
                    if (y - Camera.HEIGHT / 2 < 0) y = Camera.HEIGHT / 2;
                    if (x + Camera.WIDTH / 2 > production.getColumns() * cellWidth)
                        x = production.getColumns() * cellWidth - Camera.WIDTH / 2;
                    if (y + Camera.HEIGHT / 2 > production.getRows() * cellHeight)
                        y = production.getRows() * cellHeight - Camera.HEIGHT / 2;
                    camera.lookAt(x, y);
                }
                break;
            case MotionEvent.ACTION_UP:
                dragging = false;
        }


    }


    public static long getCycleTimeMs() {
        return cycleMilliseconds;
    }
}
