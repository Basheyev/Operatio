package com.axiom.operatio.model.production.machine;

import android.content.res.Resources;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.block.BlockRenderer;
import com.axiom.operatio.model.production.conveyor.ConveyorRenderer;

import static com.axiom.operatio.model.production.ProductionRenderer.Z_ORDER_EXCLAMATION;
import static com.axiom.operatio.model.production.ProductionRenderer.Z_ORDER_MACHINES;

/**
 * Рендер машины
 */
public class MachineRenderer extends BlockRenderer {

    protected static Sprite allMachines = null;

    protected Machine machine;                             // Отрисовываемая машина
    protected Sprite sprite;                               // Анимации машины
    protected Sprite fault;                                // Значек сбоя
    protected int idleAnimation;                           // Код анимации простоя
    protected int busyAnimation;                           // Код анимации занятости
    protected ConveyorRenderer conveyorRenderer;

    public static final int NO_NEXT_ANIMATION = -1;
    protected int nextAnimation = NO_NEXT_ANIMATION;

    public MachineRenderer(Machine machine) {
        this.machine = machine;
        int ID = machine.getType().ID;
        if (allMachines==null) {
            Resources resources = SceneManager.getResources();
            allMachines = new Sprite(resources, R.drawable.blocks, 8, 16);
        }

        sprite = allMachines.getAsSprite(ID * 8, ID * 8 + 7);
        sprite.setZOrder(Z_ORDER_MACHINES);
        idleAnimation = sprite.addAnimation(0, 0, 8, true);
        busyAnimation = sprite.addAnimation(0, 7, 8, true);
        sprite.setActiveAnimation(idleAnimation);
        fault = allMachines.getAsSprite(71);
        fault.setZOrder(Z_ORDER_EXCLAMATION);

        conveyorRenderer = new ConveyorRenderer(machine);
    }


    public void draw(Camera camera, float x, float y, float width, float height) {
        Production production = machine.getProduction();

        // Ставим анимацию на паузу если производство на паузе
        if (production != null) {
            sprite.animationPaused = production.isPaused();
        } else return;

            // Рисуем значок сбоя, если произошел сбой
        if (machine.getState() == Machine.FAULT) {
            sprite.animationPaused = true;
            fault.draw(camera, x, y, width, height);
        }

        // Рисуем конвейер
        conveyorRenderer.draw(camera, x, y, width, height);

        // Рисуем машину
        sprite.draw(camera, x, y, width, height);
        if (sprite.isAnimationLastFrame() && nextAnimation != NO_NEXT_ANIMATION) {
            if (sprite.getActiveAnimation() != nextAnimation) sprite.setActiveAnimation(nextAnimation);
            nextAnimation = NO_NEXT_ANIMATION;
        }

        // Рисуем вход-выход
        if (production.isPaused()) {
            drawInOut(camera, machine.getInputDirection(), machine.getOutputDirection(),
                    x, y, width, height, sprite.getZOrder() + 2);
        }
    }

    public void arrangeAnimation(int inputDirection, int outputDirection) {
        conveyorRenderer.adjustAnimation(inputDirection, outputDirection);
    }

    public void setIdleAnimation() {
        nextAnimation = idleAnimation;
    }

    public void setBusyAnimation() {
        nextAnimation = busyAnimation;
    }

    @Override
    public void setAnimationSpeed(float speed) {
        int activeAnimation = sprite.getActiveAnimation();
        Sprite.Animation animation = sprite.getAnimation(activeAnimation);
        if (animation!=null) animation.speed = speed;
    }

}
