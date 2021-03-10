package com.axiom.operatio.scenes.production.controller;

import android.view.MotionEvent;

import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.ProductionRenderer;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.scenes.production.ProductionScene;
import com.axiom.operatio.scenes.production.view.AdjustmentPanel;
import com.axiom.operatio.scenes.production.view.ProductionSceneUI;

/**
 * Обработчик движения камеры
 */
public class CameraMoveHandler {

    private Production production;
    private ProductionRenderer productionRenderer;

    private boolean actionInProgress = false;
    private float cursorX, cursorY;
    private int lastCol, lastRow;
    private float minX, minY, maxX, maxY;
    private static float HORIZONTAL_MARGIN = 370;
    private static float VERTICAL_MARGIN = 250;

    public CameraMoveHandler(Production prod, ProductionRenderer prodRender) {
        this.production = prod;
        this.productionRenderer = prodRender;
    }


    public void onMotion(MotionEvent event, float worldX, float worldY) {
        int column = productionRenderer.getProductionColumn(worldX);
        int row = productionRenderer.getProductionRow(worldY);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                actionDown(column, row, worldX, worldY);
                break;
            case MotionEvent.ACTION_MOVE:
                actionMove(worldX, worldY);
                break;
            case MotionEvent.ACTION_UP:
                actionUp(column, row);
        }
    }


    private void actionDown(int column, int row, float worldX, float worldY) {
        lastCol = column;
        lastRow = row;
        cursorX = worldX;
        cursorY = worldY;
        actionInProgress = true;
    }


    private void actionMove(float worldX, float worldY) {

        float halfWidth = Camera.WIDTH * 0.5f;
        float halfHeight = Camera.HEIGHT * 0.5f;
        float cellWidth = productionRenderer.getCellWidth();
        float cellHeight = productionRenderer.getCellHeight();
        int columns = production.getColumns();
        int rows = production.getRows();

        minX = -HORIZONTAL_MARGIN + halfWidth;
        minY = -VERTICAL_MARGIN + halfHeight;
        maxX = cellWidth * columns + HORIZONTAL_MARGIN - halfWidth;
        maxY = cellHeight * rows + VERTICAL_MARGIN - halfHeight;

        if (actionInProgress) {
            Camera camera = Camera.getInstance();
            float x = camera.getX() + (cursorX - worldX);
            float y = camera.getY() + (cursorY - worldY);
            if (x < minX) x = minX;
            if (x > maxX) x = maxX;
            if (y < minY) y = minY;
            if (y > maxY) y = maxY;
            camera.lookAt(x, y);
        }
    }

    private void actionUp(int column, int row) {
        if (actionInProgress) {
            if (column >= 0 && row >= 0 && lastCol == column && lastRow == row) {
                Block block = production.getBlockAt(column, row);
                if (block != null) {
                    AdjustmentPanel opsPanel = ProductionSceneUI.getAdjustmentPanel();
                    if (production.isBlockSelected()
                            && production.getSelectedCol() == column
                            && production.getSelectedRow() == row) {
                        production.unselectBlock();
                        opsPanel.hideBlockInfo();
                    } else {
                        opsPanel.showBlockInfo(block);
                        production.selectBlock(column, row);
                    }
                }
            }

        }
        actionInProgress = false;
    }


    public void invalidateAction() {
        // Отменить действие
        actionInProgress = false;
    }

}
