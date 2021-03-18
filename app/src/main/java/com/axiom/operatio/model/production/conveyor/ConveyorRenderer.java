package com.axiom.operatio.model.production.conveyor;

import android.content.res.Resources;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.core.geometry.Vector;
import com.axiom.atom.engine.data.Channel;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.model.production.block.BlockRenderer;
import com.axiom.operatio.model.materials.Item;

import static com.axiom.operatio.model.production.block.Block.BUSY;
import static com.axiom.operatio.model.production.block.Block.DOWN;
import static com.axiom.operatio.model.production.block.Block.FAULT;
import static com.axiom.operatio.model.production.block.Block.LEFT;
import static com.axiom.operatio.model.production.block.Block.RIGHT;
import static com.axiom.operatio.model.production.block.Block.UP;


public class ConveyorRenderer extends BlockRenderer {

    protected static Sprite allConveyors = null;
    protected Block block;                                       // Блок к которму привзян рендер
    protected Sprite sprite;                                     // Спрайт конвейера
    protected Sprite fault;                                      // Значек сбоя
    protected int animStraight, animUpToRight, animRightToUp;

    private Vector coordBuffer = new Vector();

    public ConveyorRenderer(Block block) {
        this.block = block;
        if (allConveyors==null) {
            Resources resources = SceneManager.getResources();
            allConveyors = new Sprite(resources, R.drawable.blocks, 8, 11);
        }

        sprite = allConveyors.getAsSprite(40, 63);
        sprite.setZOrder(5);

        fault = allConveyors.getAsSprite(71);
        fault.setZOrder(8);

        createAnimations();
        adjustAnimation(block.getInputDirection(), block.getOutputDirection());
    }


    private void createAnimations() {
        animStraight = sprite.addAnimation(0,7, 15, true);
        animUpToRight = sprite.addAnimation(8,15, 15, true);
        animRightToUp = sprite.addAnimation(16,23, 15, true);
    }


    /**
     * Подготовить анимацию с учетом направления входа и выхода
     * @param inputDirection направление входа
     * @param outputDirection направление выхода
     */
    public void adjustAnimation(int inputDirection, int outputDirection) {
        if (inputDirection== LEFT && outputDirection== RIGHT)
            adjustSprite(animStraight, 0, false, false);
        if (inputDirection== RIGHT && outputDirection== LEFT)
            adjustSprite(animStraight, 0, true, false);
        if (inputDirection== DOWN && outputDirection== UP)
            adjustSprite(animStraight, (float) Math.PI / 2, false, false);
        if (inputDirection== UP && outputDirection== DOWN)
            adjustSprite(animStraight, (float) -Math.PI / 2, false, false);
        if (inputDirection== UP && outputDirection== RIGHT)
            adjustSprite(animUpToRight, 0, false, false);
        if (inputDirection== RIGHT && outputDirection== UP)
            adjustSprite(animRightToUp, 0, false, false);
        if (inputDirection== LEFT && outputDirection== UP)
            adjustSprite(animRightToUp, 0, true, false);
        if (inputDirection== LEFT && outputDirection== DOWN)
            adjustSprite(animRightToUp, 0, true, true);
        if (inputDirection== DOWN && outputDirection== LEFT)
            adjustSprite(animUpToRight, 0, true, true);
        if (inputDirection== RIGHT && outputDirection== DOWN)
            adjustSprite(animRightToUp, 0, false, true);
        if (inputDirection== DOWN && outputDirection== RIGHT)
            adjustSprite(animUpToRight, 0, false, true);
        if (inputDirection== UP && outputDirection== LEFT)
            adjustSprite(animUpToRight, 0, true, false);
    }


    private void adjustSprite(int activeAnimation, float rotation, boolean horizontalFlip, boolean verticalFlip) {
        sprite.setActiveAnimation(activeAnimation);
        sprite.setRotation(rotation);
        sprite.flipHorizontally(horizontalFlip);
        sprite.flipVertically(verticalFlip);
    }


    public void draw(Camera camera, float x, float y, float width, float height) {

        // Проверить поставлена ли на паузу игра
        boolean gamePaused = false;
        Production production = block.getProduction();
        if (production!=null) gamePaused = production.isPaused();

        // Если конвейер занят или игра поставлена на паузу - остановить анимацию движения
        if (block.getState()==BUSY || block.getState()==FAULT || gamePaused) {
            sprite.animationPaused = true;
        } else
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

        // Рисуем значок сбоя, если произошел сбой
        if (block instanceof Conveyor && block.getState()== Conveyor.FAULT) {
            fault.draw(camera, x, y, width, height);
        }

    }


