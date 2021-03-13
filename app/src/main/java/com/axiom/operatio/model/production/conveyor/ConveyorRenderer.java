package com.axiom.operatio.model.production.conveyor;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.data.Channel;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.model.production.block.BlockRenderer;
import com.axiom.operatio.model.materials.Item;

import static com.axiom.operatio.model.production.block.Block.BUSY;
import static com.axiom.operatio.model.production.block.Block.DOWN;
import static com.axiom.operatio.model.production.block.Block.LEFT;
import static com.axiom.operatio.model.production.block.Block.RIGHT;
import static com.axiom.operatio.model.production.block.Block.UP;


public class ConveyorRenderer extends BlockRenderer {

    protected Block block;
    protected Sprite sprite;

    protected int animStraight, animUpToRight, animRightToUp;

    protected long timeStarted;

    public ConveyorRenderer(Block block) {
        this.block = block;
        sprite = new Sprite(SceneManager.getResources(), R.drawable.blocks,8,11);
        sprite.setZOrder(5);
        createAnimations();
        arrangeAnimation(block.getInputDirection(), block.getOutputDirection());
        timeStarted = block.getProduction().getClockMilliseconds();
    }

    private void createAnimations() {
        animStraight = sprite.addAnimation(40,47, 15, true);
        animUpToRight = sprite.addAnimation(48,55, 15, true);
        animRightToUp = sprite.addAnimation(56,63, 15, true);
    }


    /**
     * Подготовить анимацию с учетом направления входа и выхода
     * @param inputDirection
     * @param outputDirection
     */
    public void arrangeAnimation(int inputDirection, int outputDirection) {

        if (inputDirection== LEFT && outputDirection== RIGHT) {
            sprite.setActiveAnimation(animStraight);
            sprite.setRotation(0);
            sprite.flipHorizontally(false);
            sprite.flipVertically(false);
        }
        if (inputDirection== RIGHT && outputDirection== LEFT) {
            sprite.setActiveAnimation(animStraight);
            sprite.setRotation(0);
            sprite.flipHorizontally(true);
            sprite.flipVertically(false);
        }
        if (inputDirection== DOWN && outputDirection== UP) {
            sprite.setActiveAnimation(animStraight);
            sprite.setRotation((float) Math.PI / 2);
            sprite.flipHorizontally(false);
            sprite.flipVertically(false);
        }
        if (inputDirection== UP && outputDirection== DOWN) {
            sprite.setActiveAnimation(animStraight);
            sprite.setRotation((float) -Math.PI / 2);
            sprite.flipHorizontally(false);
            sprite.flipVertically(false);
        }
        if (inputDirection== UP && outputDirection== RIGHT) {
            sprite.setActiveAnimation(animUpToRight);
            sprite.setRotation(0);
            sprite.flipHorizontally(false);
            sprite.flipVertically(false);
        }
        if (inputDirection== RIGHT && outputDirection== UP) {
            sprite.setActiveAnimation(animRightToUp);
            sprite.setRotation(0);
            sprite.flipHorizontally(false);
            sprite.flipVertically(false);
        }
        if (inputDirection== LEFT && outputDirection== UP) {
            sprite.setActiveAnimation(animRightToUp);
            sprite.setRotation(0);
            sprite.flipHorizontally(true);
            sprite.flipVertically(false);
        }
        if (inputDirection== LEFT && outputDirection== DOWN) {
            sprite.setActiveAnimation(animRightToUp);
            sprite.setRotation(0);
            sprite.flipHorizontally(true);
            sprite.flipVertically(true);
        }
        if (inputDirection== DOWN && outputDirection== LEFT) {
            sprite.setActiveAnimation(animUpToRight);
            sprite.setRotation(0);
            sprite.flipHorizontally(true);
            sprite.flipVertically(true);
        }
        if (inputDirection== RIGHT && outputDirection== DOWN) {
            sprite.setActiveAnimation(animRightToUp);
            sprite.setRotation(0);
            sprite.flipHorizontally(false);
            sprite.flipVertically(true);
        }
        if (inputDirection== DOWN && outputDirection== RIGHT) {
            sprite.setActiveAnimation(animUpToRight);
            sprite.setRotation(0);
            sprite.flipHorizontally(false);
            sprite.flipVertically(true);
        }
        if (inputDirection== UP && outputDirection== LEFT) {
            sprite.setActiveAnimation(animUpToRight);
            sprite.setRotation(0);
            sprite.flipHorizontally(true);
            sprite.flipVertically(false);
        }
    }


