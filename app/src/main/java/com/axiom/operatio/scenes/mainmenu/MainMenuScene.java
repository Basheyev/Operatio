package com.axiom.operatio.scenes.mainmenu;

import android.view.MotionEvent;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.BatchRender;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;

public class MainMenuScene extends GameScene {

    private Sprite background;
    private MenuPanel menuPanel;

    @Override
    public String getSceneName() {
        return "Menu";
    }

    @Override
    public void startScene() {
        if (menuPanel==null) {
            background = new Sprite(SceneManager.getResources(), R.drawable.background);
            Widget widget = getSceneWidget();
            menuPanel = new MenuPanel();
            widget.addChild(menuPanel);
        }
        //Camera.getInstance().lookAt(Camera.WIDTH/2, Camera.HEIGHT/2);
    }

    @Override
    public void disposeScene() {

    }

    @Override
    public void updateScene(float deltaTime) {

    }

    @Override
    public void preRender(Camera camera) {
        background.zOrder = 0;
        background.draw(camera,camera.getMinX(),camera.getMinY(), Camera.WIDTH,Camera.HEIGHT);
    }


    @Override
    public void postRender(Camera camera) {

    }

    @Override
    public void onMotion(MotionEvent event, float worldX, float worldY) {

    }
}
