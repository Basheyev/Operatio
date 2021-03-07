package com.axiom.operatio.scenes.common;

import android.graphics.Color;

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

    private Button menuButton;
    private Button optionsButton;

    private StringBuffer timeString;
    private StringBuffer balanceString;
    private double lastBalance = 0;
    private long lastDay = 0;

    private int tickSound;

    private static final int panelColor = 0xCC505050;
    private static final String MENU = MainMenuScene.SCENE_NAME;
    private static final String INVENTORY = InventoryScene.SCENE_NAME;
    private static final String PRODUCTION = ProductionScene.SCENE_NAME;
    private static final String TECHNOLOGY = "technology";
    private static final String REPORT = ReportScene.SCENE_NAME;
    private static final String OPTIONS = "options";


    public ScenesPanel(Production production) {
        super();
        buildUI();
        this.production = production;
    }


    private void buildUI() {

        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);
        setColor(panelColor);
        setLocalBounds(0, 960, 1920, 120);

        timeCaption = new Caption("time");
        timeCaption.setLocalBounds(384, 20, 256, 80);
        timeCaption.setTextColor(Color.WHITE);
        timeCaption.setTextScale(1.5f);
        timeString = new StringBuffer(64);
        addChild(timeCaption);

        balanceCaption = new Caption("balance");
        balanceCaption.setLocalBounds(1500, 20, 256, 80);
        balanceCaption.setTextColor(Color.WHITE);
        balanceCaption.setTextScale(1.5f);
        balanceString = new StringBuffer(64);
        addChild(balanceCaption);

        menuButton = buildButton(0, MENU);
        menuButton.setLocation(0,0);
        menuButton.setClickListener(listener);
        addChild(menuButton);

        inventoryButton = buildButton(4, INVENTORY);
        inventoryButton.setLocation(718, 0);
        inventoryButton.setClickListener(listener);
        addChild(inventoryButton);

        productionButton = buildButton(5, PRODUCTION);
        productionButton.setLocation(862, 0);
        productionButton.setClickListener(listener);
        addChild(productionButton);

        technologyButton = buildButton(6, TECHNOLOGY);
        technologyButton.setLocation(1006, 0);
        technologyButton.setClickListener(listener);
        addChild(technologyButton);

        reportButton = buildButton(7, REPORT);
        reportButton.setLocation(1150, 0);
        reportButton.setClickListener(listener);
        addChild(reportButton);

        optionsButton = buildButton(1, OPTIONS);
        optionsButton.setLocation(1792, 0);
        optionsButton.setClickListener(listener);
        addChild(optionsButton);

    }


    private Button buildButton(int spriteIndex, String tag) {
        Sprite icon = new Sprite(SceneManager.getResources(), R.drawable.ui_icons, 4, 4);
        icon.setActiveFrame(spriteIndex);
        Button button = new Button(icon);
        button.setSize(128, 128);
        button.setColor(Color.GRAY);
        button.setTextColor(1,1,1,1);
        button.setTag(tag);
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

            if (tag.equals(MENU)) {
                if (activeScene instanceof ProductionScene) {
                    ((ProductionScene) activeScene).pause();
                } else production.setPaused(true);
                changeScene(MENU);
            }
            else if (tag.equals(INVENTORY)) changeScene(INVENTORY);
            else if (tag.equals(PRODUCTION)) changeScene(PRODUCTION);
            else if (tag.equals(TECHNOLOGY)) changeScene(TECHNOLOGY);
            else if (tag.equals(REPORT)) changeScene(REPORT);
            else if (tag.equals(OPTIONS)) changeScene(OPTIONS);
        }
    };


    private void changeScene(String sceneName) {
        SceneManager sceneManager = SceneManager.getInstance();
        GameScene currentScene = sceneManager.getActiveScene();
        if (currentScene.getSceneName().equals(sceneName)) return;

       // SoundRenderer.playSound(tickSound);
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
    }

}
