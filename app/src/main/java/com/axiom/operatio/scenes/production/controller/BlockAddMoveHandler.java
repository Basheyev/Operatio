package com.axiom.operatio.scenes.production.controller;


import android.view.MotionEvent;

import com.axiom.atom.R;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.operatio.model.Production;
import com.axiom.operatio.model.ProductionRenderer;
import com.axiom.operatio.model.block.Block;
import com.axiom.operatio.scenes.production.ProductionScene;
import com.axiom.operatio.scenes.production.view.UIBuilder;

public class BlockAddMoveHandler {

    private InputHandler inputHandler;
    private ProductionScene scene;
    private Production production;
    private ProductionRenderer productionRenderer;

    private Block dragBlock = null;
    private boolean actionInProgress = false;
    private float cursorX, cursorY;
    private int lastCol, lastRow;
    private int blockPlaced;

    public BlockAddMoveHandler(InputHandler inputHandler,
                               ProductionScene scn, Production prod, ProductionRenderer prodRender) {
        blockPlaced = SoundRenderer.loadSound(R.raw.block_add_snd);
        this.inputHandler = inputHandler;
        this.production = prod;
        this.productionRenderer = prodRender;
        this.scene = scn;
    }

    public synchronized void onMotion(MotionEvent event, float worldX, float worldY) {
        int cols = production.getColumns();
        int rows = production.getRows();
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
                    dragBlock = block;
                    production.removeBlock(block);
                    actionInProgress = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (actionInProgress && dragBlock!=null) {
                    cursorX = worldX;
                    cursorY = worldY;
                    productionRenderer.startBlockMoving(dragBlock, cursorX, cursorY);
                    production.selectBlock(column, row);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (actionInProgress) {
                    productionRenderer.stopBlockMoving();
                    if (column >= 0 && row >= 0 && column < cols && row < rows) {
                        if (block == null) {
                            SoundRenderer.playSound(blockPlaced);
                            production.setBlock(dragBlock, column, row);
                            dragBlock.adjustFlowDirection();
                            production.selectBlock(column, row);
                        } else {
                            production.setBlock(dragBlock, lastCol, lastRow);
                            production.selectBlock(lastCol, lastRow);
                        }
                    } else {
                        production.setBlock(dragBlock, lastCol, lastRow);
                        production.selectBlock(lastCol, lastRow);
                    }
                    // Если это было добавление то отжимаем кнопки
                    if (lastCol==-1 && lastRow==-1) {
                        UIBuilder.getBlocksPanel().untoggleButtons();
                    }
                    actionInProgress = false;
                }
        }
    }

    // Используется для добавления
    public synchronized void startAction(Block block, float worldX, float worldY) {
        if (actionInProgress) return;
        // TODO Специально ошибочное значение (нормально сделаить)
        lastCol = -1;  // Помечаем что это вновь созданные блок
        lastRow = -1;  // Помечаем что это вновь созданный блок
        cursorX = worldX;
        cursorY = worldY;
        dragBlock = block;
        actionInProgress = true;
    }


    public synchronized void invalidateAction() {
        // Вернуть блок на место
        if (actionInProgress) {
            productionRenderer.stopBlockMoving();
            // Если lastCol==lastRow==-1 значит удалить блок
            production.setBlock(dragBlock, lastCol, lastRow);
        }
        // Отменить действие
        actionInProgress = false;
    }
}
