package com.axiom.operatio.scenes.production.view;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Caption;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.scenes.production.ProductionScene;


// TODO Заменить текстовые кнопки на иконки
public class ProductionSceneUI {

    protected static Production production;
    protected static ProductionScene productionScene;
    protected static Button pauseButton;
    protected static BlocksPanel blocksPanel;
    protected static ModePanel modePanel;
    protected static AdjustmentPanel adjustmentPanel;
    protected static Button balance;
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
                if (w.getTag().equals("Menu")) {
                    SoundRenderer.playSound(tickSound);
                    productionScene.getInputHandler().invalidateAllActions();
                    productionScene.pause();
                    SceneManager.getInstance().setActiveScene("Menu");
                } else if (w.getTag().equals("Inventory")) {
                    productionScene.getInputHandler().invalidateAllActions();
                    SoundRenderer.playSound(tickSound);
                    SceneManager.getInstance().setActiveScene("Inventory");
                }
            }
        };

        Button exitButton = new Button("Menu");
        exitButton.setTag("Menu");
        exitButton.setTextColor(1,1,1,1);
        exitButton.setLocalBounds(0,960,340,100);
        exitButton.setColor(0.8f, 0.5f, 0.5f, 0.9f);
        exitButton.setClickListener(exitListener);
        widget.addChild(exitButton);

        Button inventoryButton = new Button("Inventory");
        inventoryButton.setTag("Inventory");
        inventoryButton.setTextColor(1,1,1,1);
        inventoryButton.setLocalBounds(Camera.WIDTH - 375,960,375,100);
        inventoryButton.setColor(0.8f, 0.5f, 0.5f, 0.9f);
        inventoryButton.setClickListener(exitListener);
        widget.addChild(inventoryButton);

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

        balance = new Button(String.format("$%.0f", production.getCashBalance()));
        balance.setColor(0xCC505050);
        balance.setTextColor(Color.WHITE);
        balance.setTextScale(1.5f);
        balance.setLocalBounds(Camera.WIDTH/2-150, 1000, 300, 80);
        widget.addChild(balance);
    }

    public static BlocksPanel getBlocksPanel() {
        return blocksPanel;
    }

    public static ModePanel getModePanel() {
        return modePanel;
    }

    public static AdjustmentPanel getAdjustmentPanel() { return adjustmentPanel; }

    public static Button getBalance() { return balance; }
}
