package com.axiom.operatio.scenes.production.ui;

import android.graphics.Color;
import android.util.Log;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;

import java.util.ArrayList;

public class BlocksPanel extends Panel {

    public final int panelColor = 0xCC505050;

    protected String toggledButton;

    public BlocksPanel() {
        super();
        setLocalBounds(0,0,180,1080);
        setColor(panelColor);
        buildButtons();
    }

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


    @Override
    public void draw(Camera camera) {
        super.draw(camera);
        if (toggledButton!=null) {
            GraphicsRender.setZOrder(zOrder);
            GraphicsRender.drawText(toggledButton.toCharArray(), 50, 50, 5);
        }
    }


    private void buildButtons() {
        Sprite sprite;
        Widget button;
        int animation;

        for (int i =0; i<4; i++) {
            sprite = new Sprite(SceneManager.getResources(), R.drawable.machines, 8, 8);
            animation = sprite.addAnimation(i * 8, i * 8 + 7, 8, true);
            sprite.setActiveAnimation(animation);
            button = new Button(sprite);
            button.setTag(""+i);
            button.setLocalBounds(30, 940 - i * 140, 120, 120);
            button.setColor(Color.GRAY);
            button.setClickListener(listener);
            this.addChild(button);
        }

        int i = 4;
        sprite = new Sprite(SceneManager.getResources(), R.drawable.buffer_texture, 4, 4);
        animation = sprite.addAnimation(0, 8, 8,true);
        sprite.setActiveAnimation(animation);
        button = new Button(sprite);
        button.setTag(""+i);
        button.setLocalBounds(30, 940 - i * 140, 120, 120);
        button.setColor(Color.GRAY);
        button.setClickListener(listener);
        this.addChild(button);

        i = 5;
        sprite = new Sprite(SceneManager.getResources(), R.drawable.conveyor_texture, 4, 6);
        animation = sprite.addAnimation(0, 3, 8,true);
        sprite.setActiveAnimation(animation);
        button = new Button(sprite);
        button.setTag(""+i);
        button.setLocalBounds(30, 940 - i * 140, 120, 120);
        button.setColor(Color.GRAY);
        button.setClickListener(listener);
        this.addChild(button);

    }


    public String getToggledButton() {
        return toggledButton;
    }

}
