package com.basheyev.operatio.scenes.production.view;

import android.graphics.Color;
import android.view.MotionEvent;

import com.basheyev.atom.engine.ui.widgets.Panel;
import com.basheyev.atom.engine.ui.widgets.Widget;
import com.basheyev.operatio.model.gameplay.GamePermissions;
import com.basheyev.operatio.model.production.machine.MachineType;
import com.basheyev.operatio.scenes.production.ProductionScene;

import java.util.ArrayList;

import static com.basheyev.operatio.scenes.production.view.BlockButton.MACHINES_INDEX_START;

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
        setLocalBounds(0,160,310,700);
        setColor(panelColor);
        buildButtons();
    }

    public void untoggleButtons() {
        for (Widget widget:children) {
            widget.setColor(Color.DKGRAY);
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

    public void updatePermissions() {
        GamePermissions permissions = productionScene.getProduction().getPermissions();
        ArrayList<Widget> children = getChildren();
        for (int i=0; i<children.size(); i++) {
            BlockButton button = (BlockButton) children.get(i);
            int blockID = Integer.parseInt(button.getTag());
            // если это машина
            if (blockID >= MACHINES_INDEX_START) {
                button.setActive(permissions.isAvailable(MachineType.getMachineType(blockID - MACHINES_INDEX_START)));
            } else button.setActive(true);
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
