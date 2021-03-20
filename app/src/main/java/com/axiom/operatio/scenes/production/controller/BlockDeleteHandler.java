package com.axiom.operatio.scenes.production.controller;

import android.view.MotionEvent;

import com.axiom.atom.R;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.operatio.model.ledger.Ledger;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.ProductionRenderer;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.scenes.production.view.AdjustmentPanel;
import com.axiom.operatio.scenes.production.view.ProductionSceneUI;

/**
 * Обработчик удаления блока
 */
public class BlockDeleteHandler {

    private InputHandler inputHandler;
    private Production production;
    private ProductionRenderer productionRenderer;

    private boolean actionInProgress = false;
    private int lastCol, lastRow;
    private int blockRemoveSound;


    public BlockDeleteHandler(InputHandler inputHandler, Production prod, ProductionRenderer prodRender) {
        this.inputHandler = inputHandler;
        this.production = prod;
        this.productionRenderer = prodRender;
        blockRemoveSound = SoundRenderer.loadSound(R.raw.block_remove_snd);
    }


    public void onMotion(MotionEvent event, float worldX, float worldY) {
        int column = productionRenderer.getProductionColumn(worldX);
        int row = productionRenderer.getProductionRow(worldY);
        Block block = production.getBlockAt(column, row);

        // Если никакого блока нет - вызываем обработчик движения камеры
        if (block==null) inputHandler.getCameraMoveHandler().onMotion(event, worldX, worldY);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (block!=null) startAction(column, row);
                break;
            case MotionEvent.ACTION_UP:
                deleteBlock(column, row, block);
        }
    }


    private void startAction(int column, int row) {
        lastCol = column;
        lastRow = row;
        actionInProgress = true;
    }


    private void deleteBlock(int column, int row, Block block) {
        if (actionInProgress && column >= 0 && row >= 0 && lastCol==column && lastRow==row) {
            if (block!=null) {
                production.removeBlock(block,true);
                production.getLedger().increaseCashBalance(Ledger.REVENUE_BLOCK_SOLD, block.getPrice());
                production.unselectBlock();
                AdjustmentPanel opsPanel = ProductionSceneUI.getAdjustmentPanel();
                opsPanel.hideBlockInfo();
                SoundRenderer.playSound(blockRemoveSound);
            }
            actionInProgress = false;
        }
    }

    public void invalidateAction() {
        actionInProgress = false;
    }

}
