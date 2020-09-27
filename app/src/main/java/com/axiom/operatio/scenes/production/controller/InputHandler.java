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

    protected HandleLookAround handleLookAround;
    private HandleBlockAdd handleBlockAdd;
    private HandleBlockDelete handleBlockDelete;
    private HandleBlockMove handleBlockMove;
    private HandleBlockRotate handleBlockRotate;

    private ProductionScene scene;
    private Production production;
    private ProductionRenderer productionRenderer;

    
    public InputHandler(ProductionScene scene, Production production, ProductionRenderer productionRenderer) {
        this.production = production;
        this.productionRenderer = productionRenderer;
        this.scene = scene;
        handleLookAround = new HandleLookAround(this, scene, production, productionRenderer);
        handleBlockAdd = new HandleBlockAdd(this, scene, production, productionRenderer);
        handleBlockDelete = new HandleBlockDelete(this, scene, production, productionRenderer);
        handleBlockMove = new HandleBlockMove(this, scene, production, productionRenderer);
        handleBlockRotate = new HandleBlockRotate(this, scene, production, productionRenderer);
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
            case LOOK_AROUND: handleLookAround.onMotion(event, worldX, worldY); break;
            case BLOCK_ADD: handleBlockAdd.onMotion(event, worldX, worldY); break;
            case BLOCK_DELETE: handleBlockDelete.onMotion(event, worldX, worldY); break;
            case BLOCK_MOVE: handleBlockMove.onMotion(event, worldX, worldY); break;
            case BLOCK_ROTATE: handleBlockRotate.onMotion(event, worldX, worldY); break;
        }


    }



}
