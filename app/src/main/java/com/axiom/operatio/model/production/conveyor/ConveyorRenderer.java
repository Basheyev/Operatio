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
import com.axiom.operatio.model.production.machine.Machine;

import static com.axiom.operatio.model.production.block.Block.BUSY;
import static com.axiom.operatio.model.production.block.Block.DOWN;
import static com.axiom.operatio.model.production.block.Block.FAULT;
import static com.axiom.operatio.model.production.block.Block.LEFT;
import static com.axiom.operatio.model.production.block.Block.RIGHT;
import static com.axiom.operatio.model.production.block.Block.UP;

/**
 * Рендер конвейера
 */
public class ConveyorRenderer extends BlockRenderer {

    protected static Sprite allConveyors = null;
    protected Block block;                                       // Блок к которому привзян рендер
    protected Sprite sprite;                                     // Спрайт основного конвейера
    protected Sprite leftJointSprite, rightJointSprite;            // Спрайты боковых соединений
    protected Sprite fault;                                      // Значек сбоя
    protected int animStraight, animUpToRight, animRightToUp;
    protected int animL_U2R, animL_R2U;
    protected int animR_U2R, animR_R2U;
    protected Block lastLeftBlock, lastRightBlock;

    private Vector coordBuffer = new Vector();

    public ConveyorRenderer(Block block) {
        this.block = block;
        if (allConveyors==null) {
            Resources resources = SceneManager.getResources();
            allConveyors = new Sprite(resources, R.drawable.blocks, 8, 16);
        }

        sprite = allConveyors.getAsSprite(40, 63);
        sprite.setZOrder(5);

        leftJointSprite = allConveyors.getAsSprite(48, 63);
        leftJointSprite.setZOrder(4);
        rightJointSprite = allConveyors.getAsSprite(48, 63);
        leftJointSprite.setZOrder(4);

        fault = allConveyors.getAsSprite(71);
        fault.setZOrder(8);

        createAnimations();
        adjustAnimation(block.getInputDirection(), block.getOutputDirection());
    }


    private void createAnimations() {

        animStraight = sprite.addAnimation(0,7, 25, true);
        animUpToRight = sprite.addAnimation(8,15, 25, true);
        animRightToUp = sprite.addAnimation(16,23, 25, true);

        animL_U2R = leftJointSprite.addAnimation(0,7, 25, true);
        animL_R2U = leftJointSprite.addAnimation(8,15, 25, true);

        animR_U2R = rightJointSprite.addAnimation(0,7, 25, true);
        animR_R2U = rightJointSprite.addAnimation(8,15, 25, true);

    }


    /**
     * Подготовить анимацию с учетом направления входа и выхода
     * @param inputDirection направление входа
     * @param outputDirection направление выхода
     */
    public void adjustAnimation(int inputDirection, int outputDirection) {
        if (inputDirection== LEFT && outputDirection== RIGHT)
            adjustBaseSprite(animStraight, 0, false, false); else
        if (inputDirection== RIGHT && outputDirection== LEFT)
            adjustBaseSprite(animStraight, 0, true, false); else
        if (inputDirection== DOWN && outputDirection== UP)
            adjustBaseSprite(animStraight, (float) Math.PI / 2, false, false); else
        if (inputDirection== UP && outputDirection== DOWN)
            adjustBaseSprite(animStraight, (float) -Math.PI / 2, false, false); else
        if (inputDirection== UP && outputDirection== RIGHT)
            adjustBaseSprite(animUpToRight, 0, false, false); else
        if (inputDirection== RIGHT && outputDirection== UP)
            adjustBaseSprite(animRightToUp, 0, false, false); else
        if (inputDirection== LEFT && outputDirection== UP)
            adjustBaseSprite(animRightToUp, 0, true, false); else
        if (inputDirection== LEFT && outputDirection== DOWN)
            adjustBaseSprite(animRightToUp, 0, true, true); else
        if (inputDirection== DOWN && outputDirection== LEFT)
            adjustBaseSprite(animUpToRight, 0, true, true); else
        if (inputDirection== RIGHT && outputDirection== DOWN)
            adjustBaseSprite(animRightToUp, 0, false, true); else
        if (inputDirection== DOWN && outputDirection== RIGHT)
            adjustBaseSprite(animUpToRight, 0, false, true); else
        if (inputDirection== UP && outputDirection== LEFT)
            adjustBaseSprite(animUpToRight, 0, true, false);
    }


