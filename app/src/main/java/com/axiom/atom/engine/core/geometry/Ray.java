package com.axiom.atom.engine.core.geometry;

import static com.axiom.atom.engine.physics.PhysicsRender.*;

/**
 * Хранит данные о луче. Выичсляет пересечение луча с другим AABB
 *
 * (C) Atom Engine, Bolat Basheyev 2020
 */
public class Ray {

    public Vector origin = new Vector();
    public Vector direction = new Vector();

    protected Vector dirfrac = new Vector();
    public float rayLength;


    public Ray(float px, float py, float dx, float dy) {
        setRay(px,py,dx,dy);
    }

    public Ray() {
        setRay(0,0,0,0);
    }

    public void setRay(float px, float py, float dx, float dy) {
        origin.setValue(px, py);
        direction.setValue(dx, dy);
    }


    /**
     * Проверияет столкновение луча с AABB
     * @param aabb коробка с которым нужно проверить столкновение луча
     * @return вектор нормали плоскости столкновения луча с объектов или NV_ZERO
     */
    public Vector intersects(AABB aabb) {
        // r.dir is unit direction vector of ray
        dirfrac.x = 1.0f / direction.x;
        dirfrac.y = 1.0f / direction.y;

        // min is the corner of AABB with minimal coordinates - left bottom, max is maximal corner
        // origin is origin of ray
        float t1 = (aabb.min.x - origin.x) * dirfrac.x;
        float t2 = (aabb.max.x - origin.x) * dirfrac.x;
        float t3 = (aabb.min.y - origin.y) * dirfrac.y;
        float t4 = (aabb.max.y - origin.y) * dirfrac.y;

        float tmin = Math.max(Math.min(t1, t2), Math.min(t3, t4));
        float tmax = Math.min(Math.max(t1, t2), Math.max(t3, t4));

        // if tmax < 0, ray (line) is intersecting AABB, but the whole AABB is behind us
        if (tmax < 0) {
            rayLength = tmax;
            return NV_ZERO;
        }

        // if tmin > tmax, ray doesn't intersect AABB
        if (tmin > tmax) {
            rayLength = tmax;
            return NV_ZERO;
        }

        rayLength = tmin; // Длина луча до пересечения может потребоваться в будущем
        if (rayLength == t1) return NV_LEFT;
        if (rayLength == t2) return NV_RIGHT;
        if (rayLength == t3) return NV_TOP;
        if (rayLength == t4) return NV_BOTTOM;
        return NV_ZERO;
    }

}
