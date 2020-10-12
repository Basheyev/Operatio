package com.axiom.operatio.scenes.production.controller;

import android.view.MotionEvent;

import com.axiom.atom.engine.input.ScaleEvent;
import com.axiom.operatio.model.Production;
import com.axiom.operatio.model.ProductionRenderer;
import com.axiom.operatio.scenes.production.ProductionScene;

/**
 * Обработчик ввода сцены изменяющий модель Производства
 */
public class InputHandler {

    public static final int LOOK_AROUND = 0;
    public static final int BLOCK_ADD_MOVE = 1;
    public static final int BLOCK_DELETE = 2;
    public static final int BLOCK_ROTATE = 3;

    private CameraMoveHandler cameraMoveHandler;
    private CameraScaleHandler cameraScaleHandler;
    private BlockDeleteHandler blockDeleteHandler;
    private BlockAddMoveHandler blockAddMoveHandler;
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
        blockAddMoveHandler = new BlockAddMoveHandler(this, scene, production, productionRenderer);
        blockDeleteHandler = new BlockDeleteHandler(this, scene, production, productionRenderer);
        blockRotateHandler = new BlockRotateHandler(this, scene, production, productionRenderer);
    }


    public void onMotion(MotionEvent event, float worldX, float worldY) {

        boolean blockTooggled = scene.blocksPanel.getToggledButton() != null;
        boolean modeToggled = scene.modePanel.getToggledButton() != null;

        // STATE
        int state = LOOK_AROUND;
        if (blockTooggled) {
            state = BLOCK_ADD_MOVE;
        } else
        if (modeToggled) {
            if (scene.modePanel.getToggledButton().equals("0")) state = BLOCK_ADD_MOVE;
            if (scene.modePanel.getToggledButton().equals("1")) state = BLOCK_ROTATE;
            if (scene.modePanel.getToggledButton().equals("2")) state = BLOCK_DELETE;
        }

        switch (state) {
            case LOOK_AROUND: cameraMoveHandler.onMotion(event, worldX, worldY); break;
            case BLOCK_ADD_MOVE: blockAddMoveHandler.onMotion(event, worldX, worldY); break;
            case BLOCK_DELETE: blockDeleteHandler.onMotion(event, worldX, worldY); break;
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
        blockDeleteHandler.invalidateAction();
        blockAddMoveHandler.invalidateAction();
        cameraMoveHandler.invalidateAction();
        cameraScaleHandler.invalidateAction();
    }

    /**
     * Отменяет все начатые действия кроме масштабирования
     */
    public void invalidateAllActionsButScale() {
        blockDeleteHandler.invalidateAction();
        blockAddMoveHandler.invalidateAction();
        cameraMoveHandler.invalidateAction();
    }


    public CameraMoveHandler getCameraMoveHandler() {
        return cameraMoveHandler;
    }

    public CameraScaleHandler getCameraScaleHandler() {
        return cameraScaleHandler;
    }


    public BlockAddMoveHandler getBlockAddMoveHandler() {
        return blockAddMoveHandler;
    }

    public BlockDeleteHandler getBlockDeleteHandler() {
        return blockDeleteHandler;
    }

    public BlockRotateHandler getBlockRotateHandler() {
        return blockRotateHandler;
    }

}
