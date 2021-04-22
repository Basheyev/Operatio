package com.axiom.operatio.model.production.conveyor;

import com.axiom.atom.engine.core.geometry.Vector;
import com.axiom.atom.engine.data.Channel;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.operatio.model.materials.Item;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.model.production.machine.Machine;

import javax.crypto.Mac;

import static com.axiom.operatio.model.production.block.Block.DOWN;
import static com.axiom.operatio.model.production.block.Block.LEFT;
import static com.axiom.operatio.model.production.block.Block.NONE;
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

        Channel<Item> inputQueue = block.getInputQueue();      // Входящая очередь блока
        Channel<Item> outputQueue = block.getOutputQueue();    // Исходящая очередь блока

        float deliveryTime = getItemDeliveryTime(block);       // Время доставки в миллисекундах
        float capacity = block.getTotalCapacity();             // Вместимость конвейера в предметах
        float stridePerItem = 1.0f / (capacity / 2.0f);        // Нормализованный шаг для одного предмета
        float strideTime = deliveryTime / capacity * 2.0f;     // Время одного шага движения предмета
        long now = block.getProduction().getClock();           // Текущие время производства
        long lastPollDelta = now - block.getLastPollTime();    // Время с последней отдачи предмета
        float normalDelta = 1.0f - lastPollDelta / strideTime; // Нормализованное время
        if (normalDelta < 0) normalDelta = 0;                  // с последней отдачи предмета
        float strideBias = normalDelta * stridePerItem;        // Смещение прогресса движения

        // Если есть предмет в выходящей очереди - отрисовываем
        // предмет в конце конвейера с учетом смещения
        int deliveredCounter = block.getOutputQueue().size();
        if (deliveredCounter > 0) {
            Item outputItem = outputQueue.peek();
            drawItem(camera, x, y, width, height, outputItem, 1.0f - strideBias);
        }

        // Отрисовываем предметы входящей очереди
        float maxProgress = (1.0f - strideBias) - deliveredCounter * stridePerItem;

        if (block instanceof Machine) maxProgress = 0.5f;

        for (int i=0; i<inputQueue.size(); i++) {
            Item item = inputQueue.get(i);
            float progress = (now - item.getTimeOwned()) / deliveryTime;
            if (progress > maxProgress) {
                progress = maxProgress;
                maxProgress -= stridePerItem;
            }
            if (progress < 0) progress = 0;
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
        if (item==null) return;
        int sourceDirection = item.getSourceDirection();
        if (sourceDirection == NONE) sourceDirection = block.getInputDirection();

        calculateCoordinates(progress, sourceDirection, block.getOutputDirection(), coordBuffer);

        float minx = x + coordBuffer.x * width + width * 0.25f;
        float miny = y + coordBuffer.y * height + height * 0.25f;

        Sprite materialSprite = item.getMaterial().getImage();
        materialSprite.setZOrder(sprite.getZOrder() + 1);
        materialSprite.draw(camera, minx, miny,width * 0.5f, height * 0.5f);

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
        // Прямые направления
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
        // Повороты на 90 градусов
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
        // Обратные повороты на 90 градусов
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


    /**
     * Возвращает время обработки предмета
     * @param block блок
     * @return время обработки предмета в миллисекундах
     */
    private float getItemDeliveryTime(Block block) {
        float cycles = 1;
        float cycleTime = this.block.getProduction().getCycleMilliseconds();
        if (block instanceof Conveyor)  cycles = ((Conveyor) block).getDeliveryCycles();
        else if (block instanceof Machine) cycles = ((Machine) block).getOperation().getCycles() + 1;
        return cycles * cycleTime;
    }

}
