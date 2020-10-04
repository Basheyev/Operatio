package com.axiom.operatio.scenes.production.view;

import android.content.res.Resources;

import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Widget;

public class UIBuilder {

    protected static BlocksPanel blocksPanel;
    protected static ModePanel editorPanel;

    public static void buildUI(final Resources resources, Widget widget) {

        ClickListener exitListener = new ClickListener() {
            @Override
            public void onClick(Widget w) {
                SceneManager.getInstance().setActiveScene("Menu");
            }
        };

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
