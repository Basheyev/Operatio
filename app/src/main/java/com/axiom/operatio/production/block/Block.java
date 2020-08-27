package com.axiom.operatio.production.block;


import com.axiom.operatio.production.Production;
import com.axiom.operatio.production.materials.Item;

import java.util.concurrent.ArrayBlockingQueue;


/**
 * Базовый блок производства реализующий примитивную механику
 */
public abstract class Block {

    protected Production production;                  // Производство к которомуо относится блок
    protected int state = IDLE;                       // Текущее состояние блока
    protected int inputDirection, outputDirection;    // Направление ввода и вывода
    protected int inputCapacity, outputCapacity;      // Максимальая вместимость блока в предметах
    protected ArrayBlockingQueue<Item> input;         // Буферы ввода предметов
    protected ArrayBlockingQueue<Item> output;        // Буферы вывода предметов
    public int column, row;                           // Координаты блока в сетке карты
    protected Renderer renderer;                      // Отрисовщик

    /**
     * Конструктор блока производства
     * @param production производство
     * @param inCapacity размер буфера ввода в количестве предметов
     * @param outCapacity размер буфера вывода в количестве предметов
     */
    public Block(Production production, int inDir, int inCapacity, int outDir, int outCapacity) {
        this.production = production;
        this.inputDirection = inDir;
        this.inputCapacity = inCapacity;
        this.outputDirection = outDir;
        this.outputCapacity = outCapacity;
        this.input = new ArrayBlockingQueue<Item>(inCapacity);
        this.output = new ArrayBlockingQueue<Item>(outCapacity);
    }


    /**
     * Добавляет предмет во входную очередь блока
     * @param item предмет
     * @return true - если блок принял предмет, false - если нет
     */
    public boolean push(Item item) {
        if (item==null) return false;
        if (state==BUSY) return false;
        if (input.size()>=inputCapacity) return false;
        item.setOwner(this, Production.getCurrentCycle());
        input.add(item);
        return true;
    }


    /**
     * Возвращает обработанный предмет из блока, но не удаляет из выходной очереди
     * @return первый в очереди готовый предмет или null если такого нет
     */
    public Item peek() {
        return output.peek();
    }


    /**
     * Возвращает обработанный предмет из блока и удаляет из выходной очереди
     * @return первый в очереди готовый предмет или null если такого нет
     */
    public Item poll() {
        Item item = output.peek();
        if (item==null) return null;
        return output.poll();
    }


    /**
     * Забирает один предмет из блока по направлению входа
     * @return true - если получилось забрать, false - если нет
     */
    protected Item getItemFromInputDirection() {
        Block inputBlock = production.getBlockAt(this, inputDirection);
        if (inputBlock==null) return null;   // Если на входящем направление ничего нет
        Item item = inputBlock.peek();       // Пытаемся взять предмет из блока входа
        if (item==null) return null;         // Если ничего нет возвращаем false
        if (!push(item)) return null;        // Если не получилось добавить к себе - false
        return inputBlock.poll();            // Если получилось - удаляем из блока входа
    }

    /**
     * Возвращает состояние блока
     * @return состояние блока
     */
    public int getState() {
        return state;
    }

    /**
     * Возвращает общее количество предметов в блоке
     * @return общее количество предметов в блоке
     */
    public int getItemsAmount() {
        return input.size() + output.size();
    }

    public int getCapacity() {
        return inputCapacity + outputCapacity;
    }

    public int getInputDirection() {
        return inputDirection;
    }

    public int getOutputDirection() {
        return outputDirection;
    }

    public Renderer getRenderer() {
        return renderer;
    }

    /**
     * Обрабатывает входной поток предметов в выходной поток предметов
     * @return true - если успешно, false - если произошел сбой
     */
    public abstract void process();


    //---------------------------------------------------------------------------------------
    // Состояние блока
    //---------------------------------------------------------------------------------------
    public static final int IDLE = 0;
    public static final int BUSY = 1;
    public static final int FAULT = -1;

    //---------------------------------------------------------------------------------------
    // Ориентация
    //---------------------------------------------------------------------------------------
    public static final int NONE = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int UP = 3;
    public static final int DOWN = 4;


}
