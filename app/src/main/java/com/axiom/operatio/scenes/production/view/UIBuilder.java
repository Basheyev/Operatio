package com.axiom.operatio.scenes.production.view;

import android.content.res.Resources;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.Production;

public class UIBuilder {

    protected static Production production;
    protected static Button pauseButton;
    protected static BlocksPanel blocksPanel;
    protected static ModePanel editorPanel;
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


    public static void buildUI(final Resources resources, Widget widget, Production prod) {

        production = prod;

        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);

        ClickListener pauseListener = new ClickListener() {
            @Override
            public void onClick(Widget w) {
                SoundRenderer.playSound(tickSound);
                if (production.isPaused()) {
                    production.setPaused(false);
                    setPausedButtonState(false);
                } else {
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
                    UIBuilder.setPausedButtonState(true);
                }
                SceneManager.getInstance().setActiveScene("Menu");
            }
        };

        pauseButton = new Button("PLAY");
        pauseButton.setTextColor(0,0,0,1);
        pauseButton.setColor(0,1,0,1);
        pauseButton.setLocalBounds(Camera.WIDTH-250, 0, 250, 140);
        pauseButton.setClickListener(pauseListener);
        widget.addChild(pauseButton);

        Button exitButton = new Button("Exit");
        exitButton.setTextColor(1,1,1,1);
        exitButton.setLocalBounds(1700,960,200,100);
        exitButton.setColor(0.8f, 0.5f, 0.5f, 0.9f);
        exitButton.setClickListener(exitListener);
        widget.addChild(exitButton);

        blocksPanel = new BlocksPanel();
        widget.addChild(blocksPanel);

        editorPanel = new ModePanel();
        widget.addChild(editorPanel);
    }

    public static Widget getBlocksPanel() {
        return blocksPanel;
    }

    public static Widget getEditorPanel() {
        return editorPanel;
    }

}
