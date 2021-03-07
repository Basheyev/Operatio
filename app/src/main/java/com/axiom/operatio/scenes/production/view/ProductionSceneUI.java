package com.axiom.operatio.scenes.production.view;

import android.content.res.Resources;

import com.axiom.atom.R;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.scenes.common.ScenesPanel;
import com.axiom.operatio.scenes.production.ProductionScene;


public class ProductionSceneUI {

    protected static Production production;
    protected static ProductionScene productionScene;
    protected static Button pauseButton;
    protected static BlocksPanel blocksPanel;
    protected static ModePanel modePanel;
    protected static AdjustmentPanel adjustmentPanel;

    protected static ScenesPanel scenesPanel;

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

        blocksPanel = new BlocksPanel(scene);
        widget.addChild(blocksPanel);

        modePanel = new ModePanel();
        widget.addChild(modePanel);

        adjustmentPanel = new AdjustmentPanel(production);
        adjustmentPanel.hideBlockInfo();
        widget.addChild(adjustmentPanel);

        pauseButton = new Button("PLAY");
        pauseButton.setTextColor(0,0,0,1);
        pauseButton.setColor(0,1,0,1);
        pauseButton.setLocalBounds(Camera.WIDTH-375, 0, 375, 140);
        pauseButton.setClickListener(pauseListener);
        production.setPaused(true);
        setPausedButtonState(true);
        widget.addChild(pauseButton);

        scenesPanel = new ScenesPanel(production);
        widget.addChild(scenesPanel);

    }

    public static BlocksPanel getBlocksPanel() {
        return blocksPanel;
    }

    public static ModePanel getModePanel() {
        return modePanel;
    }

    public static AdjustmentPanel getAdjustmentPanel() { return adjustmentPanel; }

//    public static Button getBalance() { return balance; }
}
