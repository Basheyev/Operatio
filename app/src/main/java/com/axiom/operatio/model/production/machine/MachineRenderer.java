package com.axiom.operatio.model.production.machine;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.block.BlockRenderer;
import com.axiom.operatio.model.production.conveyor.ConveyorRenderer;

public class MachineRenderer extends BlockRenderer {

    protected Machine machine;                             // Отрисовываемая машина
    protected Sprite sprite;                               // Анимации машины
    protected int idleAnimation;                           // Код анимации простоя
    protected int busyAnimation;                           // Код анимации занятости
    protected ConveyorRenderer conveyorRenderer;

    public MachineRenderer(Machine machine) {
        this.machine = machine;
        int ID = machine.getType().ID;
        sprite = new Sprite(SceneManager.getResources(), R.drawable.blocks, 8, 11);
        sprite.zOrder = 2;
        idleAnimation = sprite.addAnimation(ID * 8, ID * 8, 8, true);
        busyAnimation = sprite.addAnimation(ID * 8, ID * 8 + 7, 8, true);
        sprite.setActiveAnimation(idleAnimation);
        conveyorRenderer = new ConveyorRenderer(machine);
    }


    public void draw(Camera camera, float x, float y, float width, float height) {
        Production production = machine.getProduction();
        if (production!=null) {
            sprite.animationPaused = production.isPaused();
        }

        // Рисуем конвейер
        conveyorRenderer.draw(camera, x, y, width, height);

        // Рисуем машину
        sprite.draw(camera, x, y, width, height);

        // Рисуем вход-выход
        drawInOut(camera, machine.getInputDirection(), machine.getOutputDirection(),
                x, y, width, height, sprite.zOrder + 2);

    }

    public void arrangeAnimation(int inputDirection, int outputDirection) {
        conveyorRenderer.arrangeAnimation(inputDirection, outputDirection);
    }

    public void setIdleAnimation() {
        if (sprite.getTimesPlayed()>0)
        sprite.setActiveAnimation(idleAnimation);
    }

    public void setBusyAnimation() {
        sprite.setActiveAnimation(busyAnimation);
    }

}
