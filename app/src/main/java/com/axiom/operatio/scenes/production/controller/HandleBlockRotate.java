package com.axiom.operatio.scenes.production.controller;

import android.view.MotionEvent;

import com.axiom.operatio.model.Production;
import com.axiom.operatio.model.ProductionRenderer;
import com.axiom.operatio.model.block.Block;
import com.axiom.operatio.scenes.production.ProductionScene;

//  TODO Вращение блока производства (направлений вход-выход)
public class HandleBlockRotate {

    private ProductionScene scene;
    private Production production;
    private ProductionRenderer productionRenderer;
    private int lastCol, lastRow;

    public HandleBlockRotate(ProductionScene scene, Production production, ProductionRenderer productionRenderer) {
        this.production = production;
        this.productionRenderer = productionRenderer;
        this.scene = scene;
    }


    public void onMotion(MotionEvent event, float worldX, float worldY) {
        int column = productionRenderer.getProductionColumn(worldX);
        int row = productionRenderer.getProductionRow(worldY);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastCol = column;
                lastRow = row;
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if (column >= 0 && row >= 0 && lastCol==column && lastRow==row) {
                    Block block = production.getBlockAt(column, row);
                    if (block!=null) rotateBlock(block);
                }
        }

    }


    private void rotateBlock(Block block) {
        int newInpDir = Block.nextClockwiseDirection(block.getInputDirection());
        int newOutDir = Block.nextClockwiseDirection(block.getOutputDirection());
        block.setInputDirection(newInpDir);
        block.setOutputDirection(newOutDir);
    }


}
