package com.axiom.operatio.scenes.production.view;

import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;

import java.util.ArrayList;

public class ModePanel extends Panel {

    public final int panelColor = 0xCC505050;
    protected String toggledButton;

    protected ClickListener listener = new ClickListener() {
        @Override
        public void onClick(Widget w) {
            Widget parent = w.getParent();
            if (parent!=null) {
                ArrayList<Widget> children = parent.getChildren();
                if (children!=null) {
                    for (Widget widget:children) {
                        if (widget!=w) widget.setColor(Color.GRAY);
                    }
                }
            }
            if (w.getColor()==Color.GRAY) {
                w.setColor(Color.RED);
                toggledButton = w.getTag();
                Log.i("BUTTON", toggledButton);
            } else {
                w.setColor(Color.GRAY);
                toggledButton = null;
            }
        }
    };


    public ModePanel() {
        super();
        setLocalBounds(Camera.WIDTH-500,0,400,140);
        setColor(panelColor);
        buildButtons();
    }

    private void buildButtons() {
        Widget button;
        Sprite sprite;

        for (int i =0; i<3; i++) {
            sprite = new Sprite(SceneManager.getResources(), R.drawable.mode_buttons, 4, 1);
            sprite.setActiveFrame(i);
            button = new Button(sprite);
            button.setTag(""+i);
            button.setLocalBounds(30 + i * 120, 20, 100, 100);
            button.setColor(Color.GRAY);
            button.setClickListener(listener);
            this.addChild(button);
        }

    }

    public String getToggledButton() {
        return toggledButton;
    }

}
