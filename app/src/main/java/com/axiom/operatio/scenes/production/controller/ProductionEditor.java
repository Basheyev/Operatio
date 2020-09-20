package com.axiom.operatio.scenes.production.controller;

import android.util.Log;
import android.view.MotionEvent;

import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.operatio.production.Production;
import com.axiom.operatio.production.ProductionRenderer;
import com.axiom.operatio.production.block.Block;
import com.axiom.operatio.production.buffer.Buffer;
import com.axiom.operatio.production.machines.Machine;
import com.axiom.operatio.production.machines.MachineType;
import com.axiom.operatio.production.transport.Conveyor;
import com.axiom.operatio.scenes.production.ProductionScene;

// Задачи которые необходимо выполнить:
// Продумать архитектуру разделения
//
// TODO Перетаскивание блока с панели на карту (Drag & Drop)
// TODO Отображение будущего места блока на карте во время перетаскивания (занимаемое место)

// TODO Добавить перетаскивание блока с одного места на другое
// TODO Добавить вращение блока (направлений вход-выход)

// TODO Добавить кнопки на панель Буфер и Конвейер

public class ProductionEditor {

    private ProductionScene scene;
    private Production production;
    private ProductionRenderer productionRenderer;
    private boolean dragging = false;
    private float cursorX, cursorY;
    private int lastCol, lastRow;

    public ProductionEditor(ProductionScene scene, Production production, ProductionRenderer productionRenderer) {
        this.production = production;
        this.productionRenderer = productionRenderer;
        this.scene = scene;
    }

    public void onMotion(MotionEvent event, float worldX, float worldY) {
        int column = productionRenderer.getProductionColumn(worldX);
        int row = productionRenderer.getProductionRow(worldY);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastCol = column;
                lastRow = row;
                dragging = true;
                cursorX = worldX;
                cursorY = worldY;
                break;
            case MotionEvent.ACTION_MOVE:
                if (dragging) {
                    Camera camera = Camera.getInstance();
                    float x = camera.getX() + (cursorX - worldX);
                    float y = camera.getY() + (cursorY - worldY);
                    // Проверка границ карты
                    if (x - Camera.WIDTH / 2 < 0) x = Camera.WIDTH / 2;
                    if (y - Camera.HEIGHT / 2 < 0) y = Camera.HEIGHT / 2;
                    if (x + Camera.WIDTH / 2 > production.getColumns() * scene.cellWidth)
                        x = production.getColumns() * scene.cellWidth - Camera.WIDTH / 2;
                    if (y + Camera.HEIGHT / 2 > production.getRows() * scene.cellHeight)
                        y = production.getRows() * scene.cellHeight - Camera.HEIGHT / 2;
                    camera.lookAt(x, y);
                }
                break;
            case MotionEvent.ACTION_UP:
                dragging = false;
                if (column >= 0 && row >= 0 && lastCol==column && lastRow==row) {
                    Block block = production.getBlockAt(column, row);
                    if (block!=null) {
                        if (block instanceof Machine) SoundRenderer.playSound(scene.snd1);
                        if (block instanceof Conveyor) SoundRenderer.playSound(scene.snd2);
                        if (block instanceof Buffer) SoundRenderer.playSound(scene.snd3);
                        Log.i("PROD COL=" + column + ", ROW=" + row, block.toString());
                    } else if (scene.panel.getToggledButton()!=null) {
                        String toggled = scene.panel.getToggledButton();
                        addBlockAt(toggled, column, row);
                    }
                }

        }
    }

    protected void addBlockAt(String toggled, int column, int row) {
        Block block = null;
        MachineType mt = null;
        int choice = Integer.parseInt(toggled);
        switch (choice) {
            case 0:
                mt = MachineType.getMachineType(0);
                block = new Machine(production,
                        mt, mt.getOperations()[0],
                        Machine.LEFT, Machine.RIGHT);
                break;
            case 1:
                mt = MachineType.getMachineType(1);
                block = new Machine(production,
                        mt, mt.getOperations()[0],
                        Machine.LEFT, Machine.RIGHT);
                break;
            case 2:
                mt = MachineType.getMachineType(2);
                block = new Machine(production,
                        mt, mt.getOperations()[0],
                        Machine.LEFT, Machine.RIGHT);
                break;
            case 3:
                mt = MachineType.getMachineType(3);
                block = new Machine(production,
                        mt, mt.getOperations()[0],
                        Machine.LEFT, Machine.RIGHT);
                break;
            case 4:
                block = new Buffer(production, 100);
                break;
            case 5:
                block = new Conveyor(production, Block.LEFT, Block.RIGHT, 5);
                break;
        }

        if (block!=null) {
            production.setBlock(block, column, row);
        }

    }

}
