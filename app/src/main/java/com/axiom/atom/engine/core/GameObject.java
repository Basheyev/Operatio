package com.axiom.atom.engine.core;

import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.core.geometry.Vector;
import com.axiom.atom.engine.physics.PhysicsRender;


/**
 * Игровой объект - базовая сущность игровой сцены<br>
 * Графчиеское представление объекта отрисовывается графическим рендером<br>
 * Физическое представление объекта обрабатываемтся физическим рендером<br>
 * <br>
 * (C) Atom Engine, Bolat Basheyev 2020
 */
public abstract class GameObject {

    //-------------------------------------------------------------------------------
    // Графические параметры объекта
    //-------------------------------------------------------------------------------
    protected GameScene scene = null;         // Сцена которой принадлежит объект
    public Sprite sprite = null;              // Спрайт игрового объекта
    public float scale = 1.0f;                // Масштаб спрайта игрового объекта
    public boolean active = true;             // Активен ли объект (нужна ли обработка)
    public float x, y;                        // координаты объекта в мировых координатах
    public float oldX, oldY;                  // предыдущие мировые координаты объекта
    //------------------------------------------------------------------------------
    // Физические параметры объекта
    //------------------------------------------------------------------------------
    public int bodyType = PhysicsRender.BODY_VOID;    // VOID, STATIC, KINEMATIC, DYNAMIC
    public Vector velocity;                   // Вектор скорости движения объекта
    public float mass=1;                      // Масса объекта
    public float restitution=0.5f;            // Упругость объекта
    protected AABB localBounds;               // Границы в локальных координатах объекта
    protected AABB worldBounds;               // Границы в глобальных координатах объекта
    public boolean grounded = false;          // стоит ли на статическом объекте

    //------------------------------------------------------------------------------
    public GameObject(GameScene gameScene) {
        scene = gameScene;
        velocity = new Vector();
        localBounds = new AABB(0,0,0,0);
        worldBounds = new AABB(0,0,0,0);
    }

    //-----------------------------------------------------------------------------
    // Обработчики событий объекта
    //-----------------------------------------------------------------------------
    public abstract void update(float deltaTime);
    public abstract void draw(Camera camera);
    public abstract void onCollision(GameObject object);

    //------------------------------------------------------------------------------
    private float lastX, lastY;
    private boolean localBoundsChanged = false;
    /**
     * Возвращает AABB в мировых координатах
     * @return
     */
    public AABB getWorldBounds() {
        if (x!=lastX || y!=lastY || localBoundsChanged) {
            worldBounds.setBounds(
                    x + localBounds.min.x,
                    y + localBounds.min.y,
                    x + localBounds.max.x,
                    y + localBounds.max.y
            );
            lastX = x;
            lastY = y;
            localBoundsChanged = false;
        }
        return worldBounds;
    }

    /**
     * Возвращает AABB в локальных координатах
     * @return
     */
    public AABB getLocalBounds() {
        return localBounds;
    }

    /**
     * Устанавливает границы в локальных координатах объекта
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     */
    public void setLocalBounds(float minX, float minY, float maxX, float maxY) {
        localBounds.setBounds(minX,minY,maxX,maxY);
        localBoundsChanged = true;
    }

    /**
     * Устанавливает границы в локальных координатах объекта
     * @param bounds
     */
    public void setLocalBounds(AABB bounds) {
        localBounds.setBounds(bounds.min.x,bounds.min.y,bounds.max.x,bounds.max.y);
    }

}
