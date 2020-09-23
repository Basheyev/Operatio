package com.axiom.operatio.scenes.mainmenu;

import android.view.MotionEvent;

import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;

public class MainMenuScene extends GameScene {

    @Override
    public String getSceneName() {
        return "Menu";
    }

    @Override
    public void startScene() {
        Widget widget = getSceneWidget();
        MenuPanel menuPanel = new MenuPanel();
        widget.addChild(menuPanel);
    }

    @Override
    public void disposeScene() {

    }

    @Override
    public void updateScene(float deltaTime) {

    }

    @Override
    public void preRender(Camera camera) {
        GraphicsRender.clear();
    }

    @Override
    public void postRender(Camera camera) {

    }

    @Override
    public void onMotion(MotionEvent event, float worldX, float worldY) {

    }
}
