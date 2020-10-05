package com.axiom.operatio.scenes.production.view;

import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;

import java.util.ArrayList;

public class ModePanel extends Panel {

    public final int panelColor = 0xCC505050;
    protected int tickSound;
    protected String toggledButton;

    protected ClickListener listener = new ClickListener() {
        @Override
        public void onClick(Widget w) {

            if (UIBuilder.blocksPanel.getToggledButton()!=null) {
                UIBuilder.blocksPanel.untoggleButtons();
            }

            SoundRenderer.playSound(tickSound);

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
        setLocalBounds(Camera.WIDTH-650,0,400,140);
        setColor(panelColor);
        buildButtons();
        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);
    }

    public void untoggleButtons() {
        for (Widget widget:children) {
            widget.setColor(Color.GRAY);
        }
        toggledButton = null;
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
