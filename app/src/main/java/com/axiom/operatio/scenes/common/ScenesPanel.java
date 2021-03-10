package com.axiom.operatio.scenes.common;

import android.graphics.Color;
import android.view.MotionEvent;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Caption;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.gameplay.Ledger;
import com.axiom.operatio.model.gameplay.Utils;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.scenes.report.ReportScene;
import com.axiom.operatio.scenes.inventory.InventoryScene;
import com.axiom.operatio.scenes.mainmenu.MainMenuScene;
import com.axiom.operatio.scenes.production.ProductionScene;
import com.axiom.operatio.scenes.technology.TechnologyScene;

/**
 * Панель переключения между сценами
 */
public class ScenesPanel extends Panel {

    private Production production;

    private Button inventoryButton;
    private Button productionButton;
    private Button technologyButton;
    private Button reportButton;

    private Caption timeCaption;
    private Caption balanceCaption;
    private Button coinButton;

    private Button menuButton;
    private Button pauseButton;

    private double lastBalance = 0;
    private long lastDay = 0;

    private int tickSound;

    private static final int panelColor = 0xCC505050;
    private static final String MENU = MainMenuScene.SCENE_NAME;
    private static final String INVENTORY = InventoryScene.SCENE_NAME;
    private static final String PRODUCTION = ProductionScene.SCENE_NAME;
    private static final String TECHNOLOGY = TechnologyScene.SCENE_NAME;
    private static final String REPORT = ReportScene.SCENE_NAME;
    private static final String PAUSE = "pause";


    public ScenesPanel(Production production) {
        super();
        buildUI();
        this.production = production;
    }


    private void buildUI() {
        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);
        setColor(panelColor);
        setLocalBounds(0, 960, 1920, 120);

        timeCaption = buildCaption("time", 384, 20, 256, 80);
        balanceCaption = buildCaption("balance", 1500, 20, 256, 80);
        coinButton = buildButton(15, "Coin", 1410, 8, 96, 96, false);
        coinButton.setColor(0,0,0, 0);

        menuButton = buildButton(0, MENU, 24, 0, 128, 128, true);
        inventoryButton = buildButton(4, INVENTORY, 680, 0, 128, 128, true);
        productionButton = buildButton(5, PRODUCTION, 824, 0, 128, 128, true);
        technologyButton = buildButton(6, TECHNOLOGY, 968, 0, 128, 128, true);
        reportButton = buildButton(7, REPORT, 1112, 0, 128, 128, true);
        pauseButton = buildButton(2, PAUSE, 1768, 0, 128, 128, true);

    }

    private Caption buildCaption(String txt, float x, float y, float w, float h) {
        Caption caption = new Caption(txt);
        caption.setLocalBounds(x, y, w, h);
        caption.setTextColor(Color.WHITE);
        caption.setTextScale(1.5f);
        addChild(caption);
        return caption;
    }

    private Button buildButton(int spriteIndex, String tag, float x, float y, float w, float h, boolean addListener) {
        Sprite icon = new Sprite(SceneManager.getResources(), R.drawable.ui_icons, 4, 4);
        icon.setActiveFrame(spriteIndex);
        Button button = new Button(icon);
        button.setLocation(x, y);
        button.setSize(w, h);
        button.setColor(Color.GRAY);
        button.setTextColor(1,1,1,1);
        if (addListener) button.setClickListener(listener);
        button.setTag(tag);
        addChild(button);
        return button;
    }


    @Override
    public void draw(Camera camera) {
        double currentBalance = Math.round(production.getCashBalance());
        long currentDay = production.getCurrentCycle() / Ledger.OPERATIONAL_DAY_CYCLES;
        if (currentBalance != lastBalance || currentDay != lastDay) {
            timeCaption.setText("Day: " + currentDay);
            balanceCaption.setText(Utils.moneyFormat(currentBalance));
            lastBalance = currentBalance;
            lastDay = currentDay;
        }
        highlightButton();
        super.draw(camera);
    }


    protected ClickListener listener = new ClickListener() {
        @Override
        public void onClick(Widget w) {
            SoundRenderer.playSound(tickSound);
            String tag = w.getTag();

            GameScene activeScene = SceneManager.getInstance().getActiveScene();
            // Если выполнялось какое-то действие в производстве - отменяем
            if (activeScene instanceof ProductionScene) {
                ((ProductionScene) activeScene).getInputHandler().invalidateAllActions();
            }

            if (tag.equals(MENU)) {
                production.setPaused(true);
                changeScene(MENU);
            }
            else if (tag.equals(INVENTORY)) changeScene(INVENTORY);
            else if (tag.equals(PRODUCTION)) changeScene(PRODUCTION);
            else if (tag.equals(TECHNOLOGY)) changeScene(TECHNOLOGY);
            else if (tag.equals(REPORT)) changeScene(REPORT);
            else if (tag.equals(PAUSE)) {
                if (production.isPaused()) {
                    production.setPaused(false);
                    setPausedButtonState(false);
                } else {
                    production.setPaused(true);
                    setPausedButtonState(true);
                }
            }
        }
    };


    private void changeScene(String sceneName) {
        SceneManager sceneManager = SceneManager.getInstance();
        GameScene currentScene = sceneManager.getActiveScene();
        if (currentScene.getSceneName().equals(sceneName)) return;
        sceneManager.setActiveScene(sceneName);
    }


    private void highlightButton() {
        GameScene activeScene = SceneManager.getInstance().getActiveScene();
        String currentScene = activeScene.getSceneName();

        inventoryButton.setColor(Color.GRAY);
        productionButton.setColor(Color.GRAY);
        technologyButton.setColor(Color.GRAY);
        reportButton.setColor(Color.GRAY);

        if (currentScene.equals(INVENTORY)) inventoryButton.setColor(Color.RED);
        else if (currentScene.equals(PRODUCTION)) productionButton.setColor(Color.RED);
        else if (currentScene.equals(TECHNOLOGY)) technologyButton.setColor(Color.RED);
        else if (currentScene.equals(REPORT)) reportButton.setColor(Color.RED);

        setPausedButtonState(production.isPaused());
    }


    public void setPausedButtonState(boolean paused) {
        Sprite buttonSprite = pauseButton.getBackground();
        if (paused) {
            buttonSprite.setActiveFrame(3);
            pauseButton.setTextColor(1,1,1,1);
            pauseButton.setColor(1,0,0,1);
        } else {
            buttonSprite.setActiveFrame(2);
            pauseButton.setTextColor(0,0,0,1);
            pauseButton.setColor(0,1,0,1);
        }
    }


    @Override
    public boolean onMotionEvent(MotionEvent event, float worldX, float worldY) {
        GameScene scene = SceneManager.getInstance().getActiveScene();
        if (scene instanceof ProductionScene) {
            ProductionScene productionScene = (ProductionScene) scene;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                productionScene.getInputHandler().invalidateAllActions();
            }
        }
        return super.onMotionEvent(event, worldX, worldY);
    }

}