package com.axiom.operatio.scenes.production.view;

import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.scenes.production.ProductionScene;

import java.util.ArrayList;

/**
 * Панель выбора режима редактирования: перемещение, поворот и удаление
 */
public class ModePanel extends Panel {

    public static String[] modeTags =
            {
                    "Move blocks to desired position",
                    "Rotate blocks input/output direction",
                    "Remove and sell block",
                    "Buy production area"
            };

    public final int panelColor = 0xCC505050;
    private ProductionScene productionScene;
    private int tickSound;
    private String toggledButton;
    private static Sprite uiIcons = null;

    protected ClickListener listener = new ClickListener() {
        @Override
        public void onClick(Widget w) {

            if (ProductionSceneUI.getBlocksPanel().getToggledButton()!=null) {
                ProductionSceneUI.getBlocksPanel().untoggleButtons();
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
                HelperPanel helperPanel = productionScene.getHelperPanel();
                helperPanel.setText(toggledButton);
            } else {
                w.setColor(Color.GRAY);
                toggledButton = null;
                productionScene.setHelperMissionText();
            }

            productionScene.getProduction().unselectBlock();
            productionScene.getAdjustmentPanel().hideBlockInfo();
        }
    };


    public ModePanel(ProductionScene scene) {
        super();
        this.productionScene = scene;
        setLocalBounds(730,0,440,140);
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

        if (uiIcons==null) {
            Resources resources = SceneManager.getResources();
            uiIcons = new Sprite(resources, R.drawable.ui_icons, 4, 4);
        }

        for (int i =0; i<4; i++) {
            sprite = uiIcons.getAsSprite(8 + i);
            button = new Button(sprite);
            button.setTag(modeTags[i]);
            button.setLocalBounds(25 + i * 100, 20, 90, 100);
            button.setColor(Color.GRAY);
            button.setClickListener(listener);
            this.addChild(button);
        }

    }

    public String getToggledButton() {
        return toggledButton;
    }

    @Override
    public boolean onMotionEvent(MotionEvent event, float worldX, float worldY) {
        if (event.getActionMasked()==MotionEvent.ACTION_UP) {
            productionScene.getInputHandler().invalidateAllActions();
        }
        return super.onMotionEvent(event, worldX, worldY);
    }
}
