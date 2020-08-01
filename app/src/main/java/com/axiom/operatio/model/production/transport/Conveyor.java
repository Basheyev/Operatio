package com.axiom.operatio.model.production.transport;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameObject;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.operatio.model.production.blocks.Block;
import com.axiom.operatio.model.production.ProductionModel;
import com.axiom.operatio.model.production.materials.Item;

/**
 * Транспортировщик: конвейер, AGV, манимулятор (из буфера в буфер)
 *
 */
public class Conveyor extends Block {

    protected long deliveryTime;          // Время доставки в циклах производства
    protected long pushTimeCycle;         // Время забора материала
    protected long lastPushTime;          // Последнее время забора материала

    protected Sprite sprite;              // Спрайтовая анимация конвейера
    protected Sprite matImage;
    protected int animLR, animRL;
    protected int animUD, animDU;
    protected int animRU, animUR;

    public Conveyor(GameScene scene, ProductionModel p, int in, int out, int deliveryTime, int capacity, float scale) {
        super(scene, p, capacity, in, out);

        this.deliveryTime = deliveryTime;
        this.pushTimeCycle = deliveryTime / capacity;

        this.scale = scale;
        sprite = new Sprite(scene.getResources(), R.drawable.conveyor_texture,4,6);
        sprite.zOrder = 0;
        float w = sprite.getWidth() * scale;
        float h = sprite.getHeight() * scale;
        setLocalBounds(-w/2,-h/2,w/2,h/2);

        animLR = sprite.addAnimation(0,3,15,true);
        animRL = sprite.addAnimation(4,7,15,true);
        animUD = sprite.addAnimation(8,11,15,true);
        animDU = sprite.addAnimation(12,15,15,true);
        animRU = sprite.addAnimation(16,19,15,true);
        animUR = sprite.addAnimation(20,23,15,true);
        matImage = new Sprite(scene.getResources(), R.drawable.material);

        if (in==LEFT && out==RIGHT) sprite.setActiveAnimation(animLR);
        if (in==RIGHT && out==LEFT) sprite.setActiveAnimation(animRL);
        if (in==DOWN && out== UP) sprite.setActiveAnimation(animDU);
        if (in== UP && out==DOWN) sprite.setActiveAnimation(animUD);
        if (in==RIGHT && out==UP) sprite.setActiveAnimation(animRU);
        if (in==UP && out==RIGHT) sprite.setActiveAnimation(animUR);
        if (in==LEFT && out==UP) {
            sprite.setActiveAnimation(animRU);
            sprite.flipHorizontally(true);
        }
        if (in==DOWN && out==LEFT) {
            sprite.setActiveAnimation(animUR);
            sprite.flipVertically(true);
            sprite.flipHorizontally(true);
        }
        if (in==RIGHT && out==DOWN) {
            sprite.setActiveAnimation(animRU);
            sprite.flipVertically(true);
        }
        if (in==DOWN && out==RIGHT) {
            sprite.setActiveAnimation(animUR);
            sprite.flipVertically(true);
        }
        if (in==LEFT && out==DOWN) {
            sprite.setActiveAnimation(animRU);
            sprite.flipVertically(true);
            sprite.flipHorizontally(true);
        }
        if (in==UP && out==LEFT) {
            sprite.setActiveAnimation(animUR);
            sprite.flipHorizontally(true);
        }

    }


