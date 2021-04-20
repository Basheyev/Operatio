package com.axiom.operatio.scenes.production;

import android.view.MotionEvent;

import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.BatchRender;
import com.axiom.atom.engine.input.ScaleEvent;
import com.axiom.operatio.model.gameplay.GameMission;
import com.axiom.operatio.model.gameplay.MissionManager;
import com.axiom.operatio.model.production.ProductionRenderer;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.scenes.production.view.HelperPanel;
import com.axiom.operatio.scenes.report.ReportScene;
import com.axiom.operatio.scenes.production.view.BlocksPanel;
import com.axiom.operatio.scenes.production.controller.InputHandler;
import com.axiom.operatio.scenes.production.view.ModePanel;
import com.axiom.operatio.scenes.production.view.adjustment.AdjustmentPanel;
import com.axiom.operatio.scenes.inventory.InventoryScene;
import com.axiom.operatio.scenes.technology.TechnologyScene;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Сцена производства
 */
public class ProductionScene extends GameScene {

    public static final String SCENE_NAME = "Production";

    private Production production;
    private InputHandler inputHandler;
    private ProductionRenderer productionRenderer;
    private BlocksPanel blocksPanel;
    private ModePanel modePanel;
    private HelperPanel helperPanel;
    private AdjustmentPanel adjustmentPanel;

    private boolean initialized = false;
    private int currentLevel = -1;
    private long permissionLastChangeTime = 0;


    public ProductionScene() {
        production = new Production();
    }


    public ProductionScene(Production production) {
        this.production = production;
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
            sceneManager.addGameScene(new TechnologyScene(production));
            sceneManager.addGameScene(new ReportScene(production));

            productionRenderer = production.getRenderer();
            inputHandler = new InputHandler(this, production, productionRenderer);
            ProductionSceneUI.buildUI(this, getResources(), getSceneWidget(), production);
            blocksPanel = ProductionSceneUI.getBlocksPanel();
            modePanel = ProductionSceneUI.getModePanel();
            adjustmentPanel = ProductionSceneUI.getAdjustmentPanel();
            helperPanel = ProductionSceneUI.getHelperPanel();
            if (production.isBlockSelected()) {
                adjustmentPanel.showBlockInfo(production.getSelectedBlock());
            }
            initialized = true;
        }

        // Включить доступные машины на этом уровне
        blocksPanel.updatePermissions();

    }


    @Override
    public void changeScene() { }

    @Override
    public void disposeScene() {
        // Выгружаем из памяти текущие игровые сцены
        sceneManager.removeGameScene(ReportScene.SCENE_NAME);
        sceneManager.removeGameScene(TechnologyScene.SCENE_NAME);
        sceneManager.removeGameScene(InventoryScene.SCENE_NAME);
        production.clearBlocks();
    }

    @Override
    public void updateScene(float deltaTimeNs) {

        production.process();

        long changeTime = production.getPermissions().getLastChangeTime();
        boolean permissionsChanged = false;
        if (changeTime > permissionLastChangeTime) {
            permissionsChanged = true;
            permissionLastChangeTime = changeTime;
        }

        // Проверить не сменился ли уровень (обновить доступ к кнопкам)
        if (currentLevel != production.getCurrentMissionID() || permissionsChanged) {
            currentLevel = production.getCurrentMissionID();
            // Включить доступные машины на этом уровне
            blocksPanel.updatePermissions();
            setHelperMissionText();
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

    public void setHelperMissionText() {
        // Если ничто не выбрано написать суть миссии
        GameMission mission = MissionManager.getMission(production.getCurrentMissionID());
        if (mission!=null) {
            String goal = "Mission #" + mission.getID() + " - " + mission.getName() + "\n\n" + mission.getDescription();;
            helperPanel.setText(goal);
        }
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


    public BlocksPanel getBlocksPanel() {
        return blocksPanel;
    }

    public ModePanel getModePanel() {
        return modePanel;
    }

    public AdjustmentPanel getAdjustmentPanel() {
        return adjustmentPanel;
    }

    public HelperPanel getHelperPanel() {
        return helperPanel;
    }
}
