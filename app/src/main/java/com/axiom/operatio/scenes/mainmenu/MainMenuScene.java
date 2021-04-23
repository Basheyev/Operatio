package com.axiom.operatio.scenes.mainmenu;

import android.view.MotionEvent;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.gles2d.Texture;
import com.axiom.atom.engine.graphics.renderers.BatchRender;
import com.axiom.atom.engine.graphics.renderers.Line;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.gameplay.GameSaveLoad;

import static android.graphics.Color.BLACK;

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
    private float scrollerX;

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

            storyPanel = new StoryPanel();
            storyPanel.setLocalBounds(menuPanel.getX() + menuPanel.getWidth() + 50, 290, 980, 560);
            widget.addChild(storyPanel);

            slotsPanel = new SlotsPanel(this);
            slotsPanel.setZOrder(storyPanel.getZOrder() + 100);
            slotsPanel.setVisible(false);
            widget.addChild(slotsPanel);
        }
        scrollerX = Camera.WIDTH;
        menuPanel.updateUI();
    }

    @Override
    public void changeScene() {

    }

    @Override
    public void disposeScene() {

    }

    @Override
    public void updateScene(float deltaTime) {
        scrollerX -= 20 * deltaTime;
        if (scrollerX<=0) scrollerX=Camera.WIDTH;
    }

    @Override
    public void preRender(Camera camera) {
        float cx = camera.getMinX();
        float cy = camera.getMinY();
        background.setZOrder(0);
        background.draw(camera,cx + scrollerX, cy, Camera.WIDTH,Camera.HEIGHT);
        background.draw(camera, cx + scrollerX - Camera.WIDTH, cy, Camera.WIDTH,Camera.HEIGHT);
        logo.setZOrder(3);
        logo.draw(camera, cx + 50, cy + Camera.HEIGHT - logo.getHeight() - 25, logo.getWidth(), logo.getHeight());
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
