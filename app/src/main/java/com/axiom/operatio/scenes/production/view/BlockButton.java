package com.axiom.operatio.scenes.production.view;

import android.graphics.Color;
import android.util.Log;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Widget;

import java.util.ArrayList;

// TODO Drag & Drop
public class BlockButton extends Button {

    BlocksPanel panel;
    protected int tickSound;

    public BlockButton(BlocksPanel panel, int id) {
        super();

        int animation;
        if (id>=0 && id<5) {
            background = new Sprite(SceneManager.getResources(), R.drawable.blocks, 8, 8);
            animation = background.addAnimation(id * 8, id * 8 + 7, 8, true);
            background.setActiveAnimation(animation);
        } else if (id==5) {
            background = new Sprite(SceneManager.getResources(), R.drawable.buffer_texture, 4, 4);
            animation = background.addAnimation(0, 8, 8,true);
            background.setActiveAnimation(animation);
        } else if (id==6) {
            background = new Sprite(SceneManager.getResources(), R.drawable.blocks, 8, 8);
            animation = background.addAnimation(40, 47, 15,true);
            background.setActiveAnimation(animation);
        }

        setColor(0.5f, 0.7f, 0.5f, 0.9f);
        setColor(Color.GRAY);
        this.panel = panel;
        this.tag = "" + id;
        panel.addChild(this);
        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);
        setClickListener(listener);
    }


    protected ClickListener listener = new ClickListener() {
        @Override
        public void onClick(Widget w) {
            if (UIBuilder.editorPanel.getToggledButton()!=null) {
                UIBuilder.editorPanel.untoggleButtons();
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
                panel.toggledButton = w.getTag();
            } else {
                w.setColor(Color.GRAY);
                panel.toggledButton = null;
            }
        }
    };




}