    public void adjustAnimationLeft(int inputDirection, int outputDirection) {
        if (inputDirection== UP && outputDirection== RIGHT)
            adjustLeftSprite(animL_U2R, false, false); else
        if (inputDirection== RIGHT && outputDirection== DOWN)
            adjustLeftSprite(animL_R2U,  false, true); else
        if (inputDirection== DOWN && outputDirection== LEFT)
            adjustLeftSprite(animL_U2R,  true, true); else
        if (inputDirection== LEFT && outputDirection== UP)
            adjustLeftSprite(animL_R2U,  true, false);
    }

    public void adjustAnimationRight(int inputDirection, int outputDirection) {
        if (inputDirection== RIGHT && outputDirection== UP)
            adjustRightSprite(animR_R2U,  false, false); else
        if (inputDirection== UP && outputDirection== LEFT)
            adjustRightSprite(animR_U2R,  true, false); else
        if (inputDirection== LEFT && outputDirection== DOWN)
            adjustRightSprite(animR_R2U,  true, true); else
        if (inputDirection== DOWN && outputDirection== RIGHT)
            adjustRightSprite(animR_U2R, false, true);
    }

    private void adjustBaseSprite(int activeAnimation, float rotation, boolean horizontalFlip, boolean verticalFlip) {
        sprite.setActiveAnimation(activeAnimation);
        sprite.setRotation(rotation);
        sprite.flipHorizontally(horizontalFlip);
        sprite.flipVertically(verticalFlip);
    }

    private void adjustLeftSprite(int activeAnimation, boolean horizontalFlip, boolean verticalFlip) {
        leftJointSprite.setActiveAnimation(activeAnimation);
        leftJointSprite.flipHorizontally(horizontalFlip);
        leftJointSprite.flipVertically(verticalFlip);
    }

    private void adjustRightSprite(int activeAnimation,  boolean horizontalFlip, boolean verticalFlip) {
        rightJointSprite.setActiveAnimation(activeAnimation);
        rightJointSprite.flipHorizontally(horizontalFlip);
        rightJointSprite.flipVertically(verticalFlip);
    }


    /**
     * Отрисовывает конвейер
     * @param camera камера
     * @param x левая координата клетки
     * @param y нижняя координата клетки
     * @param width ширина клетки
     * @param height высота клетки
     */
    public void draw(Camera camera, float x, float y, float width, float height) {

        // Проверить поставлена ли на паузу игра
        boolean gamePaused = false;
        Production production = block.getProduction();
        if (production!=null) gamePaused = production.isPaused();

        // Если конвейер занят или игра поставлена на паузу - остановить анимацию движения
        if (block.getState()==BUSY || block.getState()==FAULT || gamePaused) {
            sprite.animationPaused = true;
            leftJointSprite.animationPaused = true;
            rightJointSprite.animationPaused = true;
        } else {
            sprite.animationPaused = false;
            leftJointSprite.animationPaused = false;
            rightJointSprite.animationPaused = false;
        }
        // Нарисовать боковые конвейеры
        drawSideConveyors(camera, x, y, width, height);

        // Отрисовать сам конвейер
        sprite.draw(camera,x,y, width, height);

        // Отрисовать материалы
        drawItems(camera, x, y, width, height);

        // Отрисовать вход/выход если игра на паузе
        if (gamePaused) {
            drawInOut(camera, block.getInputDirection(), block.getOutputDirection(),
                    x, y, width, height, sprite.getZOrder() + 2);
        }

        // Рисуем значок сбоя, если произошел сбой
        if (block instanceof Conveyor && block.getState()== Conveyor.FAULT) {
            fault.draw(camera, x, y, width, height);
        }

    }

    /**
     * Отрисовываем боковые соединения конвейеров
     * @param camera камера
     * @param x левая координата клетки
     * @param y нижняя координата клетки
     * @param width ширина клетки
     * @param height высота клетки
     */
    private void drawSideConveyors(Camera camera, float x, float y, float width, float height) {
        if (!(block instanceof Conveyor)) return;
        if (!block.isStraight()) return;

        int leftDir = block.getLeftDirection();
        int rightDir = block.getRightDirection();
        Block leftBlock = block.getProduction().getBlockAt(block, leftDir);
        Block rightBlock = block.getProduction().getBlockAt(block, rightDir);
        if (leftBlock==null && rightBlock==null) return;

        Conveyor conveyor = (Conveyor) block;
        if (conveyor.isJoinedConveyor(leftBlock)) {
            if (leftBlock != lastLeftBlock) {
                adjustAnimationLeft(leftDir, block.getOutputDirection());
                lastLeftBlock = leftBlock;
            }
            leftJointSprite.draw(camera,x,y, width, height);
        }
        if (conveyor.isJoinedConveyor(rightBlock)) {
            if (rightBlock != lastRightBlock) {
                adjustAnimationRight(rightDir, block.getOutputDirection());
                lastRightBlock = rightBlock;
            }
            rightJointSprite.draw(camera,x,y, width, height);
        }
    }



