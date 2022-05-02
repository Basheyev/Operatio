package com.basheyev.operatio.scenes.production.controller;

import android.view.MotionEvent;

import com.basheyev.atom.R;
import com.basheyev.atom.engine.core.GameLoop;
import com.basheyev.atom.engine.data.events.GameEvent;
import com.basheyev.atom.engine.sound.SoundRenderer;
import com.basheyev.operatio.model.gameplay.OperatioEvents;
import com.basheyev.operatio.model.ledger.Ledger;
import com.basheyev.operatio.model.production.Production;
import com.basheyev.operatio.model.production.ProductionRenderer;
import com.basheyev.operatio.model.production.block.Block;
import com.basheyev.operatio.scenes.production.view.adjustment.AdjustmentPanel;
import com.basheyev.operatio.scenes.production.ProductionSceneUI;

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
                production.getLedger().debitCashBalance(Ledger.REVENUE_BLOCK_SOLD, block.getPrice());
                production.unselectBlock();
                AdjustmentPanel opsPanel = ProductionSceneUI.getAdjustmentPanel();
                opsPanel.hideBlockInfo();
                GameLoop.getInstance().fireGameEvent(new GameEvent(OperatioEvents.BLOCK_DELETED, block));
                SoundRenderer.playSound(blockRemoveSound);

                // Запускаем эффект частиц денег
                float w = productionRenderer.getCellWidth();
                float h = productionRenderer.getCellHeight();
                productionRenderer.getMoneyParticles().addParticle(
                        block.getPrice(),
                        column * w, row * h + h/2);
            }
            actionInProgress = false;
        }
    }

    public void invalidateAction() {
        actionInProgress = false;
    }

}
