package com.axiom.operatio.scenes.production;

import android.view.MotionEvent;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.BatchRender;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.operatio.model.ProductionBuilder;
import com.axiom.operatio.model.ProductionRenderer;
import com.axiom.operatio.model.Production;
import com.axiom.operatio.scenes.production.view.BlocksPanel;
import com.axiom.operatio.scenes.production.controller.InputHandler;
import com.axiom.operatio.scenes.production.view.ModePanel;
import com.axiom.operatio.scenes.production.view.UIBuilder;

public class ProductionScene extends GameScene {

    private int sceneState;

    private Production production;
    private InputHandler inputHandler;
    private ProductionRenderer productionRenderer;

    // Надо сделать сеттеры и геттеры
    public BlocksPanel blocksPanel;
    public ModePanel modePanel;
    public float cellWidth = 128;                  // Ширина клетки
    public float cellHeight = 128;                 // Высота клетки
    public int snd1, snd2, snd3;
    private boolean initialized = false;

    private long lastCycleTime;                     // Время последнего цикла (миллисекунды)
    private static long cycleMilliseconds = 300;    // Длительносить цикла (миллисекунды)

    @Override
    public String getSceneName() {
        return "Production";
    }

    @Override
    public void startScene() {
        //Input.enabled = false;
        if (!initialized) {
            production = ProductionBuilder.createDemoProduction();
            productionRenderer = new ProductionRenderer(production, cellWidth, cellHeight);
            inputHandler = new InputHandler(this, production, productionRenderer);
            UIBuilder.buildUI(getResources(), getSceneWidget());
            blocksPanel = (BlocksPanel) UIBuilder.getBlocksPanel();
            modePanel = (ModePanel) UIBuilder.getEditorPanel();
            snd1 = SoundRenderer.loadSound(R.raw.machine_snd);
            snd2 = SoundRenderer.loadSound(R.raw.conveyor_snd);
            snd3 = SoundRenderer.loadSound(R.raw.buffer_snd);
            initialized = true;
        }
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
        String fps = "FPS:" + GraphicsRender.getFPS() +
                " QUADS:" + BatchRender.getEntriesCount() +
                " CALLS:" + BatchRender.getDrawCallsCount() +
                " TIME:" + GraphicsRender.getRenderTime() + "ms";
        GraphicsRender.drawText(fps.toCharArray(), x - 700,y + 500, 2);
    }

    @Override
    public void onMotion(MotionEvent event, float worldX, float worldY) {
        // TODO Вызывать разные обработчики в зависимости от состояния сцены
        inputHandler.onMotion(event,worldX,worldY);
    }


    public static long getCycleTimeMs() {
        return cycleMilliseconds;
    }
}