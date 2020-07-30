package com.axiom.atom.engine.core.geometry;

/**
 * Axis Aligned Bound Box
 * Хранит данные о AABB: координаты, центр, размеры
 * Выичсляет пересечение с другим AABB или точкой
 *
 * (C) Atom Engine, Bolat Basheyev 2020
 */
public class AABB {

    public Vector min;
    public Vector max;
    public Vector center;
    public float width;
    public float height;

    public AABB(float minX, float minY, float maxX, float maxY) {
        min = new Vector(minX, minY);
        max = new Vector(maxX, maxY);
        width = max.x - min.x;
        height = max.y - min.y;
        center = new Vector(min.x + width * 0.5f, min.y + height * 0.5f);
    }

    public void setBounds(float minX, float minY, float maxX, float maxY) {
        min.x = minX;
        min.y = minY;
        max.x = maxX;
        max.y = maxY;
        width = max.x - min.x;
        height = max.y - min.y;
        center.x = min.x + width * 0.5f;
        center.y = min.y + height * 0.5f;
    }

    /**
     * Проверка пересечения AABB vs AABB
     * @param box другой AABB
     * @return true - если пересекаются или false - если нет
     */
    public boolean collides(AABB box) {
        if (max.y < box.min.y || min.y > box.max.y) return false;
        if (max.x < box.min.x || min.x > box.max.x) return false;
        return true;
    }

    /**
     * Проверка пересечения AABB и точки
     * @param x координата
     * @param y координата
     * @return true - если пересекаются или false - если нет
     */
    public boolean collides(float x, float y) {
        if (x < min.x || x > max.x) return false;
        if (y < min.y || y > max.y) return false;
        return true;
    }

}
