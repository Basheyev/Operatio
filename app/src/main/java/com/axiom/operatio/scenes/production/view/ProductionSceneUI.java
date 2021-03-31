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

/**
 * Пользовательский интерфейс сцены производства
 */
public class ProductionSceneUI {

    private static Production production;
    private static ProductionScene productionScene;
    private static BlocksPanel blocksPanel;
    private static ModePanel modePanel;
    private static AdjustmentPanel adjustmentPanel;

    private static ScenesPanel scenesPanel;

    private static int tickSound;


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
