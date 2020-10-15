package com.axiom.operatio.scenes.production.view;

import android.content.res.Resources;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.scenes.production.ProductionScene;

// TODO Просмотр материалов при выборе с Буффером (для быстрого добавления)
public class ProductionSceneUI {

    protected static Production production;
    protected static ProductionScene productionScene;
    protected static Button pauseButton;
    protected static BlocksPanel blocksPanel;
    protected static ModePanel modePanel;
    protected static OperationPanel operationPanel;
    protected static int tickSound;

    public static void setPausedButtonState(boolean paused) {
        if (paused) {
            pauseButton.setText("PAUSE");
            pauseButton.setTextColor(1,1,1,1);
            pauseButton.setColor(1,0,0,1);
        } else {
            pauseButton.setText("PLAY");
            pauseButton.setTextColor(0,0,0,1);
            pauseButton.setColor(0,1,0,1);
        }
    }


    public static void buildUI(ProductionScene scene, final Resources resources, Widget widget, Production prod) {

        production = prod;
        productionScene = scene;
        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);

        ClickListener pauseListener = new ClickListener() {
            @Override
            public void onClick(Widget w) {
                SoundRenderer.playSound(tickSound);
                if (production.isPaused()) {
                    productionScene.getInputHandler().invalidateAllActions();
                    production.setPaused(false);
                    setPausedButtonState(false);
                } else {
                    productionScene.getInputHandler().invalidateAllActions();
                    production.setPaused(true);
                    setPausedButtonState(true);
                }
            }
        };

        ClickListener exitListener = new ClickListener() {
            @Override
            public void onClick(Widget w) {
                SoundRenderer.playSound(tickSound);
                if (!production.isPaused()) {
                    production.setPaused(true);
                    ProductionSceneUI.setPausedButtonState(true);
                }
                SceneManager.getInstance().setActiveScene("Menu");
            }
        };

        Button exitButton = new Button("Menu");
        exitButton.setTextColor(1,1,1,1);
        exitButton.setLocalBounds(0,960,340,100);
        exitButton.setColor(0.8f, 0.5f, 0.5f, 0.9f);
        exitButton.setClickListener(exitListener);
        widget.addChild(exitButton);

        Button warehouseButton = new Button("Warehouse");
        warehouseButton.setTextColor(1,1,1,1);
        warehouseButton.setLocalBounds(Camera.WIDTH - 375,960,375,100);
        warehouseButton.setColor(0.8f, 0.5f, 0.5f, 0.9f);
        warehouseButton.setClickListener(exitListener);
        widget.addChild(warehouseButton);

        blocksPanel = new BlocksPanel(scene);
        widget.addChild(blocksPanel);

        modePanel = new ModePanel();
        widget.addChild(modePanel);

        operationPanel = new OperationPanel();
        operationPanel.hideBlockInfo();
        widget.addChild(operationPanel);

        pauseButton = new Button("PLAY");
        pauseButton.setTextColor(0,0,0,1);
        pauseButton.setColor(0,1,0,1);
        pauseButton.setLocalBounds(Camera.WIDTH-375, 0, 375, 140);
        pauseButton.setClickListener(pauseListener);
        production.setPaused(true);
        setPausedButtonState(true);
        widget.addChild(pauseButton);
    }

    public static BlocksPanel getBlocksPanel() {
        return blocksPanel;
    }

    public static ModePanel getModePanel() {
        return modePanel;
    }

    public static OperationPanel getOperationPanel() { return operationPanel; }

}
