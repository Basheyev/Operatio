package com.axiom.operatio.scenes;

import android.content.res.Resources;
import android.graphics.Color;
import android.transition.Scene;
import android.util.Log;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;

public class UIBuilder {

    public static void buildUI(final Resources resources, Widget widget) {

        ClickListener listener = new ClickListener() {
            @Override
            public void onClick(Widget w) {
                Log.i("INPUT", "BUTTON PRESSED " + w.toString());
            }
        };

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

        Widget panel = new Panel();
        panel.setLocalBounds(0,0,180,1080);
        panel.setClickListener(listener);
        widget.addChild(panel);

        Widget button1 = new Button(new Sprite(resources, R.drawable.material), "BTN 1");
        button1.setLocalBounds(20,940,140,120);
        button1.setClickListener(listener);
        panel.addChild(button1);

        Widget button2 = new Button("BTN 2");
        button2.setLocalBounds(20,800,140,120);
        button2.setClickListener(listener);
        panel.addChild(button2);

        String str = null;
        Widget button3 = new Button(str);
        button3.setLocalBounds(20,660,140,120);
        button3.setClickListener(listener);
        panel.addChild(button3);

        Widget button4 = new Button(str);
        button4.setLocalBounds(20,520,140,120);
        button4.setClickListener(listener);
        panel.addChild(button4);

        Widget button5 = new Button(str);
        button5.setLocalBounds(20,380,140,120);
        button5.setClickListener(listener);
        panel.addChild(button5);

        Widget button6 = new Button(str);
        button6.setLocalBounds(20,240,140,120);
        button6.setClickListener(listener);
        panel.addChild(button6);

        Widget button7 = new Button(str);
        button7.setLocalBounds(20,100,140,120);
        button7.setClickListener(listener);
        panel.addChild(button7);

    }

}
