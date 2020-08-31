package com.axiom.atom.engine.graphics.ui;

import android.view.MotionEvent;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;

import java.util.ArrayList;

/**
 * Реализует простейший виджет пользовательского интерфейса (контейнер)
 */
public class Widget {

    public boolean visible;                // Виден ли виджет (отображается/обрабатывает события)
    public boolean opaque = true;
    public float r,g,b,alpha;
    protected int zOrder = 1000;           // Слой виджета

    protected Widget parent;               // Родительский виджет (null для корневых)
    protected ArrayList<Widget> children;  // Дочерние виджеты (вложенные)
    protected AABB localBounds;            // Границы виджета в координатах родительского виджета
    protected AABB worldBounds;            // Границы виджета в мировых координатах
    protected AABB scissorBounds;          // Границы отсечения в экранных координатах


    /**
     * Создает корневой виджет сцены на весь экран
     */
    public Widget() {
        this(0,0, Camera.SCREEN_WIDTH, Camera.SCREEN_HEIGHT);
        r = 0.6f;
        g = 0.6f;
        b = 0.8f;
        alpha = 0.7f;
    }


    public Widget(float x, float y, float width, float height) {
        this.children = new ArrayList<Widget>();
        this.localBounds = new AABB(x,y, x + width, y + height);
        this.worldBounds = new AABB(x,y, x + width, y + height);
        this.scissorBounds = new AABB(0,0, 0, 0);
        this.visible = true;
    }


    public Widget getParent() {
        return parent;
    }

    public ArrayList<Widget> getChildren() {
        return children;
    }

    public boolean addChild(Widget widget) {
        // Уходим если добавляют null
        if (widget==null) return false;
        // Уходим если уже добавляли
        if (children.contains(widget)) return false;
        // Уходим если среди родительских есть такой
        // (чтобы избежать зацикливания при отрисовке)
        Widget wdg = this;
        while (wdg != null) {
            if (wdg == widget) return false;
            wdg = wdg.parent;
        }
        // Добавляем в дочерние
        children.add(widget);
        widget.zOrder = zOrder + 1;
        widget.parent = this;
        return true;
    }

    public void removeChild(Widget widget) {
        if (widget!=null) {
            children.remove(widget);
            widget.parent = null;
        }
    }

    /**
     * Устанавливает границы виджета в координатах родительского виджета
     * @param x - минимальная левая координата
     * @param y - минимальныя нижняя координата
     * @param width - ширина виджета
     * @param height - высота виджета
     */
    public void setLocalBounds(float x, float y, float width, float height) {
        localBounds.setBounds(x, y, x + width, y + height);
    }

    /**
     * Возвращает границы объекта в координатах родительского виджета
     * @return границы объекта
     */
    public AABB getLocalBounds() {
        return localBounds;
    }


    /**
     * Возвращает границы виджета в мировых координатах (без учёта границ родительского виджета)
     * @return границы виджета в мировых координатах для отрисовки виджета
     */
    protected AABB getWorldBounds() {
        if (parent != null) {
            AABB parentWorldBounds = parent.getWorldBounds();
            float x1, y1, x2, y2;
            x1 = parentWorldBounds.min.x + localBounds.min.x;
            y1 = parentWorldBounds.min.y + localBounds.min.y;
            x2 = parentWorldBounds.min.x + localBounds.max.x;
            y2 = parentWorldBounds.min.y + localBounds.max.y;
            worldBounds.setBounds(x1, y1, x2, y2);
        } else {
            worldBounds.copy(localBounds);
        }
        return worldBounds;
    }


    /**
     * Возвращает физическую экранную область отсечения виджета
     * @return прямоугольник отсечения на физическом экране либо null если нет пересечения
     */
    protected AABB getScreenClippingAABB(Camera camera) {
        // Весь алгоритм построен так, чтобы не выделять память дополнительно на каждом кадре
        // Берем сначала область виджета в мировых координатах
        scissorBounds.copy(getWorldBounds());
        Widget widget = this;
        // Проходим от текущего виджета до самого корневого родительского
        // и вычисляем область пересечения в мировых координатах (видимый ли виджет вообще)
        while (widget != null) {
            if (scissorBounds.findIntersection(widget.getWorldBounds(), scissorBounds)==null) {
                return null;
            }
            widget = widget.parent;
        }
        // Пересчитываем с помощью камеры мировые координаты в физические экранные координаты
        if (!camera.convertToScreen(scissorBounds)) return null; // Возвращем null если не виден
        // Возвращаем видимую физическую экранную область
        return scissorBounds;
    }

    /**
     * Отрисовывает виджет и дочерние виджеты
     * @param camera
     */
    public void draw(Camera camera) {
        AABB clippingArea = getScreenClippingAABB(camera);
        if (clippingArea==null) return;

        if (opaque) {
            GraphicsRender.setZOrder(zOrder);
            GraphicsRender.setColor(r, g, b, alpha);
            GraphicsRender.drawRectangle(getWorldBounds(), clippingArea);
        }

        for (Widget child:children) {
            child.draw(camera);
        }
    }


    /**
     * Обработчик событий ввода виджета
     * @param event данные события ввода (движение)
     * @param worldX пересчитанная X из экранной координаты в координату игрового мира
     * @param worldY пересчитанная Y из экранной координаты в координату игрового мира
     * @return удалить ли событие, чтобы не передавать дальше (true - да, false - нет)
     */
    public boolean onMotionEvent(MotionEvent event, float worldX, float worldY) {
        // Доставляем события дочерним виджетам
        boolean deleteEvent = false;
        for (int i=0; i<children.size(); i++) {
            Widget widget = children.get(i);
            // Если widget не null
            if (widget!=null) {
                if (widget.visible) {
                    deleteEvent = widget.onMotionEvent(event, worldX, worldY);
                    if (deleteEvent) break;
                }
            }
        }
        return deleteEvent;
    }

}
