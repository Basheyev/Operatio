package com.basheyev.operatio.scenes.technology;

import android.graphics.Color;
import android.view.MotionEvent;

import com.basheyev.atom.R;
import com.basheyev.atom.engine.core.GameLoop;
import com.basheyev.atom.engine.core.GameScene;
import com.basheyev.atom.engine.core.SceneManager;
import com.basheyev.atom.engine.data.events.GameEvent;
import com.basheyev.atom.engine.data.events.GameEventSubscriber;
import com.basheyev.atom.engine.graphics.gles2d.Camera;
import com.basheyev.atom.engine.graphics.renderers.Sprite;
import com.basheyev.atom.engine.sound.SoundRenderer;
import com.basheyev.atom.engine.ui.widgets.Widget;
import com.basheyev.operatio.model.gameplay.OperatioEvents;
import com.basheyev.operatio.model.market.Market;
import com.basheyev.operatio.model.production.Production;
import com.basheyev.operatio.scenes.common.DebugInfo;
import com.basheyev.operatio.scenes.common.ScenesPanel;

public class TechnologyScene extends GameScene implements GameEventSubscriber {

    public static final String SCENE_NAME = "Technology";

    private boolean initialized = false;
    private Production production;
    private ScenesPanel scenesPanel;
    private MaterialsTree materialsTree;
    private RecipePanel recipePanel;
    private static Sprite background;
    private static int tickSound;
    private long lastTime;
    private int currentLevel = -1;

    public TechnologyScene(Production production) {
        this.production = production;
    }

    @Override
    public String getSceneName() {
        return SCENE_NAME;
    }

    @Override
    public void startScene() {
        if (!initialized) buildUI();
        Market market = production.getMarket();
        market.process();
        materialsTree.updateData();
        scenesPanel.updatePlayButtonState();
    }

    @Override
    public void changeScene(String nextScene) {

    }

    @Override
    public void disposeScene() {

    }

    @Override
    public void updateScene(float deltaTime) {

        long now = System.currentTimeMillis();

        Market market = production.getMarket();
        market.process();
        production.process();

        // если прошла секунда времени
        if (now - lastTime > 1000) {
            materialsTree.updateData();
            lastTime = now;
        }


        // Если сменился уровень
        if (currentLevel != production.getCurrentMissionID()) {
            currentLevel = production.getCurrentMissionID();
            // Включить доступные машины на этом уровне
            materialsTree.updatePermissions();
        }

    }


    @Override
    public boolean onGameEvent(GameEvent event) {
        switch (event.getTopic()) {
            case OperatioEvents.MISSION_COMPLETED:
                materialsTree.updatePermissions();
                break;
            default:
        }
        return false;
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

    protected void buildUI() {

        background = new Sprite(SceneManager.getResources(), R.drawable.bck_technology);
        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);

        Widget widget = getSceneWidget();

        scenesPanel = new ScenesPanel(production);
        widget.addChild(scenesPanel);

        materialsTree = new MaterialsTree(production, this);
        widget.addChild(materialsTree);

        recipePanel = new RecipePanel(materialsTree, production);
        widget.addChild(recipePanel);

        initialized = true;

        GameLoop.getInstance().addGameEventSubscriber(this);

    }


    public ScenesPanel getScenesPanel() {
        return scenesPanel;
    }


    public MaterialsTree getMaterialsTree() {
        return materialsTree;
    }


    public RecipePanel getRecipePanel() {
        return recipePanel;
    }
}
