package com.axiom.operatio.scenes.production.controller;


import android.view.MotionEvent;

import com.axiom.atom.R;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.operatio.model.gameplay.Ledger;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.ProductionRenderer;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.scenes.production.ProductionScene;
import com.axiom.operatio.scenes.production.view.AdjustmentPanel;
import com.axiom.operatio.scenes.production.view.ProductionSceneUI;

/**
 * Обработчик добавления и перемещения блока
 */
public class BlockAddMoveHandler {

    private InputHandler inputHandler;
    private ProductionScene scene;
    private Production production;
    private ProductionRenderer productionRenderer;

    private Block dragBlock = null;
    private boolean actionInProgress = false;
    private boolean justCreatedBlock = false;
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
        int column = productionRenderer.getProductionColumn(worldX);
        int row = productionRenderer.getProductionRow(worldY);
        Block block = production.getBlockAt(column, row);

        // Если начато движение, но блока нет - вызываем обработчик движения камеры
        if (block==null) inputHandler.getCameraMoveHandler().onMotion(event, worldX, worldY);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (block!=null) actionDown(column,row,worldX,worldY, block);
                break;
            case MotionEvent.ACTION_MOVE:
                actionMove(column, row, worldX, worldY);
                break;
            case MotionEvent.ACTION_UP:
                actionUp(column, row, worldX, worldY, block);
        }

    }


    private synchronized void actionDown(int column, int row, float worldX, float worldY, Block block) {
        lastCol = column;
        lastRow = row;
        cursorX = worldX;
        cursorY = worldY;
        dragBlock = block;
        production.removeBlock(block, false);
        actionInProgress = true;
    }


    private synchronized void actionMove(int column, int row, float worldX, float worldY) {
        if (actionInProgress && dragBlock!=null) {
            cursorX = worldX;
            cursorY = worldY;
            productionRenderer.startBlockMoving(dragBlock, cursorX, cursorY);
            production.selectBlock(column, row);
        }
    }


    private synchronized void actionUp(int column, int row, float worldX, float worldY, Block block) {
        int cols = production.getColumns();
        int rows = production.getRows();
        // Если было начато движение (добавление) блока
        if (actionInProgress) {
            productionRenderer.stopBlockMoving();
            // Если блок добавлен в пределах карты производства
            if (column >= 0 && row >= 0 && column < cols && row < rows) {
                if (block == null) setBlockTo(column, row);
                else returnBlockBack(column, row);
            } else {
                if (!justCreatedBlock) returnBlockBack(lastCol, lastRow);
                invalidateAction();
            }
            // Если это было добавление, то отжимаем кнопки
            if (justCreatedBlock) {
                ProductionSceneUI.getBlocksPanel().untoggleButtons();
                justCreatedBlock = false;
            }
            actionInProgress = false;
        }
    }


    /**
     * Установить блок в указанную позицию на производстве
     * @param column - столбец
     * @param row - строка
     */
    private synchronized void setBlockTo(int column, int row) {
        AdjustmentPanel opsPanel = ProductionSceneUI.getAdjustmentPanel();
        production.setBlock(dragBlock, column, row);
        // Если был создан новый блок - вычесть деньги
        if (justCreatedBlock) {
            int expenseType = Ledger.EXPENSE_BLOCK_BOUGHT;
            production.decreaseCashBalance(expenseType, dragBlock.getPrice());
        }
        dragBlock.adjustFlowDirection();
        opsPanel.showBlockInfo(dragBlock);
        production.selectBlock(column, row);

        // Запускаем эффект частиц
        scene.getProductionRenderer().getParticles().generateParticles();
        // Проигрываем звук
        SoundRenderer.playSound(blockPlaced);
    }


    /**
     * Вернуть блок обратно, если действие запрещено
     * @param selectedColumn столбец выделяемого блока
     * @param selectedRow строка выделяемого блока
     */
    private synchronized void returnBlockBack(int selectedColumn, int selectedRow) {
        // Вернуть блок откуда взяли - ранее сохраненный столбец и строка
        production.setBlock(dragBlock, lastCol, lastRow);
        // Выделить тот блок который указали
        production.selectBlock(selectedColumn, selectedRow);
    }


    /**
     * Начало движения блока при добавлении
     * @param block новый блок
     * @param worldX мировая координата X
     * @param worldY мировая координата Y
     */
    public synchronized void startAction(Block block, float worldX, float worldY) {
        if (actionInProgress) return;
        justCreatedBlock = true; // Помечаем что это вновь созданные блок
        lastCol = -1;
        lastRow = -1;
        cursorX = worldX;
        cursorY = worldY;
        dragBlock = block;
        actionInProgress = true;
    }

    /**
     * Отмена движения блока
     */
    public synchronized void invalidateAction() {
        // Вернуть блок на место
        if (actionInProgress) {
            productionRenderer.stopBlockMoving();
            // Если lastCol==lastRow==-1 значит удалить блок
            production.setBlock(dragBlock, lastCol, lastRow);
        }
        // Отменить действие
        actionInProgress = false;
        // Отменить создание нового блока
        ProductionSceneUI.getBlocksPanel().untoggleButtons();
        // Убрать выделение блока если там ничего нет
        production.unselectBlock();
    }

}
