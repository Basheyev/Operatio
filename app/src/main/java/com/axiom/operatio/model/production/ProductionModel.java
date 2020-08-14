package com.axiom.operatio.model.production;

import com.axiom.atom.engine.core.GameObject;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.operatio.model.production.blocks.Block;
import com.axiom.operatio.model.production.buffer.Buffer;
import com.axiom.operatio.model.production.machine.Machine;
import com.axiom.operatio.model.production.machine.OperationOld;
import com.axiom.operatio.model.production.materials.Item;
import com.axiom.operatio.model.production.materials.Material;
import com.axiom.operatio.model.production.transport.Conveyor;

import java.util.ArrayList;

/**
 * Модель производства исполняющая симуляцию на базе блоков
 * (C) Bolat Basheyev 2020
 */
public class ProductionModel {

    //---------------------------------------------------------------------------------------
    // Основные данные производства
    //---------------------------------------------------------------------------------------
    protected Block[][] grid;                          // Сетка блоков производства (карта)
    protected ArrayList<Block> blocks;                 // Все блоки производства (список)
    public int columns, rows;                       // Размер карты производства в блоках
    protected GameScene scene;                         // Игровая сцена к которой относится

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
    public ProductionModel(GameScene gameScene, int cols, int rows, float gridSize) {
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
            case Block.UP:
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
            total += block.getItems().size();
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

        loadProductionObjects(0,0, scale);
        loadProductionObjects2(0,4, scale);
        loadProductionObjects3(8,1,scale);

     //   loadProductionObjects(8,0,scale);
     //   loadProductionObjects(8,4,scale);


    }



    public void loadProductionObjects(int col, int row, float scale) {
        Material material1 = Material.getMaterial(0);
        Material material2 = Material.getMaterial(8);
        Material material3 = Material.getMaterial(16);
        OperationOld op1 = new OperationOld(OperationOld.PROCESS, material1, material2,1,1);
        OperationOld op2 = new OperationOld(OperationOld.PROCESS, material2, material3,1,1);

        Buffer storage1 = new Buffer(scene, this, 100, scale);
        Machine machine1 = new Machine(scene,this, op1, Block.LEFT, Block.RIGHT,200, scale);
        Conveyor conv0 = new Conveyor(scene, this, Block.LEFT, Block.RIGHT,500, 3, scale);
        Machine machine2 = new Machine(scene, this, op2,  Block.LEFT, Block.RIGHT,200, scale);
        Buffer storage3 = new Buffer(scene,this, 100, scale);

        Conveyor conv = new Conveyor(scene, this, Block.LEFT, Block.RIGHT,500, 3, scale);
        Buffer storage4 = new Buffer(scene, this, 50, scale);


        Conveyor conv2 = new Conveyor(scene, this, Block.DOWN, Block.UP,500, 3, scale);
        Buffer storage5 = new Buffer(scene, this, 100, scale);
        Conveyor conv3 = new Conveyor(scene, this, Block.RIGHT, Block.LEFT, 1500, 3, scale);


        Conveyor conv4 = new Conveyor(scene, this, Block.RIGHT, Block.LEFT,1500, 3, scale);
        Conveyor conv5 = new Conveyor(scene, this, Block.RIGHT, Block.LEFT,1500, 3, scale);
        Conveyor conv6 = new Conveyor(scene, this, Block.RIGHT, Block.LEFT,1500, 3, scale);
        Conveyor conv7 = new Conveyor(scene, this, Block.RIGHT, Block.LEFT,1500, 3, scale);
        Conveyor conv8 = new Conveyor(scene, this, Block.UP, Block.DOWN,1500, 3, scale);
        Buffer storage6 = new Buffer(scene, this, 100, scale);

        setBlock(storage1, col,row+1);
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
        setBlock(storage6, col, row+3);
        setBlock(conv8, col, row+2);

        for (int i=0; i<64; i++) storage1.push(new Item(i));

    }


