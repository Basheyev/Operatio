package com.axiom.operatio.scenes.production.controller;

import android.view.MotionEvent;

import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.ProductionRenderer;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.model.production.block.BlockAdjuster;
import com.axiom.operatio.scenes.production.view.AdjustmentPanel;
import com.axiom.operatio.scenes.production.view.ProductionSceneUI;

/**
 * Обработчик вращения направлений входов/выходов блока
 */
public class BlockRotateHandler {

    private InputHandler inputHandler;
    private Production production;
    private ProductionRenderer productionRenderer;
    private int lastCol, lastRow;
    private boolean actionInProgress = false;


    public BlockRotateHandler(InputHandler inputHandler, Production production, ProductionRenderer productionRenderer) {
        this.inputHandler = inputHandler;
        this.production = production;
        this.productionRenderer = productionRenderer;
    }


    public void onMotion(MotionEvent event, float worldX, float worldY) {
        int column = productionRenderer.getProductionColumn(worldX);
        int row = productionRenderer.getProductionRow(worldY);
        Block block = production.getBlockAt(column, row);

        // Если нет никакого блока - вызвать обработчик движения камеры
        if (block==null) inputHandler.getCameraMoveHandler().onMotion(event, worldX, worldY);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (block!=null) startAction(column, row);
                break;
            case MotionEvent.ACTION_UP:
                rotateBlock(column, row, block);
        }

    }

    private void startAction(int column, int row) {
        lastCol = column;
        lastRow = row;
        actionInProgress = true;
    }


    private void rotateBlock(int column, int row, Block block) {
        if (actionInProgress && column >= 0 && row >= 0 && lastCol==column && lastRow==row) {
            if (block!=null) {
                BlockAdjuster.rotateFlow(block);
                AdjustmentPanel opsPanel = ProductionSceneUI.getAdjustmentPanel();
                opsPanel.showBlockInfo(block);
                production.selectBlock(column, row);
            }
        }
    }


    public void invalidateAction() {
        // Отменить действие
        actionInProgress = false;
    }


}
