package com.axiom.operatio.model.matflow.buffer;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameObject;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.operatio.model.matflow.blocks.Block;
import com.axiom.operatio.model.matflow.blocks.Production;
import com.axiom.operatio.model.matflow.materials.Item;
import com.axiom.operatio.model.matflow.materials.Material;

import java.util.LinkedList;

/**
 * Буфер для временного хранения однородных материалов (FIFO)
 * (C) Bolat Basheyev 2020
 */
public class Buffer extends Block {

    private Material material;        // Информация о хранимом материале
    private int maximumCapacity;      // Максимальная вместимость
    private Sprite sprite;            // Изображение буфера


    /**
     * Создаёт буфер нужного размера под необходимый материал
     * @param scene игровая сцена к которой относится игровой объект
     * @param info тип материала для хранения в буфере
     * @param capacity максимальное количество материалов хранимых в буфере
     * @param scale масштаб объекта
     */
    public Buffer(GameScene scene, Production production, Material info, int capacity, float scale) {
        super(scene,production,capacity,Block.NONE,Block.NONE);

        material = info;
        maximumCapacity = capacity;
        this.scale = scale;

        sprite = new Sprite(scene.getResources(), R.drawable.buffer_texture,4,4);
        sprite.zOrder = 1;
        float w = sprite.getWidth() * scale;
        float h = sprite.getHeight() * scale;
        setLocalBounds(-w/2,-h/2,w/2,h/2);

    }

    public boolean doWork() {
        return true;
    }

    /**
     * Добавляет материал в буфер, если он соответствует по типу и в буфере есть место
     * @param item который необходимо добавить в буфер
     * @return true - если материал успешно добавлен, false - если нет места или не тот материал
     */
    public boolean push(Item item) {
        if (item == null) return false;
        if (items.size() >= maximumCapacity) {
            setState(STATE_FAULT);
            return false;
        }
        if (!material.equals(item.info)) return false;
        item.owner = this;
        return items.add(item);
    }

    @Override
    public Item peek() {
        return items.peek();
    }

    /**
     * Вовзращает материал из начала буфера и убирает из буфера
     * @return материал из начала очереди
     */
    public Item poll() {
        return items.poll();
    }

    @Override
    public boolean setState(int state) {
        this.state = state;
        return true;
    }

    @Override
    public int getState() {
        return state;
    }

    /**
     * Убирает все материалы из буфера
     */
    public void clear() {
        items.clear();
    }

    /**
     * Возвращает количество материалов в буфере
     * @return количество материалов в буфере
     */
    public int itemsCount() {
        return items.size();
    }

    /**
     * Возвращает максимальную вместимость буфера
     * @return максимальная вместимость буфера
     */
    public int getCapacity() {
        return maximumCapacity;
    }

    /**
     * Возвращает стоимость материалов хранящихся в буфере
     * @return стоимость
     */
    public long getMaterialsValue() {
        return items.size() * material.price;
    }

    //------------------------------------------------------------------------------------------

    @Override
    public void update(float deltaTime) {

    }

    @Override
    public void draw(Camera camera) {
        super.draw(camera);
        float load = (float)items.size() / ((float) maximumCapacity);
        int frame = (int) Math.ceil(load * 8);  // всего кадров 8, поэтому нормируем вермя на кадры
        sprite.setActiveFrame(frame);
        sprite.draw(camera,x,y,scale);
        String bf1 = ""+ items.size();
        GraphicsRender.setZOrder(10);
        GraphicsRender.drawText(bf1.toCharArray(), x ,y ,scale / 3);
    }

    @Override
    public void onCollision(GameObject object) {

    }
}
