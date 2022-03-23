package com.axiom.operatio.scenes.mainmenu;

import android.graphics.Color;
import android.view.MotionEvent;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.gles2d.Texture;
import com.axiom.atom.engine.graphics.renderers.Circle;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.widgets.CheckBox;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.gameplay.GameSaveLoad;
import com.axiom.operatio.scenes.common.DebugInfo;

/**
 * Сцена главного меню
 */
public class MainMenuScene extends GameScene {

    public static final String SCENE_NAME = "Menu";
    private GameSaveLoad gameSaveLoad;
    private Sprite logo,  background;
    private MenuPanel menuPanel;
    private SlotsPanel slotsPanel;
    private StoryPanel storyPanel;
    private CheckBox musicCheckbox;
    private float scrollerX;

    private Circle circle;   // fixme just Circle test

    @Override
    public String getSceneName() {
        return SCENE_NAME;
    }

    @Override
    public void startScene() {
        if (menuPanel==null) {
            background = new Sprite(SceneManager.getResources(), R.drawable.bck_menu);

            Texture logoTexture = Texture.getInstance(SceneManager.getResources(), R.drawable.logo, true);
            logo = new Sprite(logoTexture,1,1);

            Widget widget = getSceneWidget();
            gameSaveLoad = new GameSaveLoad();
            menuPanel = new MenuPanel(this);
            widget.addChild(menuPanel);

            musicCheckbox = new CheckBox("Enable music/sound", true);
            musicCheckbox.setTextColor(Color.WHITE);
            musicCheckbox.setLocalBounds(menuPanel.getX() + menuPanel.getWidth() + 50, 150, 600, 80);
            musicCheckbox.setClickListener(e -> {
                float level = musicCheckbox.isChecked() ? 1.0f : 0.0f;
                SoundRenderer.setVolume(level);
            });
            widget.addChild(musicCheckbox);

            storyPanel = new StoryPanel();
            storyPanel.setZOrder(menuPanel.getZOrder());
            storyPanel.setLocalBounds(menuPanel.getX() + menuPanel.getWidth() + 50, 290, 980, 560);
            widget.addChild(storyPanel);

            slotsPanel = new SlotsPanel(this);
            slotsPanel.setVisible(false);
            widget.addChild(slotsPanel);
            slotsPanel.setZOrder(storyPanel.getZOrder() + 100);

            circle = new Circle();
            circle.setZOrder(Widget.UI_LAYER + 1000);
            circle.setColor(1,1,1,0.5f);

            // сформировать лист проигрывания
            SoundRenderer.addTrack(R.raw.music00);
            SoundRenderer.addTrack(R.raw.music01);
            SoundRenderer.addTrack(R.raw.music02);
            SoundRenderer.addTrack(R.raw.music03);
            SoundRenderer.addTrack(R.raw.music04);
        }
        scrollerX = Camera.WIDTH;
        menuPanel.updateUI();

        // start music
        if (!SoundRenderer.isTrackPlaying()) {
            SoundRenderer.setTrackShuffling(true);
            SoundRenderer.playNextTrack();
        }
    }

    @Override
    public void changeScene(String nextScene) {

    }

    @Override
    public void disposeScene() {
        SoundRenderer.stopTrack();
    }

    @Override
    public void updateScene(float deltaTime) {
        scrollerX -= 20 * deltaTime;
        if (scrollerX<=0) scrollerX=Camera.WIDTH;
    }



    @Override
    public void render(Camera camera) {
        float cx = camera.getMinX();
        float cy = camera.getMinY();
        background.setZOrder(0);
        background.draw(camera,cx + scrollerX, cy, Camera.WIDTH,Camera.HEIGHT);
        background.draw(camera, cx + scrollerX - Camera.WIDTH, cy, Camera.WIDTH,Camera.HEIGHT);
        logo.setZOrder(3);
        logo.draw(camera, cx + 50, cy + Camera.HEIGHT - logo.getHeight() - 25, logo.getWidth(), logo.getHeight());

        for (int i=0; i<10; i++) {
            circle.draw(camera, Camera.WIDTH - 120 * i, Camera.HEIGHT - 120, 100, 100);
        }

        DebugInfo.drawDebugInfo(camera, Color.WHITE);
    }

    @Override
    public void onMotion(MotionEvent event, float worldX, float worldY) {

    }


    public MenuPanel getMenuPanel() {
        return menuPanel;
    }


    public SlotsPanel getSlotsPanel() {
        return slotsPanel;
    }


    public GameSaveLoad getGameSaveLoad() {
        return gameSaveLoad;
    }

}
