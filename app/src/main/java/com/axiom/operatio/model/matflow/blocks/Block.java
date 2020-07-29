package com.axiom.operatio.model.matflow.blocks;

import com.axiom.atom.engine.core.GameObject;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.operatio.model.matflow.materials.Item;

import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Базовый блок производства реализующий основную механику потока материалов.
 * (C) Bolat Basheyev 2020
 */
public abstract class Block extends GameObject {

    //------------------------------------------------------------------------------------------
    // Состояния блока
    //------------------------------------------------------------------------------------------
    public static final int STATE_IDLE = 0;      // Блок простаивает
    public static final int STATE_BUSY = 1;   // Блок работает над предметами
    public static final int STATE_FAULT = -1;    // Сбой машины

    //------------------------------------------------------------------------------------------
    // Основные параметры и данные блока
    //------------------------------------------------------------------------------------------
    protected Production production;             // производство в котором размещен блок
    protected int state;                         // текущее состояние блока
    protected int capacity;                      // максимальная ёмкость блока (предметов)
    protected int processingTime;                // время обработки в миллесекундах
    protected int inputDirection;                // направление из которого поступают материалы
    protected int outputDirection;               // направление на которое отправляются материалы
    protected ArrayBlockingQueue<Item> items;   // Перечень предметов находящихся в блоке
    public int column, row;                   // Расположение блока на сетке производства

    //------------------------------------------------------------------------------------------
    // Константы для указания положения блока источника и блока приемника
    //------------------------------------------------------------------------------------------
    public static final int NONE = 0;       // Направление не указано
    public static final int LEFT = 1;       // Блок слева
    public static final int RIGHT = 2;      // Блок справа
    public static final int UPPER = 3;      // Блок сверху
    public static final int DOWN = 4;       // Блок снизу


    /**
     * Конструктор блока: инициализирует параметры и данные блока
     * @param scene игровая сцена к которой относится блок
     * @param production производство к которому относится блок
     * @param capacity максимальная вместимость
     * @param flowIn направление из которого приходят материалы (FLOW_NONE - нет направления)
     * @param flowOut направление на которое отправляются материаы (FLOW_NONE - нет направления)
     */
    public Block(GameScene scene, Production production, int capacity, int flowIn, int flowOut) {
        super(scene);
        this.production = production;
        this.state = STATE_IDLE;
        this.capacity = capacity;
        items = new ArrayBlockingQueue<>(capacity);
        inputDirection = flowIn;
        outputDirection = flowOut;
        processingTime = 0;
        column = 0;
        row = 0;
    }

    @Override
    public void draw(Camera camera) {
        // Считаем загрузку блока нормированную на 0-1
        float load = capacity!=0 ? items.size() / (float) capacity : 0;
        GraphicsRender.setColor(load, 1 - load,0,0.3f);
        GraphicsRender.setZOrder(10);
        GraphicsRender.drawRectangle(getWorldBounds());
    }

    /**
     * Выполняет такт работы оборудования
     * @return true если успешно, false если сбой
     */
    public abstract boolean doWork();

    /**
     * Добавляет материал в блок
     * @param item экземпляр материала
     * @return true - если успешно, false - если нет
     */
    public abstract boolean push(Item item);

    /**
     * Узнать есть ли готовый материал на отдачу из блока
     * @return экземпляр материала или Null если не получилось
     */
    public abstract Item peek();

    /**
     * Забирает материал из блока
     * @return экземпляр материала или Null если не получилось
     */
    public abstract Item poll();

    public abstract boolean setState(int state);

    public abstract int getState();
}
