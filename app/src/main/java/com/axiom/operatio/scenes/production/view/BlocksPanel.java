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

public class BlocksPanel extends Panel {

    protected boolean collapsed = false;

    public static ClickListener clickListener = new ClickListener() {
        @Override
        public void onClick(Widget w) {
            BlocksPanel panel = (BlocksPanel) w;
            if (!panel.collapsed) {
                panel.setLocation(-300, 200);
                panel.collapsed = true;
            } else {
                panel.setLocation(0, 200);
                panel.collapsed = false;
            }
        }
    };

    public final int panelColor = 0xCC505050;
    protected ProductionScene productionScene;
    protected String toggledButton;

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

    @Override
    public boolean onMotionEvent(MotionEvent event, float worldX, float worldY) {
        if (event.getAction()==MotionEvent.ACTION_UP) {
            productionScene.getInputHandler().invalidateAllActionsButScale();
        }
        return super.onMotionEvent(event, worldX, worldY);
    }
}
