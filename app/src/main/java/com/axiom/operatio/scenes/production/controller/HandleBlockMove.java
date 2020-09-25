package com.axiom.operatio.scenes.production.controller;


import android.util.Log;
import android.view.MotionEvent;

import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.operatio.model.Production;
import com.axiom.operatio.model.ProductionRenderer;
import com.axiom.operatio.model.block.Block;
import com.axiom.operatio.model.block.Renderer;
import com.axiom.operatio.model.buffer.Buffer;
import com.axiom.operatio.model.machines.Machine;
import com.axiom.operatio.model.transport.Conveyor;
import com.axiom.operatio.scenes.production.ProductionScene;

//  TODO Перемещение блока производства и отображение будущего занимаемого места (Drag & Drop)
public class HandleBlockMove {


    private ProductionScene scene;
    private Production production;
    private ProductionRenderer productionRenderer;

    private Block dragBlock = null;
    private boolean dragging = false;
    private float cursorX, cursorY;
    private int lastCol, lastRow;

    public HandleBlockMove(ProductionScene scn, Production prod, ProductionRenderer prodRender) {
        this.production = prod;
        this.productionRenderer = prodRender;
        this.scene = scn;
    }

    public void onMotion(MotionEvent event, float worldX, float worldY) {
        int column = productionRenderer.getProductionColumn(worldX);
        int row = productionRenderer.getProductionRow(worldY);
        Block block = production.getBlockAt(column, row);;

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
                    Renderer renderer = dragBlock.getRenderer();
                    renderer.draw(Camera.getInstance(), worldX - 64, worldY - 64, 128, 128);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (dragging) {
                    dragging = false;
                    if (column >= 0 && row >= 0) {
                        if (block == null) {
                            production.setBlock(dragBlock, column, row);
                        } else {
                            production.setBlock(dragBlock, lastCol, lastRow);
                        }
                    }
                }
        }
    }

}
