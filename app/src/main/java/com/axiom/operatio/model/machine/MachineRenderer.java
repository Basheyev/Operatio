package com.axiom.operatio.model.machine;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.operatio.model.Production;
import com.axiom.operatio.model.block.BlockRenderer;

public class MachineRenderer extends BlockRenderer {

    protected Machine machine;                             // Отрисовываемая машина
    protected Sprite sprite;                               // Анимации машины
    protected int idleAnimation;                           // Код анимации простоя
    protected int busyAnimation;                           // Код анимации занятости


    public MachineRenderer(Machine machine) {
        this.machine = machine;
        int ID = machine.getType().ID;
        sprite = new Sprite(SceneManager.getResources(), R.drawable.machines, 8, 8);
        sprite.zOrder = 2;
        idleAnimation = sprite.addAnimation(ID * 8, ID * 8, 8, true);
        busyAnimation = sprite.addAnimation(ID * 8, ID * 8 + 7, 8, true);
        sprite.setActiveAnimation(idleAnimation);
    }


    public void draw(Camera camera, float x, float y, float width, float height) {
        Production production = machine.getProduction();
        if (production!=null) {
            sprite.animationPaused = production.isPaused();
        }

        sprite.draw(camera, x, y, width, height);

        drawInOut(camera, machine.getInputDirection(), machine.getOutputDirection(),
                x, y, width, height, sprite.zOrder + 2);

        /*GraphicsRender.setZOrder(10);
        String bf1 = ""+ machine.getItemsAmount();
        GraphicsRender.drawText(bf1.toCharArray(), x + width / 2 ,y + height / 2,1);*/
    }

    public void setIdleAnimation() {
        sprite.setActiveAnimation(idleAnimation);
    }

    public void setBusyAnimation() {
        sprite.setActiveAnimation(busyAnimation);
    }

}
