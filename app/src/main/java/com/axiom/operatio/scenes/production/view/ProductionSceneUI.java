package com.axiom.operatio.scenes.production.view;

import android.content.res.Resources;

import com.axiom.atom.R;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.scenes.common.ScenesPanel;
import com.axiom.operatio.scenes.production.ProductionScene;


public class ProductionSceneUI {

    protected static Production production;
    protected static ProductionScene productionScene;
    protected static BlocksPanel blocksPanel;
    protected static ModePanel modePanel;
    protected static AdjustmentPanel adjustmentPanel;

    protected static ScenesPanel scenesPanel;

    protected static int tickSound;


    public static void buildUI(ProductionScene scene, final Resources resources, Widget widget, Production prod) {

        production = prod;
        productionScene = scene;
        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);

        blocksPanel = new BlocksPanel(productionScene);
        widget.addChild(blocksPanel);

        modePanel = new ModePanel(productionScene);
        widget.addChild(modePanel);

        adjustmentPanel = new AdjustmentPanel(production, productionScene);
        adjustmentPanel.hideBlockInfo();
        widget.addChild(adjustmentPanel);

        scenesPanel = new ScenesPanel(production);
        widget.addChild(scenesPanel);

    }

    public static BlocksPanel getBlocksPanel() {
        return blocksPanel;
    }

    public static ModePanel getModePanel() {
        return modePanel;
    }

    public static AdjustmentPanel getAdjustmentPanel() { return adjustmentPanel; }

}
