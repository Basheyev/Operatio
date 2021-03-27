package com.axiom.atom.engine.core.geometry;

/**
 * Axis Aligned Bound Box
 * Хранит данные о AABB: координаты, центр, размеры
 * Выичсляет пересечение с другим AABB или точкой
 *
 * (C) Atom Engine, Bolat Basheyev 2020
 */
public class AABB {

    public float minX, minY;
    public float maxX, maxY;
    public float centerX, centerY;
    public float width;
    public float height;

    public AABB(float minX, float minY, float maxX, float maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        width = maxX - minX;
        height = maxY - minY;
        centerX = minX + width * 0.5f;
        centerY = minY + height * 0.5f;
    }

    public void setBounds(float minX, float minY, float maxX, float maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        width = maxX - minX;
        height = maxY - minY;
        centerX = minX + width * 0.5f;
        centerY = minY + height * 0.5f;
    }


    public void copy(AABB source) {
        this.minX = source.minX;
        this.minY = source.minY;
        this.maxX = source.maxX;
        this.maxY = source.maxY;
        this.centerX = source.centerX;
        this.centerY = source.centerY;
        this.width = source.width;
        this.height = source.height;
    }

    /**
     * Проверка пересечения AABB vs AABB
     * @param box другой AABB
     * @return true - если пересекаются или false - если нет
     */
    public boolean collides(AABB box) {
        if (maxY < box.minY || minY > box.maxY) return false;
        if (maxX < box.minX || minX > box.maxX) return false;
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
        if (this.maxY < minY || this.minY > maxY) return false;
        if (this.maxX < minX || this.minX > maxX) return false;
        return true;
    }


    /**
     * Проверка пересечения AABB и точки
     * @param x координата
     * @param y координата
     * @return true - если пересекаются или false - если нет
     */
    public boolean collides(float x, float y) {
        if (x < minX || x > maxX) return false;
        if (y < minY || y > maxY) return false;
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
        float x1 = Math.max(minX, box.minX);
        float y1 = Math.max(minY, box.minY);
        float x2 = Math.min(maxX, box.maxX);
        float y2 = Math.min(maxY, box.maxY);
        result.setBounds(x1, y1, x2, y2);
        return result;
    }


}
