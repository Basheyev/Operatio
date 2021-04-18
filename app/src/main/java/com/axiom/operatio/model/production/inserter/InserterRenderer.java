package com.axiom.operatio.model.production.inserter;

import android.content.res.Resources;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.data.Channel;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.operatio.model.materials.Item;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.block.BlockRenderer;

import static com.axiom.operatio.model.production.block.Block.DOWN;
import static com.axiom.operatio.model.production.block.Block.LEFT;
import static com.axiom.operatio.model.production.block.Block.RIGHT;
import static com.axiom.operatio.model.production.block.Block.UP;

public class InserterRenderer extends BlockRenderer {

    private static Sprite allBlocks;
    private Inserter inserter;

    private Sprite baseSprite;
    private Sprite handSprite;
    private float rotation = 0;

    public InserterRenderer(Inserter inserter) {
        this.inserter = inserter;

        if (allBlocks==null) {
            Resources resources = SceneManager.getResources();
            allBlocks = new Sprite(resources, R.drawable.blocks, 8, 16);
        }
        baseSprite = allBlocks.getAsSprite(96);
        baseSprite.setZOrder(7);
        handSprite = allBlocks.getAsSprite(97);
        handSprite.setRotationPoint(-0.45f, 0);
        handSprite.setZOrder(8);
    }

    @Override
    public void draw(Camera camera, float x, float y, float width, float height) {
        Channel<Item> inputQueue = inserter.getInputQueue();     // Входящая очередь
        Production production = inserter.getProduction();

        // Отрисовываем основу манипулятора
        baseSprite.draw(camera, x ,y, width, height);

        // Рассчитываем прогресс движения
        long now = production.getClock();
        float progress = 0;
        Item item = inputQueue.get(0);

        if (item != null) {
            float cycleTime = production.getCycleMilliseconds();          // Длительность цикла в мс.
            float deliveryTime = Inserter.DELIVERY_CYCLES * cycleTime;    // Время доставки в мс.
            progress = (now - item.getTimeOwned()) / deliveryTime;          // Прогресс движения
            if (progress > 1) progress = 1;
        }

        drawHand(camera, x,y,width, height, progress);

        // Рисуем вход-выход
        if (production.isPaused()) {
            drawInOut(camera, inserter.getInputDirection(), inserter.getOutputDirection(),
                    x, y, width, height, baseSprite.getZOrder() + 2);
        }
    }


    private void drawHand(Camera camera, float x, float y, float width, float height, float progress) {

        float startAngle = directionToRadians(inserter.getInputDirection());
        float stopAngle = directionToRadians(inserter.getOutputDirection());
        float rotation = (stopAngle - startAngle) * progress + startAngle;

        handSprite.setRotation(rotation);
        handSprite.draw(camera, x, y , width, height);
        drawItem(camera, x, y , width, height);
    }


    private float directionToRadians(int direction) {
        switch (direction) {
            case RIGHT: return 0;
            case UP: return (float) Math.PI / 2;
            case LEFT: return (float) Math.PI;
            case DOWN: return (float) (Math.PI + Math.PI / 2);
        }
        return 0;
    }


    private void drawItem(Camera camera, float x, float y, float width, float height) {
        Channel<Item> inputQueue = inserter.getInputQueue();
        Item item = inputQueue.get(0);
        if (item==null) return;

        float ax = (float) (Math.cos(handSprite.getRotation()) * width) + x + width / 2;
        float ay = (float) (Math.sin(handSprite.getRotation()) * height) + y + height / 2;
        Sprite itemSprite = item.getMaterial().getImage();
        itemSprite.setZOrder(9);
        itemSprite.draw(camera, ax - width / 4, ay - height / 4, width / 2, height / 2);
    }


    @Override
    public void setAnimationSpeed(float speed) {

    }

}
