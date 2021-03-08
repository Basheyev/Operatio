package com.axiom.operatio.scenes.production;

import android.view.MotionEvent;

import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.BatchRender;
import com.axiom.atom.engine.input.ScaleEvent;
import com.axiom.operatio.model.production.ProductionRenderer;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.scenes.report.ReportScene;
import com.axiom.operatio.scenes.production.view.BlocksPanel;
import com.axiom.operatio.scenes.production.controller.InputHandler;
import com.axiom.operatio.scenes.production.view.ModePanel;
import com.axiom.operatio.scenes.production.view.AdjustmentPanel;
import com.axiom.operatio.scenes.production.view.ProductionSceneUI;
import com.axiom.operatio.scenes.inventory.InventoryScene;
import com.axiom.operatio.scenes.technology.TechnologyScene;

import org.json.JSONException;
import org.json.JSONObject;

public class ProductionScene extends GameScene {

    public static final String SCENE_NAME = "Production";

    private Production production;
    private InputHandler inputHandler;
    private ProductionRenderer productionRenderer;
    private int currentLevel = -1;

    // Надо сделать сеттеры и геттеры
    public BlocksPanel blocksPanel;
    public ModePanel modePanel;
    public AdjustmentPanel adjustmentPanel;
    public float initialCellWidth = 128;                  // Ширина клетки
    public float initialCellHeight = 128;                 // Высота клетки

    private double lastCashBalance = 0;
    private boolean initialized = false;


    public ProductionScene() {
        production = new Production(32,24);
    }

    public ProductionScene(JSONObject jsonProduction) throws JSONException {
        production = new Production(jsonProduction);
    }

    @Override
    public String getSceneName() {
        return SCENE_NAME;
    }

    @Override
    public void startScene() {
        if (!initialized) {
            sceneManager.addGameScene(new InventoryScene(production));
            sceneManager.addGameScene(new ReportScene(production));
            sceneManager.addGameScene(new TechnologyScene(production));
            productionRenderer = new ProductionRenderer(production, initialCellWidth, initialCellHeight);
            inputHandler = new InputHandler(this, production, productionRenderer);
            ProductionSceneUI.buildUI(this, getResources(), getSceneWidget(), production);
            blocksPanel = ProductionSceneUI.getBlocksPanel();
            modePanel = ProductionSceneUI.getModePanel();
            adjustmentPanel = ProductionSceneUI.getAdjustmentPanel();
            if (production.isBlockSelected()) {
                adjustmentPanel.showBlockInfo(production.getSelectedBlock());
            }
            initialized = true;
        }

        // Включить доступные машины на этом уровне
        blocksPanel.updatePermissions(production.getLevel());

    }


    @Override
    public void changeScene() { }

    @Override
    public void disposeScene() {
        production.clearBlocks();
    }

    @Override
    public void updateScene(float deltaTimeNs) {

        production.process();

        double currentCashBalance = production.getCashBalance();
        if ((long)lastCashBalance != (long)currentCashBalance) {
            // fixme публиковать информаци на главной панели
            /**
            Button balance = ProductionSceneUI.getBalance();
            LevelFactory lm = LevelFactory.getInstance();
            Level level = lm.getLevel(production.getLevel());
            String goal = level.getDescription();
            // todo здесь может съедаться память если не использовать StringBuffer (если только уже это компилятор не недлает)
            balance.setText("Level " + production.getLevel() + " Day " + (production.getCurrentCycle() / 60) + " " +
                    Utils.moneyFormat(Math.round(production.getCashBalance())) + "\n" + goal);**/
            lastCashBalance = currentCashBalance;
        }

        // todo эту проверку можно делать реже
        // Проверить не сменился ли уровень (обновить доступ к кнопкам)
        if (currentLevel != production.getLevel()) {
            currentLevel = production.getLevel();
            // Включить доступные машины на этом уровне
            blocksPanel.updatePermissions(production.getLevel());
        }
    }

    @Override
    public void preRender(Camera camera) {
        productionRenderer.draw(camera);
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
        GraphicsRender.setColor(0,0,0,1);
        GraphicsRender.drawText(fps, x + 750,y + 20, 1.2f);
    }

    @Override
    public void onMotion(MotionEvent event, float worldX, float worldY) {
        inputHandler.onMotion(event, worldX, worldY);
    }

    @Override
    public void onScale(ScaleEvent event, float worldX, float worldY) {
        inputHandler.onScale(event, worldX, worldY);
    }


    public Production getProduction() {
        return production;
    }

    public ProductionRenderer getProductionRenderer() {
        return productionRenderer;
    }

    public InputHandler getInputHandler() {
        return inputHandler;
    }


}
