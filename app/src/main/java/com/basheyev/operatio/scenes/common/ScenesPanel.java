package com.basheyev.operatio.scenes.common;

import android.content.res.Resources;
import android.graphics.Color;
import android.view.MotionEvent;

import com.basheyev.atom.R;
import com.basheyev.atom.engine.core.GameScene;
import com.basheyev.atom.engine.core.SceneManager;
import com.basheyev.atom.engine.graphics.gles2d.Camera;
import com.basheyev.atom.engine.graphics.renderers.Sprite;
import com.basheyev.atom.engine.graphics.renderers.Text;
import com.basheyev.atom.engine.sound.SoundRenderer;
import com.basheyev.atom.engine.ui.listeners.ClickListener;
import com.basheyev.atom.engine.ui.widgets.Button;
import com.basheyev.atom.engine.ui.widgets.Caption;
import com.basheyev.atom.engine.ui.widgets.Panel;
import com.basheyev.atom.engine.ui.widgets.Widget;
import com.basheyev.operatio.model.ledger.Ledger;
import com.basheyev.operatio.model.common.FormatUtils;
import com.basheyev.operatio.model.production.Production;
import com.basheyev.operatio.scenes.report.ReportScene;
import com.basheyev.operatio.scenes.inventory.InventoryScene;
import com.basheyev.operatio.scenes.mainmenu.MainMenuScene;
import com.basheyev.operatio.scenes.production.ProductionScene;
import com.basheyev.operatio.scenes.technology.TechnologyScene;

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

    private StringBuffer dayText;
    private StringBuffer balanceText;

    private Button playButton;

    private double lastBalance = 0;
    private long lastDay = 0;

    private int tickSound;

    private static final int PANEL_COLOR = 0xCC505050;
    private static final int UNSELECTED = Color.GRAY;
    private static final int SELECTED = 0xFFd5c01f;
    private static final int PAUSED = 0xFF9d3e4d;
    private static final int PLAYING = 0xFF80B380;
    private static final int TEXT_COLOR = Color.WHITE;

    private static final String MENU = MainMenuScene.SCENE_NAME;
    private static final String INVENTORY = InventoryScene.SCENE_NAME;
    private static final String PRODUCTION = ProductionScene.SCENE_NAME;
    private static final String TECHNOLOGY = TechnologyScene.SCENE_NAME;
    private static final String REPORT = ReportScene.SCENE_NAME;
    private static final String PLAY = "play";

    private static Sprite uiIcons = null;

    public ScenesPanel(Production production) {
        super();
        this.production = production;
        this.dayText = new StringBuffer(32);
        this.balanceText = new StringBuffer(32);
        buildUI();
    }


    private void buildUI() {
        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);
        setColor(PANEL_COLOR);
        setLocalBounds(0, Camera.HEIGHT - 120, Camera.WIDTH, 120);

        double currentBalance = Math.round(production.getLedger().getCashBalance());
        long currentDay = production.getCurrentCycle() / Ledger.OPERATIONAL_DAY_CYCLES;
        FormatUtils.formatMoney(currentBalance, balanceText);
        FormatUtils.formatLong(currentDay, dayText);

        lastBalance = currentBalance;

        timeCaption = buildCaption(dayText, 420, 20, 256, 80);
        Button dayButton = buildButton(14, "Day", 330, 11, 96, 96, false);
        dayButton.setColor(0,0,0, 0);
        balanceCaption = buildCaption(balanceText, 1500, 20, 256, 80);
        Button coinButton = buildButton(15, "Coin", 1410, 8, 96, 96, false);
        coinButton.setColor(0,0,0, 0);

        Button menuButton = buildButton(0, MENU, 24, 0, 128, 128, true);
        productionButton = buildButton(5, PRODUCTION,  680, 0, 128, 128,true);
        inventoryButton = buildButton(4, INVENTORY,  824, 0, 128, 128,true);
        technologyButton = buildButton(6, TECHNOLOGY, 968, 0, 128, 128, true);
        reportButton = buildButton(7, REPORT, 1112, 0, 128, 128, true);
        playButton = buildButton(1, PLAY, 1768, 0, 128, 128, true);
        playButton.setTextScale(1);
        playButton.setTextColor(Color.BLACK);

        updatePlayButtonState();

    }

    private Caption buildCaption(CharSequence txt, float x, float y, float w, float h) {
        Caption caption = new Caption(txt);
        caption.setLocalBounds(x, y, w, h);
        caption.setTextColor(TEXT_COLOR);
        caption.setTextScale(1.5f);
        caption.setVerticalAlignment(Text.ALIGN_CENTER);
        addChild(caption);
        return caption;
    }

    private Button buildButton(int spriteIndex, String tag, float x, float y, float w, float h, boolean addListener) {
        if (uiIcons==null) {
            Resources resources = SceneManager.getResources();
            uiIcons = new Sprite(resources, R.drawable.ui_icons, 4, 4);
        }
        Sprite icon;
        // Если это кнопка паузы - берем спрайт с двумя иконками Fast/Play/Pause
        if (tag.equals(PLAY)) {
            icon = uiIcons.getAsSprite(spriteIndex,spriteIndex + 2);
        } else icon = uiIcons.getAsSprite(spriteIndex);

        Button button = new Button(icon);
        button.setLocation(x, y);
        button.setSize(w, h);
        button.setColor(UNSELECTED);
        button.setTextColor(TEXT_COLOR);
        if (addListener) button.setClickListener(listener);
        button.setTag(tag);
        addChild(button);
        return button;
    }


    public void changeProductionSpeed() {
        long baseSpeed = Production.CYCLE_TIME;
        long tripleSpeed = Production.CYCLE_TIME / 3;

        if (production.isPaused()) {
            production.setPaused(false);
            production.setCycleMilliseconds(baseSpeed);
        } else {
            if (production.getCycleMilliseconds() == baseSpeed) {
                production.setCycleMilliseconds(tripleSpeed);
            } else if (production.getCycleMilliseconds() == tripleSpeed) {
                production.setPaused(true);
            }
        }
        updatePlayButtonState();
    }


    public void updatePlayButtonState() {
        Sprite buttonSprite = playButton.getBackground();
        long tripleSpeed = Production.CYCLE_TIME / 3;

        if (production.isPaused()) {
            buttonSprite.setActiveFrame(2);
            playButton.setColor(PAUSED);
        } else {
            if (production.getCycleMilliseconds() == tripleSpeed) {
                buttonSprite.setActiveFrame(0);
            } else buttonSprite.setActiveFrame(1);
            playButton.setColor(PLAYING);
        }
    }




    @Override
    public void draw(Camera camera) {
        double currentBalance = Math.round(production.getLedger().getCashBalance());
        long currentDay = production.getCurrentCycle() / Ledger.OPERATIONAL_DAY_CYCLES;

        if (currentDay != lastDay) {
            dayText.setLength(0);
            dayText.append(currentDay);
            timeCaption.setText(dayText);
            lastDay = currentDay;
        }

        if (currentBalance != lastBalance) {
            balanceText.setLength(0);
            balanceCaption.setText(FormatUtils.formatMoneyAppend(currentBalance, balanceText));
            lastBalance = currentBalance;
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
            else if (tag.equals(PLAY)) changeProductionSpeed();
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

        inventoryButton.setColor(UNSELECTED);
        productionButton.setColor(UNSELECTED);
        technologyButton.setColor(UNSELECTED);
        reportButton.setColor(UNSELECTED);

        switch (currentScene) {
            case INVENTORY:
                inventoryButton.setColor(SELECTED);
                break;
            case PRODUCTION:
                productionButton.setColor(SELECTED);
                break;
            case TECHNOLOGY:
                technologyButton.setColor(SELECTED);
                break;
            case REPORT:
                reportButton.setColor(SELECTED);
                break;
        }

        updatePlayButtonState();
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
