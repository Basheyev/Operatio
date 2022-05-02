package com.basheyev.operatio.scenes.production.controller;

import com.basheyev.atom.engine.input.ScaleEvent;
import com.basheyev.operatio.model.production.ProductionRenderer;

/**
 * Обработчик масштабирования сцены производства
 */
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
