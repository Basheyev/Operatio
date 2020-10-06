package com.axiom.operatio.scenes.production.controller;

import android.view.MotionEvent;

import com.axiom.operatio.model.Production;
import com.axiom.operatio.model.ProductionRenderer;
import com.axiom.operatio.scenes.production.ProductionScene;

// Обработчик ввода сцены изменяющий модель Производства

public class InputHandler {

    public static final int LOOK_AROUND = 0;
    public static final int BLOCK_ADD = 1;
    public static final int BLOCK_DELETE = 2;
    public static final int BLOCK_MOVE = 3;
    public static final int BLOCK_ROTATE = 4;

    protected CameraMoveHandler cameraMoveHandler;
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


    // TODO Invalidate actions correcly
    public void invalidateActions() {
        blockAddHandler.dragging = false;
        blockDeleteHandler.dragging = false;
        blockMoveHandler.dragging = false;
        cameraMoveHandler.dragging = false;
    }


    public BlockMoveHandler getBlockMoveHandler() {
        return blockMoveHandler;
    }

}
