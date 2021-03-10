package com.axiom.operatio.scenes.report;

import android.view.MotionEvent;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.BatchRender;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.scenes.common.ScenesPanel;
import com.axiom.operatio.scenes.production.ProductionScene;

public class ReportScene extends GameScene {

    public static final String SCENE_NAME = "Report";

    private boolean initialized = false;
    private Production production;
    private ScenesPanel scenesPanel;
    private ReportPanel reportPanel;
    private Sprite background;
    private int tickSound;
    private long lastCycle;

    public ReportScene(Production production) {
        this.production = production;
    }

    @Override
    public String getSceneName() {
        return SCENE_NAME;
    }

    @Override
    public void startScene() {
        if (!initialized) buildUI();
    }

    @Override
    public void disposeScene() {

    }

    @Override
    public void changeScene() {

    }

    @Override
    public void updateScene(float deltaTime) {
        production.getMarket().process();
        production.process();
        long currentCycle = production.getCurrentCycle();
        if (currentCycle - lastCycle > 3) {
            reportPanel.updateData();
            lastCycle = currentCycle;
        }
    }

    @Override
    public void preRender(Camera camera) {
        background.zOrder = 0;
        background.draw(camera,camera.getMinX(),camera.getMinY(), Camera.WIDTH,Camera.HEIGHT);
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

    private void buildUI() {
        background = new Sprite(SceneManager.getResources(), R.drawable.background);
        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);

        Widget widget = getSceneWidget();

        scenesPanel = new ScenesPanel(production);
        widget.addChild(scenesPanel);

        reportPanel = new ReportPanel(production);
        widget.addChild(reportPanel);

        initialized = true;
    }

}