    @Override
    public boolean push(Item item) {
        long now = System.currentTimeMillis();
        if (now - lastPushTime < pushTimeCycle) return false;
        if (items.size() >= capacity) {
            setState(STATE_FAULT);
            return false;
        }
        if (item==null) return false;
        item.owner = this;
        item.processingStart = System.currentTimeMillis();
        items.add(item);
        if (items.size() >= capacity) setState(STATE_BUSY);
        lastPushTime = now;
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
        Block input = productionModel.getBlockAt(this, inputDirection);
        if (input!=null) {
            item = input.peek();
            if (item!=null && items.size() < capacity) {
                if (this.push(item)) input.poll();
            }
        }

        // Двигаем все материалы на конвейере
        Block output = productionModel.getBlockAt(this, outputDirection);
        if (output!=null) {
            item = this.peek(); // есть ли готовый материал
            if (item==null) return false;

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


    @Override
    public void draw(Camera camera) {
        super.draw(camera);
        sprite.draw(camera,x,y, scale);

        float progress;
        float xpos = 0, ypos = 0;
        long now;

        int counter = 0;
        int finishedCount = 0;

        for (Item item:items) {
            now = System.currentTimeMillis();
            progress = (now - item.processingStart) / ((float) deliveryTime);

            // Если есть завершенные, то располагаем в правильное место
            if (finishedCount > 0) {
                float maxProgress = 1 - counter * (1.0f / capacity);
                if (progress > maxProgress) progress = maxProgress;
            }

            // Если завершено, то располагаем в правильное место
            if (progress > 1) {
                progress = 1 - counter * (1.0f / capacity);
                finishedCount++;
            }

            if (inputDirection==LEFT && outputDirection==RIGHT) {
                xpos = progress - 0.5f;
                ypos = 0;
            } else if (inputDirection==RIGHT && outputDirection==LEFT) {
                xpos = 1 - progress - 0.5f;
                ypos = 0;
            } else if (inputDirection==DOWN && outputDirection== UP) {
                xpos = 0;
                ypos = progress - 0.5f;
            } else if (inputDirection== UP && outputDirection==DOWN) {
                xpos = 0;
                ypos = 1 - progress - 0.5f;
            }
            //-----------------------------------------------------------------------
            else if (inputDirection== RIGHT && outputDirection==UP) {
                float rads = (float) ((Math.PI*1.5) - (Math.PI/2 * progress));
                xpos = (float) (Math.cos(rads) + 1) / 2;
                ypos = (float) (Math.sin(rads) + 1) / 2;
            } else if (inputDirection== DOWN && outputDirection==RIGHT) {
                float rads = (float) ((Math.PI) - (Math.PI/2 * progress));
                xpos = (float) (Math.cos(rads) + 1) / 2;
                ypos = (float) (Math.sin(rads) - 1) / 2;
            } else if (inputDirection== LEFT && outputDirection==DOWN) {
                float rads = (float) ((Math.PI/2) - (Math.PI/2 * progress));
                xpos = (float) (Math.cos(rads) - 1) / 2;
                ypos = (float) (Math.sin(rads) - 1) / 2;
            } else if (inputDirection== UP && outputDirection==LEFT) {
                float rads = (float) -(Math.PI/2 * progress);
                xpos = (float) (Math.cos(rads) - 1) / 2;
                ypos = (float) (Math.sin(rads) + 1) / 2;
            }
            //-------------------------------------------------------------------------
            else if (inputDirection== UP && outputDirection==RIGHT) {
                float rads = (float) (Math.PI + (Math.PI / 2 * progress));
                xpos = (float) (Math.cos(rads) + 1) / 2;
                ypos = (float) (Math.sin(rads) + 1) / 2;
            } else if (inputDirection== LEFT && outputDirection==UP) {
                float rads = (float) ((Math.PI * 1.5) + (Math.PI / 2 * progress));
                xpos = (float) (Math.cos(rads) - 1) / 2;
                ypos = (float) (Math.sin(rads) + 1) / 2;
            } else if (inputDirection== DOWN && outputDirection==LEFT) {
                float rads = (float) (Math.PI / 2 * progress);
                xpos = (float) (Math.cos(rads) - 1) / 2;
                ypos = (float) (Math.sin(rads) - 1) / 2;
            } else if (inputDirection== RIGHT && outputDirection==DOWN) {
                float rads = (float) (Math.PI/2 + (Math.PI/2 * progress));
                xpos = (float) (Math.cos(rads) + 1) / 2;
                ypos = (float) (Math.sin(rads) - 1) / 2;
            }

            item.material.image.zOrder = 2;
            item.material.image.draw(camera,
                    x + xpos * sprite.getWidth() * scale,
                    y + ypos * sprite.getHeight() * scale,
                    scale/2);

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
