package com.basheyev.operatio.scenes.production;

import android.content.res.Resources;

import com.basheyev.atom.R;
import com.basheyev.atom.engine.sound.SoundRenderer;
import com.basheyev.atom.engine.ui.widgets.Widget;
import com.basheyev.operatio.model.production.Production;
import com.basheyev.operatio.scenes.common.ScenesPanel;
import com.basheyev.operatio.scenes.production.view.adjustment.AdjustmentPanel;
import com.basheyev.operatio.scenes.production.view.BlocksPanel;
import com.basheyev.operatio.scenes.production.view.HelperPanel;
import com.basheyev.operatio.scenes.production.view.ModePanel;

/**
 * Пользовательский интерфейс сцены производства
 */
public class ProductionSceneUI {

    private static Production production;
    private static ProductionScene productionScene;
    private static BlocksPanel blocksPanel;
    private static ModePanel modePanel;
    private static AdjustmentPanel adjustmentPanel;
    private static HelperPanel helperPanel;

    private static ScenesPanel scenesPanel;

    private static int tickSound;


    public static void buildUI(ProductionScene scene, final Resources resources, Widget widget, Production prod) {

        production = prod;
        productionScene = scene;
        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);

        blocksPanel = new BlocksPanel(productionScene);
        blocksPanel.setInputTransparent(true);
        widget.addChild(blocksPanel);

        modePanel = new ModePanel(productionScene);
        modePanel.setInputTransparent(true);
        widget.addChild(modePanel);

        adjustmentPanel = new AdjustmentPanel(production, productionScene);
        adjustmentPanel.setInputTransparent(true);
        adjustmentPanel.hideBlockInfo();
        widget.addChild(adjustmentPanel);

        scenesPanel = new ScenesPanel(production);
        scenesPanel.setInputTransparent(true);
        widget.addChild(scenesPanel);

        helperPanel = new HelperPanel(productionScene);
        helperPanel.setInputTransparent(true);
        widget.addChild(helperPanel);

    }

    public static BlocksPanel getBlocksPanel() {
        return blocksPanel;
    }

    public static ModePanel getModePanel() {
        return modePanel;
    }

    public static AdjustmentPanel getAdjustmentPanel() { return adjustmentPanel; }

    public static HelperPanel getHelperPanel() {
        return helperPanel;
    }

    public static ScenesPanel getScenesPanel() {
        return scenesPanel;
    }
}
