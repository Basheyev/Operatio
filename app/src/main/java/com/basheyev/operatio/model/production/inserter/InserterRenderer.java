package com.basheyev.operatio.model.production.inserter;

import android.content.res.Resources;

import com.basheyev.atom.R;
import com.basheyev.atom.engine.core.SceneManager;
import com.basheyev.atom.engine.data.structures.Channel;
import com.basheyev.atom.engine.graphics.gles2d.Camera;
import com.basheyev.atom.engine.graphics.renderers.Sprite;
import com.basheyev.operatio.model.materials.Item;
import com.basheyev.operatio.model.production.Production;
import com.basheyev.operatio.model.production.block.BlockRenderer;

import static com.basheyev.operatio.model.production.ProductionRenderer.Z_ORDER_MACHINES;
import static com.basheyev.operatio.model.production.block.Block.DOWN;
import static com.basheyev.operatio.model.production.block.Block.LEFT;
import static com.basheyev.operatio.model.production.block.Block.RIGHT;
import static com.basheyev.operatio.model.production.block.Block.UP;

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
        baseSprite.setZOrder(Z_ORDER_MACHINES);
        handSprite = allBlocks.getAsSprite(97);
        handSprite.setRotationPoint(-0.45f, 0);
        handSprite.setZOrder(Z_ORDER_MACHINES + 1);
    }

    @Override
    public void draw(Camera camera, float x, float y, float width, float height) {
        Channel<Item> inputQueue = inserter.getInputQueue();     // Входящая очередь
        Production production = inserter.getProduction();

        // Рисуем вход-выход
        if (production.isPaused()) {
            drawInOut(camera, inserter.getInputDirection(), inserter.getOutputDirection(),
                    x, y, width, height, baseSprite.getZOrder());
        }

        // Отрисовываем основу манипулятора
        baseSprite.draw(camera, x ,y, width, height);

        // Рассчитываем прогресс движения
        long now = production.getClock();
        float progress = 0;

        // fixme maybe its better to use poll method?
        Item item = inputQueue.get(0);

        if (item != null) {
            float cycleTime = production.getCycleMilliseconds();          // Длительность цикла в мс.
            float deliveryTime = inserter.getDeliveryCycles() * cycleTime;    // Время доставки в мс.
            progress = (now - item.getTimeOwned()) / deliveryTime;          // Прогресс движения
            if (progress > 1) progress = 1;
        }

        drawHand(camera, x,y,width, height, progress);


    }


    private void drawHand(Camera camera, float x, float y, float width, float height, float progress) {

        int inpDir = inserter.getInputDirection();
        int outDir = inserter.getOutputDirection();
        float startAngle = directionToRadians(inpDir);
        float stopAngle = directionToRadians(outDir);
        float rotation;

        if (inpDir==DOWN && outDir==RIGHT) stopAngle = (float) Math.PI * 2; else
        if (inpDir==RIGHT && outDir==DOWN) stopAngle = (float) -(Math.PI / 2);

        rotation = (stopAngle - startAngle) * progress + startAngle;

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
        // fixme maybe its better to use poll method?
        Item item = inputQueue.get(0);
        if (item==null) return;
        float halfWidth = width * 0.5f;
        float halfHeight = height * 0.5f;
        float ax = (float) (Math.cos(handSprite.getRotation()) * width) + x + halfWidth;
        float ay = (float) (Math.sin(handSprite.getRotation()) * height) + y + halfHeight;
        Sprite itemSprite = item.getMaterial().getImage();
        itemSprite.setZOrder(handSprite.getZOrder() + 1);
        itemSprite.draw(camera, ax - halfWidth * 0.5f, ay - halfHeight * 0.5f, halfWidth, halfHeight);
    }


    @Override
    public void setAnimationSpeed(float speed) {

    }

}
