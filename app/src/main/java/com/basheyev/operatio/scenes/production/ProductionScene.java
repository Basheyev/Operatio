package com.basheyev.operatio.scenes.production;

import android.graphics.Color;
import android.view.MotionEvent;

import com.basheyev.atom.R;
import com.basheyev.atom.engine.core.GameLoop;
import com.basheyev.atom.engine.core.GameScene;
import com.basheyev.atom.engine.data.events.GameEvent;
import com.basheyev.atom.engine.data.events.GameEventSubscriber;
import com.basheyev.atom.engine.graphics.gles2d.Camera;
import com.basheyev.atom.engine.input.ScaleEvent;
import com.basheyev.atom.engine.sound.SoundRenderer;
import com.basheyev.operatio.model.gameplay.GameMission;
import com.basheyev.operatio.model.gameplay.MissionManager;
import com.basheyev.operatio.model.gameplay.OperatioEvents;
import com.basheyev.operatio.model.production.ProductionRenderer;
import com.basheyev.operatio.model.production.Production;
import com.basheyev.operatio.scenes.common.DebugInfo;
import com.basheyev.operatio.scenes.winlose.WinScene;
import com.basheyev.operatio.scenes.production.view.HelperPanel;
import com.basheyev.operatio.scenes.report.ReportScene;
import com.basheyev.operatio.scenes.production.view.BlocksPanel;
import com.basheyev.operatio.scenes.production.controller.InputHandler;
import com.basheyev.operatio.scenes.production.view.ModePanel;
import com.basheyev.operatio.scenes.production.view.adjustment.AdjustmentPanel;
import com.basheyev.operatio.scenes.inventory.InventoryScene;
import com.basheyev.operatio.scenes.technology.TechnologyScene;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Сцена производства
 */
public class ProductionScene extends GameScene implements GameEventSubscriber {

    public static final String SCENE_NAME = "Production";

    private Production production;
    private InputHandler inputHandler;
    private ProductionRenderer productionRenderer;
    private BlocksPanel blocksPanel;
    private ModePanel modePanel;
    private HelperPanel helperPanel;
    private AdjustmentPanel adjustmentPanel;
    private int musicID;

    private boolean initialized = false;


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
            sceneManager.addGameScene(new WinScene(production));

            productionRenderer = production.getRenderer();
            inputHandler = new InputHandler(this, production, productionRenderer);
            GameLoop.getInstance().addGameEventSubscriber(this);
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
        adjustmentPanel.hideBlockInfo();
        production.unselectBlock();
        setHelperMissionText();
        ProductionSceneUI.getScenesPanel().updatePlayButtonState();

        if (!SoundRenderer.isTrackPlaying()) SoundRenderer.playNextTrack();

    }


    @Override
    public void changeScene(String nextScene) {

    }

    @Override
    public void disposeScene() {
        // Выгружаем из памяти текущие игровые сцены
        sceneManager.removeGameScene(ReportScene.SCENE_NAME);
        sceneManager.removeGameScene(TechnologyScene.SCENE_NAME);
        sceneManager.removeGameScene(InventoryScene.SCENE_NAME);
        production.clearBlocks();
        SoundRenderer.stopTrack();
    }

    @Override
    public void updateScene(float deltaTimeNs) {
        production.process();
    }


    @Override
    public boolean onGameEvent(GameEvent event) {
        switch (event.getTopic()) {
            case OperatioEvents.MACHINE_RESEARCHED:
                blocksPanel.updatePermissions();
                break;
            case OperatioEvents.MISSION_COMPLETED:
                blocksPanel.updatePermissions();
                setHelperMissionText();
            default:
        }
        return false;
    }


    @Override
    public void render(Camera camera) {
        productionRenderer.draw(camera);
        DebugInfo.drawDebugInfo(camera, Color.WHITE);
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
        GameMission mission = MissionManager.getInstance().getMission(production.getCurrentMissionID());
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
