package com.axiom.operatio.scenes.inventory;

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
import com.axiom.operatio.model.gameplay.Utils;
import com.axiom.operatio.model.market.Market;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.scenes.common.ScenesPanel;
import com.axiom.operatio.scenes.production.ProductionScene;

/**
 * Сцена склада
 */
public class InventoryScene extends GameScene {

    public static final String SCENE_NAME = "Inventory";

    protected boolean initialized = false;
    protected Production production;
    protected ScenesPanel scenesPanel;
    protected MaterialsPanel materialsPanel;
    protected TechnologyPanel technologyPanel;
    protected MarketPanel marketPanel;
    protected int currentLevel = -1;
    protected static Sprite background;
    protected static int tickSound;
    private long lastTime;

    public InventoryScene(Production production) {
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
        materialsPanel.updateData();
    }

    @Override
    public void changeScene() {

    }

    @Override
    public void disposeScene() {

    }

    @Override
    public void updateScene(float deltaTime) {
        Market market = production.getMarket();

        long now = System.currentTimeMillis();

        market.process();
        production.process();

        // если прошла секунда времени
        if (now - lastTime > 1000) {
            materialsPanel.updateData();
            marketPanel.updateValues();
            lastTime = now;
        }

        // Если сменился уровень
        if (currentLevel != production.getLevel()) {
            currentLevel = production.getLevel();
            // Включить доступные машины на этом уровне
            materialsPanel.updatePermissions(production.getLevel());
        }
    }

    @Override
    public void preRender(Camera camera) {
        background.zOrder = 0;
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

        background = new Sprite(SceneManager.getResources(), R.drawable.background);
        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);

        Widget widget = getSceneWidget();

        materialsPanel = new MaterialsPanel(production,this);
        widget.addChild(materialsPanel);

        technologyPanel = new TechnologyPanel(materialsPanel);
        widget.addChild(technologyPanel);

        marketPanel = new MarketPanel(
                materialsPanel, production.getMarket(),
                production, production.getInventory());
        widget.addChild(marketPanel);

        scenesPanel = new ScenesPanel(production);
        widget.addChild(scenesPanel);

        initialized = true;

    }


}
