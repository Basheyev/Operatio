package com.axiom.operatio.scenes.report;

import android.graphics.Color;
import android.view.MotionEvent;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.BatchRender;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.scenes.common.DebugInfo;
import com.axiom.operatio.scenes.common.ScenesPanel;
import com.axiom.operatio.scenes.mainmenu.MainMenuScene;
import com.axiom.operatio.scenes.production.ProductionScene;
import com.axiom.operatio.scenes.production.ProductionSceneUI;

public class ReportScene extends GameScene {

    public static final int UPDATE_PERIOD = 3; // Cycles
    public static final String SCENE_NAME = "Report";

    private boolean initialized = false;
    private Production production;
    private ScenesPanel scenesPanel;
    private ReportPanel reportPanel;
    private Sprite background;
    private int tickSound;
    private long lastCycle;

    public ReportScene(Production production) {
        this.production = production;
    }

    @Override
    public String getSceneName() {
        return SCENE_NAME;
    }

    @Override
    public void startScene() {
        if (!initialized) buildUI();
        scenesPanel.updatePlayButtonState();
    }

    @Override
    public void disposeScene() {

    }

    @Override
    public void changeScene(String nextScene) {
        SceneManager.getInstance().getScene(ProductionScene.SCENE_NAME).changeScene(nextScene);
    }

    @Override
    public void updateScene(float deltaTime) {
        production.getMarket().process();
        production.process();
        long currentCycle = production.getCurrentCycle();
        if (currentCycle - lastCycle > UPDATE_PERIOD) {
            reportPanel.updateData();
            lastCycle = currentCycle;
        }
    }


    @Override
    public void render(Camera camera) {
        background.setZOrder(0);
        background.draw(camera,camera.getMinX(),camera.getMinY(), Camera.WIDTH,Camera.HEIGHT);
        DebugInfo.drawDebugInfo(camera, Color.WHITE);
    }

    @Override
    public void onMotion(MotionEvent event, float worldX, float worldY) {

    }

    private void buildUI() {
        background = new Sprite(SceneManager.getResources(), R.drawable.bck_report);
        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);

        Widget widget = getSceneWidget();

        scenesPanel = new ScenesPanel(production);
        widget.addChild(scenesPanel);

        reportPanel = new ReportPanel(production);
        widget.addChild(reportPanel);

        initialized = true;
    }

}
