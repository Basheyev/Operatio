package com.axiom.operatio.scenes.production.view;

import android.content.res.Resources;

import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.Production;

public class UIBuilder {

    protected static Production production;
    protected static Button pauseButton;
    protected static BlocksPanel blocksPanel;
    protected static ModePanel editorPanel;

    public static void buildUI(final Resources resources, Widget widget, Production prod) {

        // TODO Play/Pause button

        production = prod;

        ClickListener pauseListener = new ClickListener() {
            @Override
            public void onClick(Widget w) {
                if (production.isPaused()) {
                    production.setPaused(false);
                    pauseButton.setText(">");
                    pauseButton.setColor(0,1,0,1);
                } else {
                    production.setPaused(true);
                    pauseButton.setText("| |");
                    pauseButton.setColor(1,0,0,1);
                }
            }
        };

        ClickListener exitListener = new ClickListener() {
            @Override
            public void onClick(Widget w) {
                production.setPaused(true);
                SceneManager.getInstance().setActiveScene("Menu");
            }
        };

        pauseButton = new Button(">");
        pauseButton.setTextColor(1,1,1,1);
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