    public void loadProductionObjects2(int col, int row, float scale) {
        Material material1 = Material.getMaterial(0);
        Material material2 = Material.getMaterial(8);
        Material material3 = Material.getMaterial(17);
        OperationOld op1 = new OperationOld(OperationOld.PROCESS, material1, material2,1,1);
        OperationOld op2 = new OperationOld(OperationOld.PROCESS, material2, material3,1,1);

        Buffer storage1 = new Buffer(scene, this,  100, scale);
        Machine machine1 = new Machine(scene,this, op1, Block.LEFT, Block.RIGHT,200, scale);
        Conveyor conv0 = new Conveyor(scene, this, Block.LEFT, Block.RIGHT,500, 3, scale);
        Machine machine2 = new Machine(scene, this, op2,  Block.LEFT, Block.RIGHT,200, scale);
        Buffer storage3 = new Buffer(scene,this, 100, scale);

        Conveyor conv = new Conveyor(scene, this, Block.LEFT, Block.RIGHT,500, 3, scale);
        Buffer storage4 = new Buffer(scene, this, 50, scale);


        Conveyor conv2 = new Conveyor(scene, this, Block.DOWN, Block.LEFT,5000, 3, scale);
        Conveyor conv3 = new Conveyor(scene, this, Block.RIGHT, Block.LEFT,500, 3, scale);
        Conveyor conv4 = new Conveyor(scene, this, Block.RIGHT, Block.LEFT,500, 3, scale);
        Conveyor conv5 = new Conveyor(scene, this, Block.RIGHT, Block.LEFT,500, 3, scale);
        Conveyor conv6 = new Conveyor(scene, this, Block.RIGHT, Block.LEFT,500, 3, scale);
        Conveyor conv7 = new Conveyor(scene, this, Block.RIGHT, Block.LEFT,500, 3, scale);
        Conveyor conv8 = new Conveyor(scene, this, Block.RIGHT, Block.DOWN,500, 3, scale);

        setBlock(storage1, col,row+1);
        setBlock(machine1, col+1, row+1);
        setBlock(conv0, col+2, row+1);
        setBlock(machine2, col+3, row+1);
        setBlock(storage3, col+4, row+1);
        setBlock(conv, col+5,row+1);
        setBlock(storage4, col+6, row+1);
        setBlock(conv2, col+6,row+2);
        setBlock(conv3, col+5,row+2);
        setBlock(conv4, col+4,row+2);
        setBlock(conv5, col+3,row+2);
        setBlock(conv6, col+2,row+2);
        setBlock(conv7, col+1,row+2);
        setBlock(conv8, col, row+2);

        for (int i=0; i<64; i++) storage1.push(new Item(i));

    }

    public void loadProductionObjects3(int col, int row, float scale) {

        // Circle clock
        Conveyor c4 = new Conveyor(scene, this, Block.RIGHT, Block.UP,2000, 3, scale);
        setBlock(c4, col, row);
        Conveyor c5 = new Conveyor(scene, this, Block.DOWN, Block.RIGHT,2000, 3, scale);
        setBlock(c5, col, row+1);
        Conveyor c6 = new Conveyor(scene, this, Block.LEFT, Block.DOWN,2000, 3, scale);
        setBlock(c6, col+1, row+1);
        Conveyor c7 = new Conveyor(scene, this, Block.UP, Block.LEFT,2000, 3, scale);
        setBlock(c7, col+1, row);

        // Circle clockwise
        Conveyor c8 = new Conveyor(scene, this, Block.UP, Block.RIGHT,2000, 3, scale);
        setBlock(c8, col, row+3);
        Conveyor c9 = new Conveyor(scene, this, Block.LEFT, Block.UP,2000, 3, scale);
        setBlock(c9, col+1, row+3);
        Conveyor c10 = new Conveyor(scene, this, Block.DOWN, Block.LEFT,2000, 3, scale);
        setBlock(c10, col+1, row+4);
        Conveyor c1 = new Conveyor(scene, this, Block.RIGHT, Block.DOWN,2000, 3, scale);
        setBlock(c1, col, row+4);

        for (int i=0; i<3; i++) {
            c4.push(new Item(32+i));
            c8.push(new Item(40+i));
            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
      //  for (int i=0; i<3; i++)
    }


}
