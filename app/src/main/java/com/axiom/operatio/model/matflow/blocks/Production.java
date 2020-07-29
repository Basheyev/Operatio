package com.axiom.operatio.model.matflow.blocks;

import android.util.Log;

import com.axiom.atom.engine.core.GameObject;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.operatio.model.matflow.buffer.Buffer;
import com.axiom.operatio.model.matflow.machine.Machine;
import com.axiom.operatio.model.matflow.machine.Operation;
import com.axiom.operatio.model.matflow.materials.Item;
import com.axiom.operatio.model.matflow.materials.Material;
import com.axiom.operatio.model.matflow.transport.Conveyor;

import java.util.ArrayList;

/**
 * Модель производства исполняющая симуляцию на базе блоков
 * (C) Bolat Basheyev 2020
 */
public class Production {

    //---------------------------------------------------------------------------------------
    // Основные данные производства
    //---------------------------------------------------------------------------------------
    protected Block[][] grid;                          // Сетка блоков производства (карта)
    protected ArrayList<Block> blocks;                 // Все блоки производства (список)
    public int columns, rows;                       // Размер карты производства в блоках
    protected GameScene scene;                         // Игровая сцена к которой относится
    protected Material material;

    //--------------------------------------------------------------------------------------
    // Основные параметры производства
    //--------------------------------------------------------------------------------------
    public long cycleTime = 50;     // Длительность производственного цикла в миллисекундах
    private long lastCycleTime;     // Время начала прошлого цикла в миллисекундах
    private float gridSize;         // Размер блока в пикселях
    public long cycle = 0;          // Счётчик цикла производства

    /**
     * Конструктор производства
     * @param gameScene игровая сцена
     * @param cols количество столбцов производства в блоках
     * @param rows количество строк производства в блоках
     * @param gridSize размер блока в пикселях
     */
    public Production(GameScene gameScene, int cols, int rows,float gridSize) {
        this.scene = gameScene;
        this.columns = cols;
        this.rows = rows;
        this.gridSize = gridSize;

        grid = new Block[rows][cols];
        blocks = new ArrayList<>();

        loadLevel();
        lastCycleTime = System.currentTimeMillis();
    }


    /**
     * Такт производства
     */
    private boolean bugAlreadyShown = false;
    public void productionCycle() {

        long now = System.currentTimeMillis();   // берем текущее время в миллисекундах
        if (now - lastCycleTime >= cycleTime) {  // ожидаем время следующего цикла
            for (int i = 0; i<blocks.size(); i++) {
                blocks.get(i).doWork();



                // TODO Этот кусок кода показывает кто потерял предмет при total=396
                if (getTotalItems() < 396 && !bugAlreadyShown) {
                    Block b = blocks.get(i);
                    Log.i("OBJECT LOST AFTER:" + b, " COL=" + b.column + " ROW=" + b.row);
                    bugAlreadyShown = true;
                }




            }
            lastCycleTime = now;                 // сохраняем время начала последнего цикла
            cycle++;


        }
    }

    /**
     * Очистить все данные производства
     */
    public void clearBlocks() {
        for (int row=0; row < rows;row++) {
            for (int col=0; col < columns; col++) {
                grid[row][col] = null;
            }
        }
        blocks.clear();
    }


    /**
     * Установить блок в сетке производства
     * @param block блок производства
     * @param col столбец
     * @param row строка
     * @return true - если блок успешно добавлен, false если нет
     */
    public boolean setBlock(Block block, int col, int row) {
        if (block==null) return false;

        if (col < 0 || col >= columns) return false;
        if (row < 0 || row >= rows) return false;

        grid[row][col] = block;
        block.column = col;
        block.row = row;
        blocks.add(block);

        GameObject gameObject = (GameObject) block;
        gameObject.x = (col * gridSize) + (gridSize / 2);
        gameObject.y = (row * gridSize) + (gridSize / 2);
        scene.addObject(gameObject);

        return true;
    }

    public Block getBlockAt(int col, int row) {
        if (col < 0 || col >= columns) return null;
        if (row < 0 || row >= rows) return null;
        return grid[row][col];
    }

    public Block getBlockAt(Block relativeTo, int direction) {
        switch (direction) {
            case Block.LEFT:
                return getBlockAt(relativeTo.column - 1, relativeTo.row);
            case Block.RIGHT:
                return getBlockAt(relativeTo.column+1, relativeTo.row);
            case Block.UPPER:
                return getBlockAt(relativeTo.column, relativeTo.row + 1);
            case Block.DOWN:
                return getBlockAt(relativeTo.column, relativeTo.row - 1);
            default:
                return null;
        }
    }

