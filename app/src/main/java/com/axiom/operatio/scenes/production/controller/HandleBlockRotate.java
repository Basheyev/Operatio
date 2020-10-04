package com.axiom.operatio.scenes.production.controller;

import android.view.MotionEvent;

import com.axiom.operatio.model.Production;
import com.axiom.operatio.model.ProductionRenderer;
import com.axiom.operatio.model.block.Block;
import com.axiom.operatio.model.conveyor.Conveyor;
import com.axiom.operatio.scenes.production.ProductionScene;

import java.util.HashMap;

//  TODO Вращение блока производства (направлений вход-выход)
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
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if (column >= 0 && row >= 0 && lastCol==column && lastRow==row) {
                    if (block!=null) rotateBlock(block);
                }
        }

    }


    private void rotateBlock(Block block) {

        int currentInpDir = block.getInputDirection();
        int currentOutDir = block.getOutputDirection();

        if (!block.blockFlipState) {
            block.setDirections(currentOutDir, currentInpDir);
            block.blockFlipState = true;
        } else {
            // TODO Выработать правила упрощенного поворота с учетом блоков рядом
            int newInpDir = Block.nextClockwiseDirection(currentInpDir);
            int newOutDir = Block.nextClockwiseDirection(currentOutDir);
            if (newInpDir==currentOutDir) {
                block.setOutputDirection(newOutDir);
            } else {
                block.setInputDirection(newInpDir);
            }
            block.blockFlipState = false;
        }



    }


}
