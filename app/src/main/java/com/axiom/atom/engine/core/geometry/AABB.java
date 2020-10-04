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


    public void copy(AABB source) {
        this.min.x = source.min.x;
        this.min.y = source.min.y;
        this.max.x = source.max.x;
        this.max.y = source.max.y;
        this.center.x = source.center.x;
        this.center.y = source.center.y;
        this.width = source.width;
        this.height = source.height;
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
     * Определяет пересекает AABB и прямоугольника указанного в координатах
     * @param minX левый X
     * @param minY нижний Y
     * @param maxX правый X
     * @param maxY верхний Y
     * @return true - если пересекаются, false - если нет
     */
    public boolean collides(float minX, float minY, float maxX, float maxY) {
        if (max.y < minY || min.y > maxY) return false;
        if (max.x < minX || min.x > maxX) return false;
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


    /**
     * Находит область пересечения этого AABB c box и записывает область в Result
     * @param box AABB с которым надо найти пересечение
     * @param result куда записать область пересечения
     * @return если пересекаются возвращает result, если не пересекаются возвращает null
     */
    public AABB findIntersection(AABB box, AABB result) {
        if (!collides(box)) return null;
        float minX = Math.max(min.x, box.min.x);
        float minY = Math.max(min.y, box.min.y);
        float maxX = Math.min(max.x, box.max.x);
        float maxY = Math.min(max.y, box.max.y);
        result.setBounds(minX, minY, maxX, maxY);
        return result;
    }


}
