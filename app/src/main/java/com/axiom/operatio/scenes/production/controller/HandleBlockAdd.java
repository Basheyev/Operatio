package com.axiom.operatio.scenes.production.controller;

import android.view.MotionEvent;

import com.axiom.atom.R;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.operatio.model.Production;
import com.axiom.operatio.model.ProductionRenderer;
import com.axiom.operatio.model.block.Block;
import com.axiom.operatio.model.buffer.Buffer;
import com.axiom.operatio.model.machine.Machine;
import com.axiom.operatio.model.machine.MachineType;
import com.axiom.operatio.model.conveyor.Conveyor;
import com.axiom.operatio.scenes.production.ProductionScene;

//  TODO Перетаскивание блока с панели на карту производства и отображение (Drag & Drop)
public class HandleBlockAdd {

    private InputHandler inputHandler;
    private ProductionScene scene;
    private Production production;
    private ProductionRenderer productionRenderer;
    private boolean dragging = false;
    private float cursorX, cursorY;
    private int lastCol, lastRow;
    private int blockAddSound;

    public HandleBlockAdd(InputHandler inputHandler, ProductionScene scene, Production production,
                          ProductionRenderer productionRenderer) {
        this.inputHandler = inputHandler;
        this.production = production;
        this.productionRenderer = productionRenderer;
        this.scene = scene;
        blockAddSound = SoundRenderer.loadSound(R.raw.block_add_snd);
    }


    public void onMotion(MotionEvent event, float worldX, float worldY) {
        int column = productionRenderer.getProductionColumn(worldX);
        int row = productionRenderer.getProductionRow(worldY);
        Block block = production.getBlockAt(column, row);
        if (block!=null) inputHandler.handleLookAround.onMotion(event, worldX, worldY);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastCol = column;
                lastRow = row;
                dragging = true;
                cursorX = worldX;
                cursorY = worldY;
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                dragging = false;
                if (column >= 0 && row >= 0 && lastCol==column && lastRow==row) {
                    if (block==null) {
                        String toggled = scene.blocksPanel.getToggledButton();
                        if (toggled!=null) addBlockAt(toggled, column, row);
                    }
                }

        }
    }



    protected void addBlockAt(String toggled, int column, int row) {
        Block block = null;
        MachineType mt;
        int choice = Integer.parseInt(toggled);
        SoundRenderer.playSound(blockAddSound);
        switch (choice) {
            case 0:
                mt = MachineType.getMachineType(0);
                block = new Machine(production,
                        mt, mt.getOperations()[0],
                        Machine.LEFT, Machine.RIGHT);
                break;
            case 1:
                mt = MachineType.getMachineType(1);
                block = new Machine(production,
                        mt, mt.getOperations()[0],
                        Machine.LEFT, Machine.RIGHT);
                break;
            case 2:
                mt = MachineType.getMachineType(2);
                block = new Machine(production,
                        mt, mt.getOperations()[0],
                        Machine.LEFT, Machine.RIGHT);
                break;
            case 3:
                mt = MachineType.getMachineType(3);
                block = new Machine(production,
                        mt, mt.getOperations()[0],
                        Machine.LEFT, Machine.RIGHT);
                break;
            case 4:
                block = new Buffer(production, 100);
                break;
            case 5:
                block = new Conveyor(production, Block.LEFT, Block.RIGHT, 5);
                break;
        }

        if (block!=null) {
            production.setBlock(block, column, row);
        }

    }



}