    /**
     * Рассчитывает прогресс движения предметов и отрисовывает предметы на конвейре
     * @param camera камера
     * @param x левая координата клетки
     * @param y нижняя координата клетки
     * @param width ширина клетки
     * @param height высота клетки
     */
    protected void drawItems(Camera camera, float x, float y, float width, float height) {

        Channel<Item> inputQueue = block.getInputQueue();                      // Входящая очередь
        Channel<Item> outputQueue = block.getOutputQueue();                    // Исходящая очередь
        float cycleTime = this.block.getProduction().getCycleMilliseconds();   // Длительность цикла в мс.

        float deliveryCycles = 1;
        if (block instanceof Conveyor) {
            deliveryCycles = ((Conveyor) block).getDeliveryCycles();
        } else if (block instanceof Machine) {
            deliveryCycles = ((Machine) block).getOperation().getCycles() + 1;
        }

        float deliveryTime = deliveryCycles * cycleTime;                 // Время доставки в мс.
        float capacity = block.getTotalCapacity();                       // Вместимость конвейера в предметах
        float stridePerItem = 1.0f / (capacity / 2.0f);                  // Шаг для одного предмета
        float progress;

        long now = block.getProduction().getClock();
        float cycleBias = (now - block.getLastPollTime()) / cycleTime;
        if (cycleBias > 1.0f) cycleBias = 1.0f;

        float progressBias = cycleBias * stridePerItem;
        int finishedCounter = block.getOutputQueue().size();

        for (int i=0; i<outputQueue.size(); i++) {
            Item item = outputQueue.get(i);
            if (item==null) continue;
            progress = 1.0f - (i * stridePerItem) + progressBias - stridePerItem;
            if (block instanceof Machine) {
                if (progress < 0.5f) progress = 0.5f;
            }
            drawItem (camera, x, y, width, height, item, progress);
        }


        float maxProgress = 1.0f - (finishedCounter * stridePerItem);
        for (int i=0; i<inputQueue.size(); i++) {
            Item item = inputQueue.get(i);
            if (item == null) continue;
            // fixme BUG: если конвейер остановлен Bias время item продолжает идти (считать время остановки)
            now = block.getProduction().getClock();
            progress = (now - item.getTimeOwned()) /  deliveryTime;
            if (progress > maxProgress) {
                progress = maxProgress;
                maxProgress -= stridePerItem;
            }
            if (block instanceof Machine) {
              if (progress > 0.5f) progress = 0.5f;
              if (progress < 0.0f) progress = 0;
            }
            drawItem (camera, x, y, width, height, item, progress);
        }

    }


    /**
     * Отрисовывает предмет на основе координат клетки и прогресса движения
     * @param camera камера
     * @param x левая координата
     * @param y нижняя координата
     * @param width ширина клетки
     * @param height высота клетки
     * @param item предмет
     * @param progress прогресс движения 0.0-1.0
     */
    protected void drawItem(Camera camera, float x, float y, float width, float height, Item item, float progress) {

        int inputDirection = block.getInputDirection();
        int outputDirection = block.getOutputDirection();

        calculateCoordinates(progress, inputDirection, outputDirection, coordBuffer);
        float xpos = coordBuffer.x;
        float ypos = coordBuffer.y;
        float minx = x + xpos * width + width / 4;
        float miny = y + ypos * height + height / 4;

        Sprite materialSprite = item.getMaterial().getImage();
        materialSprite.setZOrder(sprite.getZOrder() + 1);
        materialSprite.draw(camera,minx,miny,width / 2, height / 2);

    }


    /**
     * Рассчитывает координаты с учетом направления движения и прогресса
     * @param progress прогресс движения материала по конвейеру 0.0-1.0
     * @param inpDir направление входа
     * @param outDir направление выхода
     * @param result расчитанные координаты
     */
    private void calculateCoordinates(float progress, int inpDir, int outDir, Vector result) {
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

    @Override
    public void setAnimationSpeed(float speed) {
        int activeAnimation = sprite.getActiveAnimation();
        Sprite.Animation animation = sprite.getAnimation(activeAnimation);
        if (animation!=null) animation.speed = speed;
    }

}
