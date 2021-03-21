package com.axiom.operatio.scenes.technology;

import android.view.MotionEvent;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.BatchRender;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.market.Market;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.scenes.common.ScenesPanel;

public class TechnologyScene extends GameScene {

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
    }

    @Override
    public void changeScene() {

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
        if (currentLevel != production.getLevel()) {
            currentLevel = production.getLevel();
            // Включить доступные машины на этом уровне
            materialsTree.updatePermissions();
        }

    }

    @Override
    public void preRender(Camera camera) {
        background.setZOrder(0);
        background.draw(camera,camera.getMinX(),camera.getMinY(), Camera.WIDTH,Camera.HEIGHT);
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
        GraphicsRender.setColor(1,1,1,1);
        GraphicsRender.drawText(fps, x + 750,y + 20, 1.2f);
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