    protected void drawItems(Camera camera, float x, float y, float width, float height) {

        Conveyor conveyor = (Conveyor) this.block;                  // Конвейер
        Channel<Item> inputQueue = conveyor.getInputQueue();        // Входящая очередь
        Channel<Item> outputQueue = conveyor.getOutputQueue();      // Исходящая очередь
        float cycleTime = this.block.getProduction().getCycleTimeMs();   // Длительность цикла в мс.
        float deliveryCycles = conveyor.getDeliveryCycles();        // Циклов для доставки предмета
        float deliveryTime = deliveryCycles * cycleTime;            // Время доставки в мс.
        float capacity = conveyor.getTotalCapacity();               // Вместимость конвейера в предметах
        float stridePerItem = 1.0f / (capacity / 2.0f);             // Шаг для одного предмета
        float progress;

        long now = conveyor.getProduction().getClock();
        float cycleBias = (now - conveyor.lastPollTime) / cycleTime;
        if (cycleBias > 1.0f) cycleBias = 1.0f;

        float progressBias = cycleBias * stridePerItem;
        int finishedCounter = conveyor.getOutputQueue().size();

        for (int i=0; i<outputQueue.size(); i++) {
            Item item = outputQueue.get(i);
            if (item==null) continue;
            progress = 1.0f - (i * stridePerItem) + progressBias - stridePerItem;
            drawItem (camera, x, y, width, height, item, progress);
        }


        float maxProgress = 1.0f - (finishedCounter * stridePerItem);
        for (int i=0; i<inputQueue.size(); i++) {
            Item item = inputQueue.get(i);
            if (item == null) continue;
            // fixme BUG: если конвейер остановлен Bias время item продолжает идти (считать время остановки)
            now = conveyor.getProduction().getClock();
            progress = (now - item.getTimeOwned()) /  deliveryTime;
            if (progress > maxProgress) {
                progress = maxProgress;
                maxProgress -= stridePerItem;
            }
            drawItem (camera, x, y, width, height, item, progress);
        }

    }


    protected void drawItem(Camera camera, float x, float y,
                            float width, float height,
                            Item item, float progress) {
        float xpos = 0, ypos = 0;

        int inputDirection = block.getInputDirection();
        int outputDirection = block.getOutputDirection();
        progressToCoordinates(progress, inputDirection, outputDirection, coordBuffer);

        Sprite materialSprite = item.getMaterial().getImage();

        float tx = x + xpos * width + width / 4;
        float ty = y + ypos * height + height / 4;
/*
        String debug = (block.getProduction().getCurrentCycle() - item.getCycleOwned()) + "\n"+progress;
        GraphicsRender.setColor(BLACK);
        GraphicsRender.drawText(debug, tx + width /4,ty +height/4,1);
*/
        materialSprite.setZOrder(sprite.getZOrder() + 1);
        materialSprite.draw(camera,tx,ty,width / 2, height / 2);

    }


    private void progressToCoordinates(float progress, int inpDir, int outDir, Vector result) {
        float rads, xpos = 0, ypos = 0;

        if (inpDir==LEFT && outDir==RIGHT) {
            xpos = progress - 0.5f;
            ypos = 0;
        } else if (inpDir==RIGHT && outDir==LEFT) {
            xpos = 1 - progress - 0.5f;
            ypos = 0;
        } else if (inpDir==DOWN && outDir== UP) {
            xpos = 0;
            ypos = progress - 0.5f;
        } else if (inpDir== UP && outDir==DOWN) {
            xpos = 0;
            ypos = 1 - progress - 0.5f;
        }
        //-----------------------------------------------------------------------
        else if (inpDir== RIGHT && outDir==UP) {
            rads = (float) ((Math.PI*1.5) - (Math.PI/2 * progress));
            xpos = (float) (Math.cos(rads) + 1) / 2;
            ypos = (float) (Math.sin(rads) + 1) / 2;
        } else if (inpDir== DOWN && outDir==RIGHT) {
            rads = (float) ((Math.PI) - (Math.PI/2 * progress));
            xpos = (float) (Math.cos(rads) + 1) / 2;
            ypos = (float) (Math.sin(rads) - 1) / 2;
        } else if (inpDir== LEFT && outDir==DOWN) {
            rads = (float) ((Math.PI/2) - (Math.PI/2 * progress));
            xpos = (float) (Math.cos(rads) - 1) / 2;
            ypos = (float) (Math.sin(rads) - 1) / 2;
        } else if (inpDir== UP && outDir==LEFT) {
            rads = (float) -(Math.PI/2 * progress);
            xpos = (float) (Math.cos(rads) - 1) / 2;
            ypos = (float) (Math.sin(rads) + 1) / 2;
        }
        //-------------------------------------------------------------------------
        else if (inpDir== UP && outDir==RIGHT) {
            rads = (float) (Math.PI + (Math.PI / 2 * progress));
            xpos = (float) (Math.cos(rads) + 1) / 2;
            ypos = (float) (Math.sin(rads) + 1) / 2;
        } else if (inpDir== LEFT && outDir==UP) {
            rads = (float) ((Math.PI * 1.5) + (Math.PI / 2 * progress));
            xpos = (float) (Math.cos(rads) - 1) / 2;
            ypos = (float) (Math.sin(rads) + 1) / 2;
        } else if (inpDir== DOWN && outDir==LEFT) {
            rads = (float) (Math.PI / 2 * progress);
            xpos = (float) (Math.cos(rads) - 1) / 2;
            ypos = (float) (Math.sin(rads) - 1) / 2;
        } else if (inpDir== RIGHT && outDir==DOWN) {
            rads = (float) (Math.PI/2 + (Math.PI/2 * progress));
            xpos = (float) (Math.cos(rads) + 1) / 2;
            ypos = (float) (Math.sin(rads) - 1) / 2;
        }

        result.x = xpos;
        result.y = ypos;

    }


}
