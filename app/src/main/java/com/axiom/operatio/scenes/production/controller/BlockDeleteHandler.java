package com.axiom.operatio.scenes.production.controller;

import android.util.Log;
import android.view.MotionEvent;

import com.axiom.atom.R;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.ProductionRenderer;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.scenes.production.ProductionScene;
import com.axiom.operatio.scenes.production.view.AdjustmentPanel;
import com.axiom.operatio.scenes.production.view.ProductionSceneUI;


public class BlockDeleteHandler {

    private InputHandler inputHandler;
    private ProductionScene scene;
    private Production production;
    private ProductionRenderer productionRenderer;

    private boolean actionInProgress = false;
    private float cursorX, cursorY;
    private int lastCol, lastRow;
    private int blockRemoveSound;

    public BlockDeleteHandler(InputHandler inputHandler, ProductionScene scn,
                              Production prod, ProductionRenderer prodRender) {
        this.inputHandler = inputHandler;
        this.production = prod;
        this.productionRenderer = prodRender;
        this.scene = scn;
        blockRemoveSound = SoundRenderer.loadSound(R.raw.block_remove_snd);
    }


    public void onMotion(MotionEvent event, float worldX, float worldY) {
        int column = productionRenderer.getProductionColumn(worldX);
        int row = productionRenderer.getProductionRow(worldY);
        Block block = production.getBlockAt(column, row);
        if (block==null) inputHandler.getCameraMoveHandler().onMotion(event, worldX, worldY);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (block!=null) {
                    lastCol = column;
                    lastRow = row;
                    cursorX = worldX;
                    cursorY = worldY;
                    actionInProgress = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if (actionInProgress && column >= 0 && row >= 0 && lastCol==column && lastRow==row) {
                    if (block!=null) {
                        production.removeBlock(block,true);
                        production.increaseCashBalance(Production.INCOME_BLOCK_SOLD, block.getPrice());
                        production.getLedger().registerBlockSold(block.getPrice());
                        production.unselectBlock();
                        AdjustmentPanel opsPanel = ProductionSceneUI.getAdjustmentPanel();
                        opsPanel.hideBlockInfo();
                        SoundRenderer.playSound(blockRemoveSound);
                    }
                }
                actionInProgress = false;
        }
    }

    public void invalidateAction() {
        actionInProgress = false;
    }

}
