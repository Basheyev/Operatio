package com.axiom.operatio.model.production.conveyor;

import com.axiom.atom.engine.core.geometry.Vector;
import com.axiom.atom.engine.data.Channel;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.operatio.model.materials.Item;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.model.production.machine.Machine;

import static com.axiom.operatio.model.production.block.Block.DOWN;
import static com.axiom.operatio.model.production.block.Block.LEFT;
import static com.axiom.operatio.model.production.block.Block.RIGHT;
import static com.axiom.operatio.model.production.block.Block.UP;

/**
 * Отрисовывает предметы на конвейере
 */
public class ItemsRenderer {

    private Vector coordBuffer = new Vector();
    private Block block;
    private Sprite sprite;

    public ItemsRenderer(Block block, Sprite baseSprite) {
        this.block = block;
        this.sprite = baseSprite;
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
}
