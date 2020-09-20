package com.axiom.operatio.scenes.production.view;

import android.content.res.Resources;

import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.scenes.production.view.BlocksPanel;

public class UIBuilder {

    public static Widget buildUI(final Resources resources, Widget widget) {

        ClickListener exitListener = new ClickListener() {
            @Override
            public void onClick(Widget w) {
                SceneManager.getInstance(resources).exitGame();
            }
        };

        Widget exitButton = new Button("EXIT");
        exitButton.setLocalBounds(1700,960,200,100);
        exitButton.setColor(0.8f, 0.5f, 0.5f, 0.9f);
        exitButton.setClickListener(exitListener);
        widget.addChild(exitButton);

        Widget blocksPanel = new BlocksPanel();
        widget.addChild(blocksPanel);

        Widget editorPanel = new EditorPanel();
        widget.addChild(editorPanel);

        // Вернем панельку выбора блока
        return blocksPanel;
    }

}
