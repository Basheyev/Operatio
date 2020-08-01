package com.axiom.operatio.model.production.machine;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameObject;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.operatio.model.production.blocks.Block;
import com.axiom.operatio.model.production.ProductionModel;
import com.axiom.operatio.model.production.materials.Item;

/**
 * Машина преобразует входящие потоки материалов в выходящие потоки материалов.
 * Реализует такие машины как: обработка материалов, сборка/смешивание материалов, расщепление
 * TODO стыковки конвейер-машина
 * TODO реализовать операцию сборки и расщепление
 */
public class Machine extends Block {

    protected Operation operation;            // Выполняемая операция
    protected int idleAnim;
    protected int busyAnim;
    protected Sprite sprite;

    /**
     * Конструктор машины выполняющей операцию за указанное количество циклов
     * @param op выполняемая операция
     * @param millisec время работы машины в количестве циклов
     * @param flowIn вуфер входящих материалов
     * @param flowOut буфер исходящих материалов
     */
    public Machine(GameScene scene, ProductionModel p, Operation op, int flowIn, int flowOut, int millisec, float scale) {
        super(scene,p,1,flowIn,flowOut);
        operation = op;
        processingTime = millisec;
        this.scale = scale;
        sprite = new Sprite(scene.getResources(), R.drawable.machine00,4,4);
        sprite.zOrder = 3;
        float w = sprite.getWidth() * scale;
        float h = sprite.getHeight() * scale;
        setLocalBounds(-w/2,-h/2,w/2,h/2);

        busyAnim = sprite.addAnimation(0,7,15,true);
        idleAnim = sprite.addAnimation(0,0,1,true);

        setState(STATE_IDLE);
    }


    public boolean push(Item item) {
        if (getState()==STATE_IDLE) {
            if (items.size() >= capacity) {
                setState(STATE_FAULT);
                return false;
            }
            if (item == null) return false;
            item.owner = this;
            item.processingStart = System.currentTimeMillis();
            items.add(item);
            setState(STATE_BUSY);
            return true;
        }
        return false;
    }


    public Item peek() {
        long now = System.currentTimeMillis();     // Текущее время в миллисекундах
        for (Item item:items) {
            if (item.owner==this && now > (item.processingStart + processingTime)) {
                return item;
            }
        }
        return null;
    }


    public Item poll() {

        long now = System.currentTimeMillis();
        for (Item item:items) {
            if (now > (item.processingStart + processingTime) || item.owner!=this) {
                item.material = operation.output;              // Превращаем материал во выходной
                items.remove(item);
                setState(STATE_IDLE);
                return item;
            }
        }
        return null;
    }



    /**
     * Выполняет операцию машины
     * @return
     */
    public boolean doWork() {
        Block input, output;
        Item item;
        if (state==STATE_IDLE) {
            input = productionModel.getBlockAt(this, inputDirection);
            if (input==null) return false;
            item = input.peek();
            if (item==null) return false;
            if (this.push(item)) input.poll();      // забрать из входного блока материал
        } else if (state==STATE_BUSY) {           // Если состояние машины "работаем"
            output = productionModel.getBlockAt(this, outputDirection);
            if (output==null) return false;
            item = peek();
            if (item==null) return false;
            if (output.push(item)) this.poll(); else return false;
        } else if (state==STATE_FAULT) {
            setState(STATE_IDLE);                  // Если был сбой, возвращаемся в ожидание
        }
        return true;
    }


    //--------------------------------------------------------------------------------------------
    public boolean setState(int newState) {
        if (state==newState) return true;
        switch (newState) {
            case STATE_IDLE:
                state = newState;
                sprite.setActiveAnimation(idleAnim);
                break;
            case STATE_BUSY:
                state = newState;
                sprite.setActiveAnimation(busyAnim);
                break;
            case STATE_FAULT:
                state = newState;
                sprite.setActiveAnimation(idleAnim);
                break;
            default:
                return false;
        }
        return true;
    }

    public int getState() {
        return state;
    }

    //-----------------------------------------------------------------------------------------


    @Override
    public void update(float deltaTime) {

    }

    @Override
    public void draw(Camera camera) {
        super.draw(camera);
        sprite.draw(camera,x,y,scale);
        String bf1 = ""+ items.size();
        GraphicsRender.setZOrder(10);
        GraphicsRender.drawText(bf1.toCharArray(), x ,y ,scale / 3);
    }

    @Override
    public void onCollision(GameObject object) {

    }
}
