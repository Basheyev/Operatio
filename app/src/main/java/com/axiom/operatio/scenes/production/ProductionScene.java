package com.axiom.operatio.scenes.production;

import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.BatchRender;
import com.axiom.atom.engine.input.ScaleEvent;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.operatio.model.ProductionBuilder;
import com.axiom.operatio.model.ProductionRenderer;
import com.axiom.operatio.model.Production;
import com.axiom.operatio.model.block.Block;
import com.axiom.operatio.model.block.BlockRenderer;
import com.axiom.operatio.scenes.production.controller.BlockMoveHandler;
import com.axiom.operatio.scenes.production.view.BlocksPanel;
import com.axiom.operatio.scenes.production.controller.InputHandler;
import com.axiom.operatio.scenes.production.view.ModePanel;
import com.axiom.operatio.scenes.production.view.UIBuilder;

// TODO Zoom in/out
public class ProductionScene extends GameScene {

    private Production production;
    private InputHandler inputHandler;
    private ProductionRenderer productionRenderer;

    // Надо сделать сеттеры и геттеры
    public BlocksPanel blocksPanel;
    public ModePanel modePanel;
    public float initialCellWidth = 128;                  // Ширина клетки
    public float initialCellHeight = 128;                 // Высота клетки
    public int snd1, snd2, snd3;
    private boolean initialized = false;


    private ScaleGestureDetector scaleDetector;

    @Override
    public String getSceneName() {
        return "Production";
    }

    @Override
    public void startScene() {

        if (!initialized) {
            production = ProductionBuilder.createDemoProduction();
            productionRenderer = new ProductionRenderer(production, initialCellWidth, initialCellHeight);
            inputHandler = new InputHandler(this, production, productionRenderer);
            UIBuilder.buildUI(getResources(), getSceneWidget(), production);
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
        production.process();
    }

    @Override
    public void preRender(Camera camera) {
        productionRenderer.draw(camera,0,0,1920,1080);
    }

    protected StringBuffer fps = new StringBuffer(100);
    @Override
    public void postRender(Camera camera) {

        BlockMoveHandler hbm = inputHandler.getBlockMoveHandler();

        if (hbm.isDragging()) {
            float cursorX = hbm.getCursorX();
            float cursorY = hbm.getCursorY();
            Block dragBlock = hbm.getDragBlock();

            BlockRenderer renderer = dragBlock.getRenderer();
            float cellWidth = productionRenderer.getCellWidth();
            float cellHeight = productionRenderer.getCellHeight();
            renderer.draw(Camera.getInstance(), cursorX - cellWidth /2, cursorY - cellHeight /2, cellWidth, cellHeight);
        }

        float x = camera.getX();
        float y = camera.getY();
        GraphicsRender.setZOrder(2000);

        fps.delete(0, fps.length());
        fps.append("FPS:").append(GraphicsRender.getFPS())
            .append(" Quads:").append(BatchRender.getEntriesCount())
            .append(" Calls:").append(BatchRender.getDrawCallsCount())
            .append(" Time:").append(GraphicsRender.getRenderTime())
            .append("ms");

        GraphicsRender.setColor(0,0,0,1);
        GraphicsRender.drawText(fps, x - 750,y + 480, 2f);
    }

    @Override
    public void onMotion(MotionEvent event, float worldX, float worldY) {
        inputHandler.onMotion(event, worldX, worldY);
    }

    @Override
    public void onScale(ScaleEvent event, float worldX, float worldY) {
        inputHandler.invalidateActions();
        productionRenderer.scale(event.scaleFactor);
        Log.i("SCALE: ", "X=" + worldX + " Y=" + worldY + " IN PROGRESS=" + event.isScalingInProgress);
    }
}
