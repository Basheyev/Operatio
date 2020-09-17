package com.axiom.operatio.scenes.ui;

import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.production.machines.MachineType;

import java.util.ArrayList;

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

        Widget panel = new BlocksPanel();
        widget.addChild(panel);

        // Вернем панельку выбора блока
        return panel;
    }

}
