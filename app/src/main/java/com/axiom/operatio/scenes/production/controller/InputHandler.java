package com.axiom.operatio.scenes.production.controller;

import android.util.Log;
import android.view.MotionEvent;

import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.operatio.model.Production;
import com.axiom.operatio.model.ProductionRenderer;
import com.axiom.operatio.model.block.Block;
import com.axiom.operatio.model.buffer.Buffer;
import com.axiom.operatio.model.machines.Machine;
import com.axiom.operatio.model.machines.MachineType;
import com.axiom.operatio.model.transport.Conveyor;
import com.axiom.operatio.scenes.production.ProductionScene;

// Обработчик ввода сцены изменяющий модель Производства

public class InputHandler {

    public static final int LOOK_AROUND = 0;
    public static final int BLOCK_ADD = 1;
    public static final int BLOCK_DELETE = 2;
    public static final int BLOCK_MOVE = 3;
    public static final int BLOCK_ROTATE = 4;

    private HandleLookAround handleLookAround;
    private HandleBlockAdd handleBlockAdd;
    private HandleBlockDelete handleBlockDelete;
    private HandleBlockMove handleBlockMove;

    private ProductionScene scene;
    private Production production;
    private ProductionRenderer productionRenderer;

    
    public InputHandler(ProductionScene scene, Production production, ProductionRenderer productionRenderer) {
        this.production = production;
        this.productionRenderer = productionRenderer;
        this.scene = scene;
        handleLookAround = new HandleLookAround(scene, production, productionRenderer);
        handleBlockAdd = new HandleBlockAdd(scene, production, productionRenderer);
        handleBlockDelete = new HandleBlockDelete(scene, production, productionRenderer);
        handleBlockMove = new HandleBlockMove(scene, production, productionRenderer);
    }


    public void onMotion(MotionEvent event, float worldX, float worldY) {


        boolean blockTooggled = scene.blocksPanel.getToggledButton() != null;
        boolean modeToggled = scene.modePanel.getToggledButton() != null;

        // STATE
        int state = LOOK_AROUND;
        if (blockTooggled) state = BLOCK_ADD; else
        if (modeToggled) {
            if (scene.modePanel.getToggledButton().equals("0")) state = BLOCK_MOVE;
            if (scene.modePanel.getToggledButton().equals("1")) state = BLOCK_ROTATE;
            if (scene.modePanel.getToggledButton().equals("2")) state = BLOCK_DELETE;
        }

        switch (state) {
            case LOOK_AROUND: handleLookAround.onMotion(event, worldX, worldY); break;
            case BLOCK_ADD: handleBlockAdd.onMotion(event, worldX, worldY); break;
            case BLOCK_DELETE: handleBlockDelete.onMotion(event, worldX, worldY); break;
            case BLOCK_MOVE: handleBlockMove.onMotion(event, worldX, worldY); break;
        }


    }



}
