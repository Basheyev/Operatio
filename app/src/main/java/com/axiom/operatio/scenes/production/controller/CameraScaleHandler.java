package com.axiom.operatio.scenes.production.controller;

import com.axiom.atom.engine.input.ScaleEvent;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.ProductionRenderer;
import com.axiom.operatio.scenes.production.ProductionScene;

public class CameraScaleHandler {

    private InputHandler inputHandler;
    private ProductionRenderer productionRenderer;

    public CameraScaleHandler(InputHandler inputHandler, ProductionRenderer productionRenderer) {
        this.inputHandler = inputHandler;
        this.productionRenderer = productionRenderer;
    }

    public void onScale(ScaleEvent event) {
        inputHandler.invalidateAllActions();
        productionRenderer.doScale(event.scaleFactor);
    }

}
