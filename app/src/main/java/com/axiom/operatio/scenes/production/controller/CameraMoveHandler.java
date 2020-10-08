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


public class CameraMoveHandler {

    private InputHandler inputHandler;
    private ProductionScene scene;
    private Production production;
    private ProductionRenderer productionRenderer;

    private boolean actionInProgress = false;
    private float cursorX, cursorY;
    private int lastCol, lastRow;
    private int snd1, snd2, snd3;

    public CameraMoveHandler(InputHandler inputHandler, ProductionScene scn, Production prod, ProductionRenderer prodRender) {
        this.inputHandler = inputHandler;
        this.production = prod;
        this.productionRenderer = prodRender;
        this.scene = scn;
        snd1 = SoundRenderer.loadSound(R.raw.machine_snd);
        snd2 = SoundRenderer.loadSound(R.raw.conveyor_snd);
        snd3 = SoundRenderer.loadSound(R.raw.buffer_snd);
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
                            if (block instanceof Machine) SoundRenderer.playSound(snd1);
                            if (block instanceof Conveyor) SoundRenderer.playSound(snd2);
                            if (block instanceof Buffer) SoundRenderer.playSound(snd3);

                        }
                    }

                    if (production.isBlockSelected()
                            && production.getSelectedCol() == column
                            && production.getSelectedRow() == row) {
                        production.unselectBlock();
                    } else production.selectBlock(column, row);
                }
                actionInProgress = false;
        }
    }


    public void invalidateAction() {
        // Отменить действие
        actionInProgress = false;
    }

}
