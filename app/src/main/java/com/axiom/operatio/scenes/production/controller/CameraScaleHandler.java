package com.axiom.operatio.scenes.production.controller;

import com.axiom.atom.engine.input.ScaleEvent;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.ProductionRenderer;
import com.axiom.operatio.scenes.production.ProductionScene;

public class CameraScaleHandler {

    private InputHandler inputHandler;
    private ProductionScene scene;
    private Production production;
    private ProductionRenderer productionRenderer;
    private boolean actionInProgress = false;

    public CameraScaleHandler(InputHandler inputHandler, ProductionScene scene, Production production,
                           ProductionRenderer productionRenderer) {
        this.inputHandler = inputHandler;
        this.production = production;
        this.productionRenderer = productionRenderer;
        this.scene = scene;
    }


    public void onScale(ScaleEvent event, float worldX, float worldY) {
        inputHandler.invalidateAllActionsButScale();
        productionRenderer.doScale(event.scaleFactor);
    }


    public void invalidateAction() {

    }

}
