package com.axiom.operatio.scenes.winlose;


import android.graphics.Paint;
import android.view.MotionEvent;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameLoop;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.data.events.GameEvent;
import com.axiom.atom.engine.data.events.GameEventSubscriber;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.graphics.renderers.Text;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Caption;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.gameplay.OperatioEvents;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.scenes.common.DebugInfo;
import com.axiom.operatio.scenes.production.ProductionScene;

import static android.graphics.Color.DKGRAY;
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
    public void changeScene() {

    }

    @Override
    public void updateScene(float deltaTime) {

    }

    @Override
    public void preRender(Camera camera) {
        background.setZOrder(0);
        background.drawExact(camera, camera.getMinX(), camera.getMinY(), camera.getMaxX(), camera.getMaxY());
    }

    @Override
    public void postRender(Camera camera) {
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
