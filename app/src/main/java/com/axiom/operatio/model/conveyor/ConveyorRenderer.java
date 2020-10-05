package com.axiom.operatio.model.conveyor;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.data.Channel;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.operatio.model.Production;
import com.axiom.operatio.model.block.BlockRenderer;
import com.axiom.operatio.model.materials.Item;

import static com.axiom.operatio.model.block.Block.DOWN;
import static com.axiom.operatio.model.block.Block.LEFT;
import static com.axiom.operatio.model.block.Block.RIGHT;
import static com.axiom.operatio.model.block.Block.UP;

public class ConveyorRenderer extends BlockRenderer {

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
        createAnimations();
        arrangeAnimation(conveyor.getInputDirection(), conveyor.getOutputDirection());
        timeStarted = Production.getClockMilliseconds();
    }

    private void createAnimations() {
        animLR = sprite.addAnimation(0,3,15,true);
        animRL = sprite.addAnimation(4,7,15,true);
        animUD = sprite.addAnimation(8,11,15,true);
        animDU = sprite.addAnimation(12,15,15,true);
        animRU = sprite.addAnimation(16,19,15,true);
        animUR = sprite.addAnimation(20,23,15,true);
    }


    public void arrangeAnimation(int inputDirection, int outputDirection) {

        if (inputDirection== LEFT && outputDirection== RIGHT) {
            sprite.setActiveAnimation(animLR);
            sprite.flipHorizontally(false);
            sprite.flipVertically(false);
        }
        if (inputDirection== RIGHT && outputDirection== LEFT) {
            sprite.setActiveAnimation(animRL);
            sprite.flipHorizontally(false);
            sprite.flipVertically(false);
        }
        if (inputDirection== DOWN && outputDirection== UP) {
            sprite.setActiveAnimation(animDU);
            sprite.flipHorizontally(false);
            sprite.flipVertically(false);
        }
        if (inputDirection== UP && outputDirection== DOWN) {
            sprite.setActiveAnimation(animUD);
            sprite.flipHorizontally(false);
            sprite.flipVertically(false);
        }
        if (inputDirection== RIGHT && outputDirection== UP) {
            sprite.setActiveAnimation(animRU);
            sprite.flipHorizontally(false);
            sprite.flipVertically(false);
        }
        if (inputDirection== UP && outputDirection== RIGHT) {
            sprite.setActiveAnimation(animUR);
            sprite.flipHorizontally(false);
            sprite.flipVertically(false);
        }
        if (inputDirection== LEFT && outputDirection== UP) {
            sprite.setActiveAnimation(animRU);
            sprite.flipHorizontally(true);
            sprite.flipVertically(false);
        }
        if (inputDirection== DOWN && outputDirection== LEFT) {
            sprite.setActiveAnimation(animUR);
            sprite.flipVertically(true);
            sprite.flipHorizontally(true);
        }
        if (inputDirection== RIGHT && outputDirection== DOWN) {
            sprite.setActiveAnimation(animRU);
            sprite.flipHorizontally(false);
            sprite.flipVertically(true);
        }
        if (inputDirection== DOWN && outputDirection== RIGHT) {
            sprite.setActiveAnimation(animUR);
            sprite.flipHorizontally(false);
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
            sprite.flipVertically(false);
        }
    }


    public void draw(Camera camera, float x, float y, float width, float height) {
        Production production = conveyor.getProduction();
        if (production!=null) {
            sprite.animationPaused = production.isPaused();
        }

        // Отрисовать сам конвейер
        sprite.draw(camera,x,y, width, height);
        // Отрисовать предметы на нём
        drawItems(camera, x, y, width, height);
        // Отрисовать вход/выход
        drawInOut(camera, conveyor.getInputDirection(), conveyor.getOutputDirection(),
                x, y, width, height, sprite.zOrder + 2);

    }


    // TODO Добавить остановку движения материалов при паузе производства
    protected void drawItems(Camera camera, float x, float y, float width, float height) {

        float cycleTime = Production.getCycleTimeMs();        // Длительность цикла в мс.
        float deliveryCycles = conveyor.getDeliveryCycles();  // Циклов для доставки предмета
        float capacity = conveyor.getTotalCapacity();         // Вместимость конвейера в предметах
        float stridePerItem = 1.0f / (capacity / 2.0f);       // Шаг для одного предмета
        float deliveryTime = deliveryCycles * cycleTime;      // Время доставки в мс.
        float progress;                                       // Прогресс движения от 0.0 до 1.0

        // Разместим на конвейере доставленные предметы
        int finishedCounter = 0;
        Channel<Item> outputQueue = conveyor.getOutputQueue();

        for (int k=0; k<outputQueue.size(); k++) {
            Item item = outputQueue.get(k);
            if (item==null) continue;
            progress = 1.0f - (finishedCounter * stridePerItem);
            drawItem (camera, x, y, width, height, item, progress);
            finishedCounter++;
        }

        // Разместим на конвейере движущиеся предметы
        Channel<Item> inputQueue = conveyor.getInputQueue();
        float maxProgress = 1.0f - (finishedCounter * stridePerItem);
        long now;
        for (int k=0; k<inputQueue.size(); k++) {
            Item item = inputQueue.get(k);
            if (item==null) continue;
            now = Production.getClockMilliseconds();
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
