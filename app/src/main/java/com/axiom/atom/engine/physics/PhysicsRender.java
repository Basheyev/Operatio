package com.axiom.atom.engine.physics;

import com.axiom.atom.engine.core.GameObject;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.core.geometry.Vector;

import java.util.ArrayList;

// вычислительная геометрия http://noonat.github.io/intersect/
// Статья Рэнди Гол https://www.randygaul.net/2013/03/27/game-physics-engine-part-1-impulse-resolution/

/**
 * Физический движок поддерживающий взаимодействие статических и динамических AABB объектов.
 * (C) Atom Engine, Bolat Basheyev 2020
 */
public class PhysicsRender {

    // Forces
    public static Vector gravity = new Vector(0,-150f); // acceleration  per second
    private static Vector gravityDelta = new Vector();
    public static float friction = 0.9f;
    public static float epsilon = 1f;
    public static float correctionPercent = 0.2f;   // usually 20% to 80%
    public static float correctionSlop = 0.01f;     // usually 0.01 to 0.1

    public static final int BODY_VOID      = 0;   // Пустота не обрабатываемтся движком физики
    public static final int BODY_STATIC    = 1;   // Объект статический (неподвижный)
    public static final int BODY_KINEMATIC = 2;   // Объект движущийся (может двигаться)
    public static final int BODY_DYNAMIC   = 3;   // Объект учитывающий импульсы столкновений

    public static final Vector NV_ZERO = new Vector(0,0);
    public static final Vector NV_LEFT = new Vector(-1,0);
    public static final Vector NV_RIGHT = new Vector(1,0);
    public static final Vector NV_TOP = new Vector(0,1);
    public static final Vector NV_BOTTOM = new Vector(0,-1);


    // Экономим время и один раз создаем эти локальные переменные
    private static float penetration;
    private static Vector distance = new Vector();
    private static Vector rv = new Vector();
    private static Vector impulse = new Vector();
    private static Vector impulseA = new Vector();
    private static Vector impulseB = new Vector();
    private static Vector correction = new Vector();

    public static void doPhysics(GameScene scene, float deltaTime) {
        ArrayList<GameObject> objects = scene.getSceneObjects();
        if (objects==null) return;

        GameObject a, b;
        Vector normal;

        // Для всех объектов сцены
        for (int i=0; i<objects.size(); i++) {
            a = objects.get(i);
            if (!a.active) continue;            // если объект не активный идём дальше
            a.oldX = a.x;                       // сохраняем предыдущую позицию X
            a.oldY = a.y;                       // сохраняем предыдущую позицию Y
            normal = NV_ZERO;                   // пока ни с чем не столкнулись
            if (a.bodyType>=BODY_KINEMATIC) {   // если объект динамический
                //------------------------------------------------------------------------
                if (a.bodyType==BODY_DYNAMIC && !a.grounded) {
                    gravityDelta.copy(gravity);
                    gravityDelta.mul(deltaTime);    // Учитваем время между кадрами
                    a.velocity.add(gravityDelta);   // добавляем гравитацию
                    a.velocity.mul(friction);       // Умножаем на трение по обеим осям
                }
                //------------------------------------------------------------------------
                // проверяем столкновения с другими объектами сцены
                for (int j=0; j<objects.size(); j++) {
                    if (i != j) {
                        // Берём второй объект
                        b = objects.get(j);
                        // Если второй объект не активен или пустой, то идём дальше
                        if (!b.active ||  b.bodyType == PhysicsRender.BODY_VOID) continue;

                        // если объекты столкнулись
                        if (a.getWorldBounds().collides(b.getWorldBounds())) {
                            if (a.bodyType==BODY_DYNAMIC && b.bodyType==BODY_DYNAMIC) {
                                normal = resolveDynamicCollision(a, b);
                                a.onCollision(b);
                                b.onCollision(a);
                            } else if (a.bodyType==BODY_DYNAMIC && b.bodyType==BODY_STATIC) {
                                normal = resolveStaticCollision(a, b);
                                a.onCollision(b);
                                b.onCollision(a);
                            } else if (a.bodyType==BODY_DYNAMIC && b.bodyType==BODY_KINEMATIC) {
                                normal = resolveStaticCollision(a, b);
                                a.onCollision(b);
                                b.onCollision(a);
                            } else if (a.bodyType==BODY_KINEMATIC && b.bodyType==BODY_STATIC) {
                                normal = resolveStaticCollision(a, b);
                                a.onCollision(b);
                                b.onCollision(a);
                            }
                        }

                    }
                }

                // Добавляем к позиции объекта ускорение
                a.x += a.velocity.x;  // перемещаем объект по его направлению X
                a.y += a.velocity.y;  // перемещаем объект по его направлению Y



            }
        }
    }



