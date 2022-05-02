package com.basheyev.operatio.scenes.winlose;


import android.view.MotionEvent;

import com.basheyev.atom.R;
import com.basheyev.atom.engine.core.GameLoop;
import com.basheyev.atom.engine.core.GameScene;
import com.basheyev.atom.engine.core.SceneManager;
import com.basheyev.atom.engine.data.events.GameEvent;
import com.basheyev.atom.engine.data.events.GameEventSubscriber;
import com.basheyev.atom.engine.graphics.GraphicsRender;
import com.basheyev.atom.engine.graphics.gles2d.Camera;
import com.basheyev.atom.engine.graphics.renderers.Sprite;
import com.basheyev.atom.engine.graphics.renderers.Text;
import com.basheyev.atom.engine.sound.SoundRenderer;
import com.basheyev.atom.engine.ui.listeners.ClickListener;
import com.basheyev.atom.engine.ui.widgets.Button;
import com.basheyev.atom.engine.ui.widgets.Caption;
import com.basheyev.atom.engine.ui.widgets.Panel;
import com.basheyev.atom.engine.ui.widgets.Widget;
import com.basheyev.operatio.model.gameplay.OperatioEvents;
import com.basheyev.operatio.model.production.Production;
import com.basheyev.operatio.scenes.common.DebugInfo;
import com.basheyev.operatio.scenes.production.ProductionScene;

import static android.graphics.Color.WHITE;

public class WinScene extends GameScene implements GameEventSubscriber {

    public static final String SCENE_NAME = "Win Scene";
    public static final int PANEL_COLOR = 0xCC505050;

    private Production production;
    private Sprite background;
    private Caption winCaption;
    private Sprite star;
    private Panel mainPanel;
    private Button[] starButton;
    private Button okButton;
    private int applauseSound;
    private boolean initialized = false;

    public WinScene(Production production) {
        this.production = production;
        background = new Sprite(SceneManager.getResources(), R.drawable.bck_win);
        star = new Sprite(SceneManager.getResources(), R.drawable.blocks, 8, 16);
        star.setActiveFrame(127);
        applauseSound = SoundRenderer.loadSound(R.raw.applause);
        GameLoop.getInstance().addGameEventSubscriber(this);
    }


    @Override
    public String getSceneName() {
        return SCENE_NAME;
    }

    @Override
    public void startScene() {
        if (!initialized) buildUI();
        GraphicsRender.clear();
        SoundRenderer.playSound(applauseSound);
    }

    private void buildUI() {

        mainPanel = new Panel();
        mainPanel.setColor(PANEL_COLOR);
        mainPanel.setLocalBounds(Camera.WIDTH/2 - 300, Camera.HEIGHT/2-300, 600, 600);
        getSceneWidget().addChild(mainPanel);

        float centerX = mainPanel.getWidth() / 2;
        float centerY = mainPanel.getHeight() / 2;
        float starSize = 128;
        float starPadding = 32;
        float starStride = starSize + starPadding;
        float startX = centerX - starStride * 3 / 2 + starPadding / 2;

        winCaption = new Caption("Mission completed");
        winCaption.setTextColor(WHITE);
        winCaption.setTextScale(2f);
        winCaption.setLocalBounds(startX, centerY + 150, starStride*3 - starStride/4, 100);
        winCaption.setHorizontalAlignment(Text.ALIGN_CENTER);
        mainPanel.addChild(winCaption);

        starButton = new Button[3];
        for (int i=0; i<3; i++) {
            starButton[i] = new Button(star);
            starButton[i].setLocalBounds(startX + i * starStride, centerY - starSize/4, starSize, starSize);
            starButton[i].setOpaque(false);
            mainPanel.addChild(starButton[i]);
        }

        okButton = new Button("GET REWARD");
        okButton.setTextColor(WHITE);
        okButton.setLocalBounds(centerX - 200, centerY - 200, 400, 100);
        okButton.setClickListener(new ClickListener() {
            @Override
            public void onClick(Widget w) {
                SceneManager.getInstance().setActiveScene(ProductionScene.SCENE_NAME);
            }
        });
        mainPanel.addChild(okButton);
    }

    @Override
    public void disposeScene() {

    }

    @Override
    public void changeScene(String nextScene) {

    }

    @Override
    public void updateScene(float deltaTime) {

    }


    @Override
    public void render(Camera camera) {
        background.setZOrder(0);
        background.drawExact(camera, camera.getMinX(), camera.getMinY(), camera.getMaxX(), camera.getMaxY());
        DebugInfo.drawDebugInfo(camera, WHITE);
    }

    @Override
    public void onMotion(MotionEvent event, float worldX, float worldY) {

    }


    @Override
    public boolean onGameEvent(GameEvent event) {
        if (event.getTopic()== OperatioEvents.MISSION_COMPLETED) {
            production.setPaused(true);
            sceneManager.setActiveScene(WinScene.SCENE_NAME);
        }
        return false;
    }
}
