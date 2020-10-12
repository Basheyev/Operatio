package com.axiom.operatio.scenes.production.controller;

import android.view.MotionEvent;

import com.axiom.atom.R;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.operatio.model.Production;
import com.axiom.operatio.model.ProductionRenderer;
import com.axiom.operatio.model.block.Block;
import com.axiom.operatio.model.buffer.Buffer;
import com.axiom.operatio.model.machine.Machine;
import com.axiom.operatio.model.conveyor.Conveyor;
import com.axiom.operatio.scenes.production.ProductionScene;
import com.axiom.operatio.scenes.production.view.OperationPanel;
import com.axiom.operatio.scenes.production.view.ProductionSceneUI;


public class CameraMoveHandler {

    private InputHandler inputHandler;
    private ProductionScene scene;
    private Production production;
    private ProductionRenderer productionRenderer;

    private boolean actionInProgress = false;
    private float cursorX, cursorY;
    private int lastCol, lastRow;


    public CameraMoveHandler(InputHandler inputHandler, ProductionScene scn, Production prod, ProductionRenderer prodRender) {
        this.inputHandler = inputHandler;
        this.production = prod;
        this.productionRenderer = prodRender;
        this.scene = scn;

    }

    public void onMotion(MotionEvent event, float worldX, float worldY) {
        int column = productionRenderer.getProductionColumn(worldX);
        int row = productionRenderer.getProductionRow(worldY);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastCol = column;
                lastRow = row;
                cursorX = worldX;
                cursorY = worldY;
                actionInProgress = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (actionInProgress) {
                    Camera camera = Camera.getInstance();
                    float x = camera.getX() + (cursorX - worldX);
                    float y = camera.getY() + (cursorY - worldY);
                    camera.lookAt(x, y);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (actionInProgress) {
                    if (column >= 0 && row >= 0 && lastCol == column && lastRow == row) {
                        Block block = production.getBlockAt(column, row);
                        if (block != null) {
                            OperationPanel opsPanel = ProductionSceneUI.getOperationPanel();
                            if (production.isBlockSelected()
                                    && production.getSelectedCol() == column
                                    && production.getSelectedRow() == row) {
                                production.unselectBlock();
                                opsPanel.hideBlockInfo();
                            } else {
                                opsPanel.showBlockInfo(block, true);
                                production.selectBlock(column, row);
                            }
                        }
                    }

                }
                actionInProgress = false;
        }
    }


    public void invalidateAction() {
        // Отменить действие
        actionInProgress = false;
    }

}
