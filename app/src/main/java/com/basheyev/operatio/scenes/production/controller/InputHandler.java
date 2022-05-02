package com.basheyev.operatio.scenes.production.controller;

import android.view.MotionEvent;

import com.basheyev.atom.engine.input.ScaleEvent;
import com.basheyev.operatio.model.production.Production;
import com.basheyev.operatio.model.production.ProductionRenderer;
import com.basheyev.operatio.scenes.production.ProductionScene;
import com.basheyev.operatio.scenes.production.view.ModePanel;

/**
 * Обработчик ввода сцены изменяющий модель Производства
 */
public class InputHandler {

    public static final int LOOK_AROUND = 0;
    public static final int BLOCK_ADD_MOVE = 1;
    public static final int BLOCK_DELETE = 2;
    public static final int BLOCK_ROTATE = 3;
    public static final int BUY_TILE = 4;

    private CameraMoveHandler cameraMoveHandler;
    private CameraScaleHandler cameraScaleHandler;
    private BlockDeleteHandler blockDeleteHandler;
    private BlockAddMoveHandler blockAddMoveHandler;
    private BlockRotateHandler blockRotateHandler;
    private BuyTileHandler buyTileHandler;
    private ProductionScene scene;

    
    public InputHandler(ProductionScene scene, Production production, ProductionRenderer productionRenderer) {
        this.scene = scene;
        cameraMoveHandler = new CameraMoveHandler(production,scene, productionRenderer);
        cameraScaleHandler = new CameraScaleHandler(this, productionRenderer);
        blockAddMoveHandler = new BlockAddMoveHandler(this, scene, production, productionRenderer);
        blockDeleteHandler = new BlockDeleteHandler(this, production, productionRenderer);
        blockRotateHandler = new BlockRotateHandler(this, production, productionRenderer);
        buyTileHandler = new BuyTileHandler(this, production, productionRenderer);
    }


    public void onMotion(MotionEvent event, float worldX, float worldY) {

        boolean blockTooggled = scene.getBlocksPanel().getToggledButton() != null;
        boolean modeToggled = scene.getModePanel().getToggledButton() != null;

        // STATE
        int state = LOOK_AROUND;
        if (blockTooggled) {
            state = BLOCK_ADD_MOVE;
        } else
        if (modeToggled) {
            String mode = scene.getModePanel().getToggledButton();
            if (mode.equals(ModePanel.modeTags[0])) state = BLOCK_ADD_MOVE;
            if (mode.equals(ModePanel.modeTags[1])) state = BLOCK_ROTATE;
            if (mode.equals(ModePanel.modeTags[2])) state = BLOCK_DELETE;
            if (mode.equals(ModePanel.modeTags[3])) state = BUY_TILE;
        }

        switch (state) {
            case LOOK_AROUND: cameraMoveHandler.onMotion(event, worldX, worldY); break;
            case BLOCK_ADD_MOVE: blockAddMoveHandler.onMotion(event, worldX, worldY); break;
            case BLOCK_DELETE: blockDeleteHandler.onMotion(event, worldX, worldY); break;
            case BLOCK_ROTATE: blockRotateHandler.onMotion(event, worldX, worldY); break;
            case BUY_TILE: buyTileHandler.onMotion(event, worldX, worldY); break;
        }

    }


    public void onScale(ScaleEvent event, float worldX, float worldY) {
        cameraScaleHandler.onScale(event);
    }


    /**
     * Отменяет все начатые действия
     */
    public void invalidateAllActions() {
        blockDeleteHandler.invalidateAction();
        blockAddMoveHandler.invalidateAction();
        blockRotateHandler.invalidateAction();
        cameraMoveHandler.invalidateAction();
        buyTileHandler.invalidateAction();
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

    public BuyTileHandler getBuyTileHandler() {
        return buyTileHandler;
    }
}
