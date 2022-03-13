package com.axiom.operatio.scenes.production;

import android.graphics.Color;
import android.view.MotionEvent;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameLoop;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.data.events.GameEvent;
import com.axiom.atom.engine.data.events.GameEventSubscriber;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.input.ScaleEvent;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.operatio.model.gameplay.GameMission;
import com.axiom.operatio.model.gameplay.MissionManager;
import com.axiom.operatio.model.gameplay.OperatioEvents;
import com.axiom.operatio.model.production.ProductionRenderer;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.scenes.common.DebugInfo;
import com.axiom.operatio.scenes.mainmenu.MainMenuScene;
import com.axiom.operatio.scenes.winlose.WinScene;
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

            int randomize = (int) (1 + Math.round(Math.random() * 2));
            switch (randomize) {
                case 1: musicID = SoundRenderer.loadMusic(R.raw.music01); break;
                case 2: musicID = SoundRenderer.loadMusic(R.raw.music02); break;
                default: musicID = SoundRenderer.loadMusic(R.raw.music03); break;
            }

            initialized = true;
        }

        // Включить доступные машины на этом уровне
        blocksPanel.updatePermissions();
        adjustmentPanel.hideBlockInfo();
        production.unselectBlock();
        setHelperMissionText();
        ProductionSceneUI.getScenesPanel().updatePlayButtonState();

        if (!SoundRenderer.isMusicPlaying(musicID)) {
            SoundRenderer.playMusic(musicID, true);
        }
    }


    @Override
    public void changeScene(String nextScene) {
        if (nextScene.equals(MainMenuScene.SCENE_NAME)) {
            SoundRenderer.pauseMusic(musicID);
        }
    }

    @Override
    public void disposeScene() {
        // Выгружаем из памяти текущие игровые сцены
        sceneManager.removeGameScene(ReportScene.SCENE_NAME);
        sceneManager.removeGameScene(TechnologyScene.SCENE_NAME);
        sceneManager.removeGameScene(InventoryScene.SCENE_NAME);
        production.clearBlocks();
        SoundRenderer.unloadMusic(musicID);
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
                production.getCurrentMissionID();
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