    /**
     * Возвращает нормаль и глубину столкновения двух объектов
     * @param A первый объект
     * @param B второй объект
     * @return нормаль столкновения или NV_ZERO если не пересеклись
     */
    public static Vector getCollisionNormal(GameObject A, GameObject B) {
        AABB a = A.getWorldBounds();
        AABB b = B.getWorldBounds();

       // Vector centerA = a.center;
       // Vector centerB = b.center;

        // Посчитать расстояние между центрами
        distance.x = b.centerX - a.centerX;
        distance.y = b.centerY - a.centerY;

        float lengthY = Math.abs(distance.y / (a.height + b.height));
        float lengthX = Math.abs(distance.x / (a.width + b.width));

        Vector normal;

        if (lengthY > lengthX) {
            if (distance.y >= 0) {
                normal = NV_TOP;
            } else {
                normal = NV_BOTTOM;
            }
            penetration = (a.height + b.height) / 2 - Math.abs(distance.y);
        } else {
            if (distance.x >= 0) {
                normal = NV_RIGHT;
            } else {
                normal = NV_LEFT;
            }
            penetration = (a.width + b.width) / 2 - Math.abs(distance.x);
        }
        return normal;
    }



    /**
     * Разрешает столкновение с учётом импульса и упругости пары динамических объектов,
     * на основе вычисленной нормали столкновения.
     * @param A первый динамический объект
     * @param B второй динамический объект
     */
    protected static Vector resolveDynamicCollision(GameObject A, GameObject B) {
        // вычислить нормаль столкновения
        Vector normal = getCollisionNormal(A,B);

        float invAmass = (A.mass != 0) ? 1 / A.mass : 0.0f;
        float invBmass = (B.mass != 0) ? 1 / B.mass : 0.0f;

        // Посчитать относительную скорость объектов RV = B - A
        rv.copy(B.velocity);
        rv.sub(A.velocity);

        // Посчитать относительную скорость в с точки зрения направления нормали столкновения
        float velAlongNormal= rv.dotProduct(normal);
        // Не разрешать столкновения если скорости направлены в противоположные стороны
        if(velAlongNormal <= 0) {
            // Рассчитать упругость объектов (минимальная из двух)
            float e = Math.min(A.restitution, B.restitution);
            // Вычислить скаляр импулься
            float j = -(1 + e) * velAlongNormal;
            j /= invAmass + invBmass;
            // Приложить импульсы Impuls = normal * j
            impulse.copy(normal);
            impulse.mul(j);

            // A.velocity -= 1 / A.mass * impulse;
            // B.velocity += 1 / B.mass * impulse;
            impulseA.copy(impulse);
            impulseA.mul(invAmass);
            impulseB.copy(impulse);
            impulseB.mul(invBmass);
            A.velocity.sub(impulseA);
            B.velocity.add(impulseB);


        }
        //-----------------------------------------------
        // Коррекция накопленной ошибки с FLOAT
        //-----------------------------------------------
        float value = (Math.max( penetration - correctionSlop, 0.0f ) / (invAmass + invBmass)) * correctionPercent;
        correction.copy(normal);
        correction.mul(value);
        impulseA.copy(correction); impulseA.mul(invAmass);
        impulseB.copy(correction); impulseB.mul(invBmass);
        A.velocity.x -= impulseA.x;
        A.velocity.y -= impulseA.y;
        B.velocity.x += impulseB.x;
        B.velocity.y += impulseB.y;

        // Чуть-чуть трения при столкновении в плоскости столкновения
        // if (normal.y != 0f) A.velocity.x *= friction;
        // if (normal.x != 0f) A.velocity.y *= friction;

        //-------------------------------------------------
        // Тут боремся с "дрожащими" объектами
        // Если столкновение снизу и ускорение < epsilon
        //-------------------------------------------------
    /*    if (normal==NV_BOTTOM && rv.length() < epsilon) {
            A.grounded = true;
            A.velocity.y = 0;
            A.velocity.x = 0;
        } else {
            A.grounded = false;
        }
*/


        return normal;
    }


    /**
     * Разрешает столкновение динамического объекта со статическим
     * @param A динамический объект
     * @param B статический объект
     */
    protected static Vector resolveStaticCollision(GameObject A, GameObject B) {
        B.mass = 0;
        B.velocity.x = 0;
        B.velocity.y = 0;
        return resolveDynamicCollision(A,B);
    }

}
