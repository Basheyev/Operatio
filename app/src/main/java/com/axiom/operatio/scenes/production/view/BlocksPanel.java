package com.axiom.operatio.scenes.production.view;

import android.graphics.Color;
import android.view.MotionEvent;

import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.gameplay.Level;
import com.axiom.operatio.model.gameplay.LevelFactory;
import com.axiom.operatio.scenes.production.ProductionScene;

import java.util.ArrayList;

/**
 * Панель добавления блоков
 */
public class BlocksPanel extends Panel {

    public static final int panelColor = 0xCC505050;
    private ProductionScene productionScene;
    private String toggledButton;

    public BlocksPanel(ProductionScene scene) {
        super();
        this.productionScene = scene;
        setLocalBounds(0,160,340,700);
        setColor(panelColor);
        buildButtons();
    }

    public void untoggleButtons() {
        for (Widget widget:children) {
            widget.setColor(Color.GRAY);
        }
        toggledButton = null;
    }

    private void buildButtons() {
        BlockButton button;
        for (int id =0; id<10; id++) {
            button = new BlockButton(productionScene,this, id);
            button.setLocalBounds(25 + (id % 2) * 130, 550 - (id / 2) * 130, 120, 120);
        }
    }

    public void updatePermissions(int level) {
        LevelFactory lm = LevelFactory.getInstance();
        Level currentLevel = lm.getLevel(level);
        ArrayList<Widget> children = getChildren();
        for (int i=0; i<children.size(); i++) {
            BlockButton button = (BlockButton) children.get(i);
            if (currentLevel.isBlockAvailable(Integer.parseInt(button.getTag()))) {
                button.visible = true;
            } else {
                button.visible = false;
            }
        }
    }

    public String getToggledButton() {
        return toggledButton;
    }

    public void setToggledButton(String tag) {
        toggledButton = tag;
    }

    @Override
    public boolean onMotionEvent(MotionEvent event, float worldX, float worldY) {
        if (event.getActionMasked()==MotionEvent.ACTION_UP) {
            productionScene.getInputHandler().invalidateAllActions();
        }
        return super.onMotionEvent(event, worldX, worldY);
    }
}