    public void draw(Camera camera, float x, float y, float width, float height) {

        // Проверить поставлена ли на паузу игра
        boolean gamePaused = false;
        Production production = block.getProduction();
        if (production!=null) gamePaused = production.isPaused();

        // Если конвейер занят или игра поставлена на паузу - остановить анимацию движения
        if (block.getState()==BUSY || gamePaused)
            sprite.animationPaused = true;
        else
            sprite.animationPaused = false;

        // Отрисовать сам конвейер
        sprite.draw(camera,x,y, width, height);

        if (block instanceof Conveyor) {
            // Отрисовать предметы на нём
            drawItems(camera, x, y, width, height);

            // Отрисовать вход/выход
            drawInOut(camera, block.getInputDirection(), block.getOutputDirection(),
                    x, y, width, height, sprite.getZOrder() + 2);
        }

    }


    protected void drawItems(Camera camera, float x, float y, float width, float height) {

        Conveyor conveyor = (Conveyor) this.block;                       // Конвейер
        Channel<Item> inputQueue = conveyor.getInputQueue();        // Входящая очередь
        Channel<Item> outputQueue = conveyor.getOutputQueue();      // Исходящая очередь
        float cycleTime = this.block.getProduction().getCycleTimeMs();   // Длительность цикла в мс.
        float deliveryCycles = conveyor.getDeliveryCycles();        // Циклов для доставки предмета
        float deliveryTime = deliveryCycles * cycleTime;            // Время доставки в мс.
        float capacity = conveyor.getTotalCapacity();               // Вместимость конвейера в предметах
        float stridePerItem = 1.0f / (capacity / 2.0f);             // Шаг для одного предмета
        float progress = 1.0f;

        long now = conveyor.getProduction().getClockMilliseconds();
        float cycleBias = (now - conveyor.lastPollTime) / cycleTime;
        if (cycleBias > 1.0f) cycleBias = 1.0f;
        float progressBias = cycleBias * stridePerItem;
        int finishedCounter = conveyor.getOutputQueue().size();

        //Sprite materialSprite;
        for (int i=0; i<outputQueue.size(); i++) {
            Item item = outputQueue.get(i);
            if (item==null) continue;
            progress = 1.0f - (i * stridePerItem) + progressBias - stridePerItem;
            //materialSprite = item.getMaterial().getImage();
            //materialSprite.useColor = true;
            //materialSprite.setColor(Color.RED);
            drawItem (camera, x, y, width, height, item, progress);
        }


        float maxProgress = 1.0f - (finishedCounter * stridePerItem);
        for (int i=0; i<inputQueue.size(); i++) {
            Item item = inputQueue.get(i);
            if (item == null) continue;
            now = conveyor.getProduction().getClockMilliseconds();
            progress = (now - item.getTimeOwned()) /  deliveryTime;
            if (progress > maxProgress) {
                progress = maxProgress;
                maxProgress -= stridePerItem;
            }
            //materialSprite = item.getMaterial().getImage();
            //materialSprite.useColor = true;
            //materialSprite.setColor(Color.GREEN);
            drawItem (camera, x, y, width, height, item, progress);
        }

    }


    protected void drawItem(Camera camera, float x, float y,
                            float width, float height,
                            Item item, float progress) {
        float xpos = 0, ypos = 0;

        int inputDirection = block.getInputDirection();
        int outputDirection = block.getOutputDirection();

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

        materialSprite.setZOrder(sprite.getZOrder() + 1);
        materialSprite.draw(camera,
                x + xpos * width + width / 4,
                y + ypos * height + height / 4,
                width / 2, height / 2);

    }



}
