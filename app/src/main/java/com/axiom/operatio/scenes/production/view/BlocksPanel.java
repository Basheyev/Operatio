package com.axiom.operatio.scenes.production.view;

import android.graphics.Color;
import android.util.Log;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.Production;
import com.axiom.operatio.scenes.production.ProductionScene;

import java.util.ArrayList;

public class BlocksPanel extends Panel {


    public final int panelColor = 0xCC505050;
    protected ProductionScene productionScene;
    protected String toggledButton;

    public BlocksPanel(ProductionScene scene) {
        super();
        this.productionScene = scene;
        setLocalBounds(0,0,180,1080);
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
        for (int id =0; id<7; id++) {
            button = new BlockButton(productionScene,this, id);
            button.setLocalBounds(30, 940 - id * 140, 120, 120);
        }
    }

    public String getToggledButton() {
        return toggledButton;
    }

}
