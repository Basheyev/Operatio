package com.axiom.operatio.model.matflow.transport;

import android.util.Log;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameObject;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.operatio.model.matflow.blocks.Block;
import com.axiom.operatio.model.matflow.blocks.Production;
import com.axiom.operatio.model.matflow.materials.Item;

/**
 * Транспортировщик: конвейер, AGV, манимулятор (из буфера в буфер)
 *
 */
public class Conveyor extends Block {

    protected long deliveryTime;          // Время доставки в циклах производства
    protected Sprite sprite;              // Спрайтовая анимация конвейера
    protected Sprite matImage;
    protected int animLR, animRL;
    protected int animUD, animDU;

    // TODO стыковка конвейр-конвейер, конвейр-машина
    public Conveyor(GameScene scene, Production p, int in, int out, int deliveryTime, int capacity, float scale) {
        super(scene, p, capacity, in, out);

        this.scale = scale;
        sprite = new Sprite(scene.getResources(), R.drawable.conveyor_texture,4,4);
        sprite.zOrder = 0;
        float w = sprite.getWidth() * scale;
        float h = sprite.getHeight() * scale;
        setLocalBounds(-w/2,-h/2,w/2,h/2);

        animLR = sprite.addAnimation(0,3,15,true);
        animRL = sprite.addAnimation(4,7,15,true);
        animUD = sprite.addAnimation(8,11,15,true);
        animDU = sprite.addAnimation(12,15,15,true);
        matImage = new Sprite(scene.getResources(), R.drawable.material);

        if (in==LEFT && out==RIGHT) sprite.setActiveAnimation(animLR);
        if (in==RIGHT && out==LEFT) sprite.setActiveAnimation(animRL);
        if (in==DOWN && out==UPPER) sprite.setActiveAnimation(animDU);
        if (in==UPPER && out==DOWN) sprite.setActiveAnimation(animUD);

        this.deliveryTime = deliveryTime;
    }


    @Override
    public boolean push(Item item) {
        if (items.size() >= capacity) {
            setState(STATE_FAULT);
            return false;
        }
        if (item==null) return false;
        item.owner = this;
        item.processingStart = System.currentTimeMillis();
        items.add(item);
        if (items.size() >= capacity) setState(STATE_BUSY);
        return true;
    }

    public Item peek() {
        long now = System.currentTimeMillis();
        for (Item item:items) {
            if (item.owner==this && now > (item.processingStart + deliveryTime)) {
                return item;
            }
        }
        return null;
    }

    @Override
    /**
     * Возвращает первый из уже доставленных предметов
     */
    public Item poll() {
        long now = System.currentTimeMillis();
        for (Item item:items) {
            if (now > (item.processingStart + deliveryTime) || item.owner!=this) {
                items.remove(item);
                setState(STATE_IDLE);
                return item;
            }
        }
        return null;
    }


    public boolean doWork() {
        Item item;

        // Если можем, то забираем из входного буфера материал
        Block input = production.getBlockAt(this, inputDirection);
        if (input!=null) {
            item = input.peek();
            if (item!=null && items.size() < capacity) {
                if (this.push(item)) input.poll();
            }
        }

        // Двигаем все материалы на конвейере
        Block output = production.getBlockAt(this, outputDirection);
        if (output!=null) {
            item = this.peek(); // есть ли готовый материал
            if (item==null) return false;

            // FIXME не уходит предмет
            if (column==8 && row==4) {
                output.peek();
            }

            if (output.push(item)) {
                this.poll();
            }

        }

        return true;
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

    //------------------------------------------------------------------------------------------


    @Override
    public void update(float deltaTime) {

    }

    // TODO Направление конвейера будет отличатся лишь по отображению
    // TODO Развить класс с транспортом: left-Right, right-left и обратные up-down, down-up
    // TODO а также угловые перемещения: left-up, left-down, up-right, down-right
    // TODO а также обратные перемещения: up-left, down-left, right-up, right-down
    @Override
    public void draw(Camera camera) {

        super.draw(camera);

        sprite.draw(camera,x,y, scale);

        float progress;
        float xpos = 0, ypos = 0;
        long now;

        int counter = 0;
        for (Item item:items) {
            now = System.currentTimeMillis();
            progress = (now - item.processingStart) / ((float) deliveryTime);

            if (progress > 1) {
                progress = 1 - counter * (1.0f / capacity);
            }

            if (inputDirection==LEFT && outputDirection==RIGHT) {
                xpos = progress * sprite.getWidth() * scale;
                ypos = sprite.getHeight() / 2 * scale;
            } else if (inputDirection==RIGHT && outputDirection==LEFT) {
                xpos = (1 - progress) * sprite.getWidth() * scale;
                ypos = sprite.getHeight() / 2 * scale;
            } else if (inputDirection==DOWN && outputDirection==UPPER) {
                xpos = sprite.getWidth() / 2 * scale;
                ypos = progress * sprite.getHeight() * scale;
            } else if (inputDirection==UPPER && outputDirection==DOWN) {
                xpos = sprite.getWidth() / 2 * scale;
                ypos = (1 - progress) * sprite.getHeight() * scale;
            }

            matImage.zOrder = 2;
            matImage.draw(camera,
                    x + xpos - sprite.getWidth()/2 * scale,
                    y + ypos - sprite.getHeight()/2 * scale,
                    scale);
            counter++;
        }

        String bf1 = ""+ items.size();
        GraphicsRender.setZOrder(10);
        GraphicsRender.drawText(bf1.toCharArray(), x ,y,scale / 3);
    }

    @Override
    public void onCollision(GameObject object) {

    }

}
