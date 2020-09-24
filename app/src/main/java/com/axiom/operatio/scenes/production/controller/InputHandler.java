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
//  TODO Разделить на несколько обработчиков:
//  TODO 1. Перемещение камеры по карте производства
//  TODO 2. Перемещение блока производства и отображение будущего занимаемого места (Drag & Drop)
//  TODO 3. Перетаскивание блока с панели на карту производства и отображение (Drag & Drop)
//  TODO 4. Вращение блока производства (направлений вход-выход)
//  TODO 5. Удаление блока производства

public class InputHandler {

    private HandleLookAround handleLookAround;
    private HandleBlockAdd handleBlockAdd;
    private HandleBlockDelete handleBlockDelete;

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
    }

    public void onMotion(MotionEvent event, float worldX, float worldY) {
        // STATE
        boolean blockTooggled = scene.blocksPanel.getToggledButton() != null;
        boolean modeToggled = scene.modePanel.getToggledButton() != null;

        if (blockTooggled || modeToggled) {
            if (modeToggled) handleBlockDelete.onMotion(event, worldX, worldY);
            if (blockTooggled) handleBlockAdd.onMotion(event, worldX, worldY);
        } else {
            handleLookAround.onMotion(event, worldX, worldY);
        }
    }



}
