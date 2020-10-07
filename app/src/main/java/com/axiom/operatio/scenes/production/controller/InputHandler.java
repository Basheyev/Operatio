package com.axiom.operatio.scenes.production.controller;

import android.util.Log;
import android.view.MotionEvent;

import com.axiom.atom.engine.input.ScaleEvent;
import com.axiom.operatio.model.Production;
import com.axiom.operatio.model.ProductionRenderer;
import com.axiom.operatio.scenes.production.ProductionScene;

// Обработчик ввода сцены изменяющий модель Производства

// TODO При движении и увеличении учитывать границы
public class InputHandler {

    public static final int LOOK_AROUND = 0;
    public static final int BLOCK_ADD = 1;
    public static final int BLOCK_DELETE = 2;
    public static final int BLOCK_MOVE = 3;
    public static final int BLOCK_ROTATE = 4;

    private CameraMoveHandler cameraMoveHandler;
    private CameraScaleHandler cameraScaleHandler;
    private BlockAddHandler blockAddHandler;
    private BlockDeleteHandler blockDeleteHandler;
    private BlockMoveHandler blockMoveHandler;
    private BlockRotateHandler blockRotateHandler;


    private ProductionScene scene;
    private Production production;
    private ProductionRenderer productionRenderer;

    
    public InputHandler(ProductionScene scene, Production production, ProductionRenderer productionRenderer) {
        this.production = production;
        this.productionRenderer = productionRenderer;
        this.scene = scene;
        cameraMoveHandler = new CameraMoveHandler(this, scene, production, productionRenderer);
        cameraScaleHandler = new CameraScaleHandler(this, scene, production, productionRenderer);
        blockAddHandler = new BlockAddHandler(this, scene, production, productionRenderer);
        blockDeleteHandler = new BlockDeleteHandler(this, scene, production, productionRenderer);
        blockMoveHandler = new BlockMoveHandler(this, scene, production, productionRenderer);
        blockRotateHandler = new BlockRotateHandler(this, scene, production, productionRenderer);
    }


    public void onMotion(MotionEvent event, float worldX, float worldY) {

        boolean blockTooggled = scene.blocksPanel.getToggledButton() != null;
        boolean modeToggled = scene.modePanel.getToggledButton() != null;

        // STATE
        int state = LOOK_AROUND;
        if (blockTooggled) {
            state = BLOCK_ADD;
        } else
        if (modeToggled) {
            if (scene.modePanel.getToggledButton().equals("0")) state = BLOCK_MOVE;
            if (scene.modePanel.getToggledButton().equals("1")) state = BLOCK_ROTATE;
            if (scene.modePanel.getToggledButton().equals("2")) state = BLOCK_DELETE;
        }

        switch (state) {
            case LOOK_AROUND: cameraMoveHandler.onMotion(event, worldX, worldY); break;
            case BLOCK_ADD: blockAddHandler.onMotion(event, worldX, worldY); break;
            case BLOCK_DELETE: blockDeleteHandler.onMotion(event, worldX, worldY); break;
            case BLOCK_MOVE: blockMoveHandler.onMotion(event, worldX, worldY); break;
            case BLOCK_ROTATE: blockRotateHandler.onMotion(event, worldX, worldY); break;
        }

    }


    public void onScale(ScaleEvent event, float worldX, float worldY) {
        cameraScaleHandler.onScale(event, worldX, worldY);
    }


    /**
     * Отменяет все начатые действия
     */
    public void invalidateAllActions() {
        blockAddHandler.invalidateAction();
        blockDeleteHandler.invalidateAction();
        blockMoveHandler.invalidateAction();
        cameraMoveHandler.invalidateAction();
        cameraScaleHandler.invalidateAction();
    }

    /**
     * Отменяет все начатые действия
     */
    public void invalidateAllActionsButScale() {
        blockAddHandler.invalidateAction();
        blockDeleteHandler.invalidateAction();
        blockMoveHandler.invalidateAction();
        cameraMoveHandler.invalidateAction();
    }


    public CameraMoveHandler getCameraMoveHandler() {
        return cameraMoveHandler;
    }

    public CameraScaleHandler getCameraScaleHandler() {
        return cameraScaleHandler;
    }

    public BlockAddHandler getBlockAddHandler() {
        return blockAddHandler;
    }

    public BlockMoveHandler getBlockMoveHandler() {
        return blockMoveHandler;
    }

    public BlockDeleteHandler getBlockDeleteHandler() {
        return blockDeleteHandler;
    }

    public BlockRotateHandler getBlockRotateHandler() {
        return blockRotateHandler;
    }
}
