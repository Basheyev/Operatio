package com.axiom.operatio.scenes.production.controller;


import android.view.MotionEvent;

import com.axiom.atom.R;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.operatio.model.Production;
import com.axiom.operatio.model.ProductionRenderer;
import com.axiom.operatio.model.block.Block;
import com.axiom.operatio.scenes.production.ProductionScene;

//  TODO Перемещение блока производства и отображение будущего занимаемого места (Drag & Drop)
public class HandleBlockMove {

    private InputHandler inputHandler;
    private ProductionScene scene;
    private Production production;
    private ProductionRenderer productionRenderer;

    private Block dragBlock = null;
    private boolean dragging = false;
    private float cursorX, cursorY;
    private int lastCol, lastRow;
    private int blockPlaced;

    public HandleBlockMove(InputHandler inputHandler,
                           ProductionScene scn, Production prod, ProductionRenderer prodRender) {
        blockPlaced = SoundRenderer.loadSound(R.raw.block_add_snd);
        this.inputHandler = inputHandler;
        this.production = prod;
        this.productionRenderer = prodRender;
        this.scene = scn;
    }

    public void onMotion(MotionEvent event, float worldX, float worldY) {
        int column = productionRenderer.getProductionColumn(worldX);
        int row = productionRenderer.getProductionRow(worldY);
        Block block = production.getBlockAt(column, row);;
        if (block==null) inputHandler.handleLookAround.onMotion(event, worldX, worldY);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (block!=null) {
                    lastCol = column;
                    lastRow = row;
                    cursorX = worldX;
                    cursorY = worldY;
                    dragBlock = block;
                    production.removeBlock(block);
                    dragging = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (dragging && dragBlock!=null) {
                    cursorX = worldX;
                    cursorY = worldY;
                    production.selectBlock(column, row);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (dragging) {
                    dragging = false;
                    if (column >= 0 && row >= 0) {
                        if (block == null) {
                            SoundRenderer.playSound(blockPlaced);
                            production.setBlock(dragBlock, column, row);
                            production.selectBlock(column, row);
                        } else {
                            production.setBlock(dragBlock, lastCol, lastRow);
                            production.selectBlock(lastCol, lastRow);
                        }

                    }
                }
        }
    }

    public synchronized boolean isDragging() {
        return dragging;
    }

    public float getCursorX() {
        return cursorX;
    }

    public float getCursorY() {
        return cursorY;
    }

    public Block getDragBlock() {
        return dragBlock;
    }
}
