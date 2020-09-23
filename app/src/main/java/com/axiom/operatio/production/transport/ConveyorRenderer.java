package com.axiom.operatio.production.transport;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.data.Channel;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.operatio.production.block.Renderer;
import com.axiom.operatio.production.materials.Item;
import com.axiom.operatio.scenes.production.ProductionScene;

import static com.axiom.operatio.production.block.Block.DOWN;
import static com.axiom.operatio.production.block.Block.LEFT;
import static com.axiom.operatio.production.block.Block.RIGHT;
import static com.axiom.operatio.production.block.Block.UP;

public class ConveyorRenderer implements Renderer {

    protected Conveyor conveyor;
    protected Sprite sprite;

    protected int animLR, animRL;
    protected int animUD, animDU;
    protected int animRU, animUR;
    protected long timeStarted;

    public ConveyorRenderer(Conveyor conveyor) {
        this.conveyor = conveyor;
        sprite = new Sprite(SceneManager.getResources(), R.drawable.conveyor_texture,4,6);
        sprite.zOrder = 1;
        arrangeAnimation(conveyor.getInputDirection(), conveyor.getOutputDirection());
        timeStarted = System.currentTimeMillis();
    }


    private void arrangeAnimation(int inputDirection, int outputDirection) {

        animLR = sprite.addAnimation(0,3,15,true);
        animRL = sprite.addAnimation(4,7,15,true);
        animUD = sprite.addAnimation(8,11,15,true);
        animDU = sprite.addAnimation(12,15,15,true);
        animRU = sprite.addAnimation(16,19,15,true);
        animUR = sprite.addAnimation(20,23,15,true);

        if (inputDirection== LEFT && outputDirection== RIGHT) {
            sprite.setActiveAnimation(animLR);
        }
        if (inputDirection== RIGHT && outputDirection== LEFT) {
            sprite.setActiveAnimation(animRL);
        }
        if (inputDirection== DOWN && outputDirection== UP) {
            sprite.setActiveAnimation(animDU);
        }
        if (inputDirection== UP && outputDirection== DOWN) {
            sprite.setActiveAnimation(animUD);
        }
        if (inputDirection== RIGHT && outputDirection== UP) {
            sprite.setActiveAnimation(animRU);
        }
        if (inputDirection== UP && outputDirection== RIGHT) {
            sprite.setActiveAnimation(animUR);
        }
        if (inputDirection== LEFT && outputDirection== UP) {
            sprite.setActiveAnimation(animRU);
            sprite.flipHorizontally(true);
        }
        if (inputDirection== DOWN && outputDirection== LEFT) {
            sprite.setActiveAnimation(animUR);
            sprite.flipVertically(true);
            sprite.flipHorizontally(true);
        }
        if (inputDirection== RIGHT && outputDirection== DOWN) {
            sprite.setActiveAnimation(animRU);
            sprite.flipVertically(true);
        }
        if (inputDirection== DOWN && outputDirection== RIGHT) {
            sprite.setActiveAnimation(animUR);
            sprite.flipVertically(true);
        }
        if (inputDirection== LEFT && outputDirection== DOWN) {
            sprite.setActiveAnimation(animRU);
            sprite.flipVertically(true);
            sprite.flipHorizontally(true);
        }
        if (inputDirection== UP && outputDirection== LEFT) {
            sprite.setActiveAnimation(animUR);
            sprite.flipHorizontally(true);
        }
    }


    public void draw(Camera camera, float x, float y, float width, float height) {
        // Отрисовать сам конвейер
        sprite.draw(camera,x,y, width, height);
        // Отрисовать предметы на нём
        drawItems(camera, x, y, width, height);
        // Отрисовать количество предметов
      /*  String bf1 = "" + conveyor.getItemsAmount();
        GraphicsRender.setZOrder(10);
        GraphicsRender.drawText(bf1.toCharArray(), x + width /2 ,y + height / 2,1);*/
    }


    protected void drawItems(Camera camera, float x, float y, float width, float height) {

        float cycleTime = ProductionScene.getCycleTimeMs();   // Длительность цикла в мс.
        float deliveryCycles = conveyor.getDeliveryCycles();  // Циклов для доставки предмета
        float capacity = conveyor.getTotalCapacity();         // Вместимость конвейера в предметах
        float stridePerItem = 1.0f / (capacity / 2.0f);       // Шаг для одного предмета
        float deliveryTime = deliveryCycles * cycleTime;      // Время доставки в мс.
        float progress;                                       // Прогресс движения от 0.0 до 1.0

        // Разместим на конвейере доставленные предметы
        int finishedCounter = 0;
        Channel<Item> outputQueue = conveyor.getOutputQueue();

        // FIXME как то надо избавиться от использования итератора для снижения задержек GC
        for (int k=0; k<outputQueue.size(); k++) {
            Item item = outputQueue.get(k);
            if (item==null) continue;
            progress = 1.0f - (finishedCounter * stridePerItem);
            drawItem (camera, x, y, width, height, item, progress);
            finishedCounter++;
        }

        // Разместим на конвейере движущиеся предмаеты
        Channel<Item> inputQueue = conveyor.getInputQueue();
        float maxProgress = 1.0f - (finishedCounter * stridePerItem);
        long now;
        for (int k=0; k<inputQueue.size(); k++) {
            Item item = inputQueue.get(k);
            if (item==null) continue;
            now = System.currentTimeMillis();
            progress = (now - item.getTimeOwned()) /  deliveryTime;
            if (progress > maxProgress) {
                progress = maxProgress;
            }
            drawItem (camera, x, y, width, height, item, progress);
        }

    }


    protected void drawItem(Camera camera, float x, float y,
                            float width, float height,
                            Item item, float progress) {
        float xpos = 0, ypos = 0;

        int inputDirection = conveyor.getInputDirection();
        int outputDirection = conveyor.getOutputDirection();

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

        Sprite materialSprite = item.getMaterial().getImage();

        materialSprite.zOrder = 2;
        materialSprite.draw(camera,
                x + xpos * width + width / 4,
                y + ypos * height + height / 4,
                width / 2, height / 2);

    }



}