    public int getTotalItems() {
        int total = 0;
        for (Block block:blocks) {
            total += block.items.size();
        }
        return total;
    }

    /**
     * Делает демо уровень: склад сырья (corn) -> машина (fryer) - склад готовой продукции (popcorn)
     * TODO сделать загрузку схем производства из файла
     */
    public void loadLevel() {

        clearBlocks();

        float scale = gridSize / 32; // Sprite size 32x32
        material = new Material(0,"material", "",0,0);
        loadProductionObjects(0,0, scale);
        loadProductionObjects(0,4,scale);
        loadProductionObjects(8,0,scale);
        loadProductionObjects(8,4,scale);
        Conveyor c1 = new Conveyor(scene, this, Block.UPPER, Block.DOWN,500, 3, scale);
        setBlock(c1, 8, 4);
        Conveyor c2 = new Conveyor(scene, this, Block.RIGHT, Block.LEFT,500, 3, scale);
        setBlock(c2, 7, 1);
        Conveyor c3 = new Conveyor(scene, this, Block.DOWN, Block.UPPER,500, 3, scale);
        setBlock(c3, 6, 4);
        Conveyor c4 = new Conveyor(scene, this, Block.LEFT, Block.RIGHT,500, 3, scale);
        setBlock(c4, 7, 5);
    }

    public void loadProductionObjects(int col, int row, float scale) {

        Operation op1 = new Operation(Operation.PROCESS, material, material,1,1);
        Operation op2 = new Operation(Operation.PROCESS, material, material,1,1);

        Buffer storage1 = new Buffer(scene, this, material, 100, scale);
        Machine machine1 = new Machine(scene,this, op1, Block.LEFT, Block.RIGHT,200, scale);
     //   Buffer storage2 = new Buffer(scene, this, material, 100, scale);
        Conveyor conv0 = new Conveyor(scene, this, Block.LEFT, Block.RIGHT,500, 3, scale);
        Machine machine2 = new Machine(scene, this, op2,  Block.LEFT, Block.RIGHT,200, scale);
        Buffer storage3 = new Buffer(scene,this, material, 100, scale);

        Conveyor conv = new Conveyor(scene, this, Block.LEFT, Block.RIGHT,500, 3, scale);
        Buffer storage4 = new Buffer(scene, this, material, 50, scale);


        Conveyor conv2 = new Conveyor(scene, this, Block.DOWN, Block.UPPER,500, 3, scale);
        Buffer storage5 = new Buffer(scene, this, material, 100, scale);
        Conveyor conv3 = new Conveyor(scene, this, Block.RIGHT, Block.LEFT,500, 3, scale);


        Conveyor conv4 = new Conveyor(scene, this, Block.RIGHT, Block.LEFT,500, 3, scale);
        Conveyor conv5 = new Conveyor(scene, this, Block.RIGHT, Block.LEFT,500, 3, scale);
        Conveyor conv6 = new Conveyor(scene, this, Block.RIGHT, Block.LEFT,500, 3, scale);
        Conveyor conv7 = new Conveyor(scene, this, Block.RIGHT, Block.LEFT,500, 3, scale);
        Conveyor conv8 = new Conveyor(scene, this, Block.UPPER, Block.DOWN,500, 3, scale);
        Buffer storage6 = new Buffer(scene, this, material, 100, scale);


        setBlock(storage1, col+0,row+1);
        setBlock(machine1, col+1, row+1);
        setBlock(conv0, col+2, row+1);
        setBlock(machine2, col+3, row+1);
        setBlock(storage3, col+4, row+1);
        setBlock(conv, col+5,row+1);
        setBlock(storage4, col+6, row+1);
        setBlock(conv2, col+6,row+2);
        setBlock(storage5, col+6, row+3);
        setBlock(conv3, col+5,row+3);
        setBlock(conv4, col+4,row+3);
        setBlock(conv5, col+3,row+3);
        setBlock(conv6, col+2,row+3);
        setBlock(conv7, col+1,row+3);
        setBlock(storage6, col+0, row+3);
        setBlock(conv8, col+0, row+2);

        for (int i=0; i<99; i++) storage1.push(new Item(material));

    }

}
