package com.axiom.operatio.scenes.inventory;

import android.graphics.Color;
import android.view.MotionEvent;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameLoop;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.data.events.GameEvent;
import com.axiom.atom.engine.data.events.GameEventSubscriber;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.gameplay.OperatioEvents;
import com.axiom.operatio.model.inventory.Inventory;
import com.axiom.operatio.model.market.Market;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.scenes.common.DebugInfo;
import com.axiom.operatio.scenes.common.ScenesPanel;
import com.axiom.operatio.scenes.production.ProductionScene;

/**
 * Сцена склада
 */
public class InventoryScene extends GameScene implements GameEventSubscriber {

    public static final String SCENE_NAME = "Inventory";
    public static final long UPDATE_TIME = Production.CYCLE_TIME * 3;

    private boolean initialized = false;
    private Production production;
    private ScenesPanel scenesPanel;
    private MaterialsPanel materialsPanel;
    private MarketPanel marketPanel;
    private StockKeepingUnitPanel stockKeepingUnitPanel;
    private static Sprite background;
    private static int tickSound;
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
        materialsPanel.updatePermissions();
        materialsPanel.updateData();
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
        Market market = production.getMarket();

        long now = System.currentTimeMillis();

        market.process();
        production.process();

        // если прошла секунда времени
        if (now - lastTime > UPDATE_TIME) {
            materialsPanel.updateData();
            marketPanel.updateValues();
            stockKeepingUnitPanel.updateData();
            lastTime = now;
        }

    }


    @Override
    public boolean onGameEvent(GameEvent event) {
        switch (event.getTopic()) {
            case OperatioEvents.MATERIAL_RESEARCHED:
            case OperatioEvents.MISSION_COMPLETED:
                materialsPanel.updatePermissions();
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

        background = new Sprite(SceneManager.getResources(), R.drawable.bck_inventory);
        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);

        Widget widget = getSceneWidget();

        materialsPanel = new MaterialsPanel(this);
        widget.addChild(materialsPanel);


        marketPanel = new MarketPanel(this);
        widget.addChild(marketPanel);


        stockKeepingUnitPanel = new StockKeepingUnitPanel(this);
        widget.addChild(stockKeepingUnitPanel);

        scenesPanel = new ScenesPanel(production);
        widget.addChild(scenesPanel);

        initialized = true;

        GameLoop.getInstance().addGameEventSubscriber(this);
    }

    public MaterialsPanel getMaterialsPanel() {
        return materialsPanel;
    }

    public MarketPanel getMarketPanel() {
        return marketPanel;
    }

    public StockKeepingUnitPanel getStockKeepingUnitPanel() { return stockKeepingUnitPanel; }


    public Market getMarket() {
        return production.getMarket();
    }

    public Inventory getInventory() {
        return production.getInventory();
    }

    public Production getProduction() {
        return production;
    }
}
