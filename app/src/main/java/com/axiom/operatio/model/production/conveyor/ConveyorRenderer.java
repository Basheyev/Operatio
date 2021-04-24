package com.axiom.operatio.model.production.conveyor;

import android.content.res.Resources;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.model.production.block.BlockRenderer;

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
    protected Sprite leftJointSprite, rightJointSprite;          // Спрайты боковых соединений
    protected Sprite fault;                                      // Значек сбоя
    protected ItemsRenderer itemsRenderer;
    protected int animStraight, animUpToRight, animRightToUp;
    protected int animL_U2R, animL_R2U;
    protected int animR_U2R, animR_R2U;
    protected Block lastLeftBlock, lastRightBlock;


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

        itemsRenderer = new ItemsRenderer(block, sprite);
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
        itemsRenderer.drawItems(camera, x, y, width, height);

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




    @Override
    public void setAnimationSpeed(float speed) {
        int activeAnimation = sprite.getActiveAnimation();
        Sprite.Animation animation = sprite.getAnimation(activeAnimation);
        if (animation!=null) animation.speed = speed;
    }

}
