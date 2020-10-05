package com.axiom.operatio.scenes.production.controller;

import android.view.MotionEvent;

import com.axiom.operatio.model.Production;
import com.axiom.operatio.model.ProductionRenderer;
import com.axiom.operatio.model.block.Block;
import com.axiom.operatio.scenes.production.ProductionScene;

public class HandleBlockRotate {

    private InputHandler inputHandler;
    private ProductionScene scene;
    private Production production;
    private ProductionRenderer productionRenderer;
    private int lastCol, lastRow;

    public HandleBlockRotate(InputHandler inputHandler, ProductionScene scene,
                             Production production, ProductionRenderer productionRenderer) {
        this.inputHandler = inputHandler;
        this.production = production;
        this.productionRenderer = productionRenderer;
        this.scene = scene;
    }


    public void onMotion(MotionEvent event, float worldX, float worldY) {
        int column = productionRenderer.getProductionColumn(worldX);
        int row = productionRenderer.getProductionRow(worldY);
        Block block = production.getBlockAt(column, row);
        if (block==null) inputHandler.handleLookAround.onMotion(event, worldX, worldY);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (block!=null) {
                    lastCol = column;
                    lastRow = row;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (column >= 0 && row >= 0 && lastCol==column && lastRow==row) {
                    if (block!=null) {
                        block.rotateFlowDirection();
                        production.selectBlock(column, row);
                    }
                }
        }

    }




}
