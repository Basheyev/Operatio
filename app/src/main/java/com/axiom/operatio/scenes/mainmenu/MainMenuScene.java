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
        Camera.getInstance().lookAt(Camera.WIDTH/2, Camera.HEIGHT/2);
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
        background.draw(camera,0,0,1920,1080);
    }

    protected StringBuffer fps = new StringBuffer(100);
    @Override
    public void postRender(Camera camera) {
        float x = camera.getX();
        float y = camera.getY();
        fps.delete(0, fps.length());
        fps.append("FPS:").append(GraphicsRender.getFPS())
                .append(" Quads:").append(BatchRender.getEntriesCount())
                .append(" Calls:").append(BatchRender.getDrawCallsCount())
                .append(" Time:").append(GraphicsRender.getRenderTime())
                .append("ms\nand Other characters that prints here ")
                .append("\"Lorem ipsum dolor sit amet, consectetur adipiscing elit,\n" +
                        "sed do eiusmod tempor incididunt ut labore et dolore magna\n" +
                        "aliqua. Ut enim ad minim veniam, quis nostrud exercitation\n" +
                        "ullamco laboris nisi ut aliquip ex ea commodo consequat.\n" +
                        "Duis aute irure dolor in reprehenderit in voluptate velit\n" +
                        "esse cillum dolore eu fugiat nulla pariatur. Excepteur sint\n" +
                        "occaecat cupidatat non proident, sunt in culpa qui officia\n" +
                        "deserunt mollit anim id est laborum.\""
                );
        GraphicsRender.setZOrder(0);
        GraphicsRender.setColor(1,1,1,1);
        GraphicsRender.drawText(fps, x - 900,y + 460, 2f);
    }

    @Override
    public void onMotion(MotionEvent event, float worldX, float worldY) {

    }
}
