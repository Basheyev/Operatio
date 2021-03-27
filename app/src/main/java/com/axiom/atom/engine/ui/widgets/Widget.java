package com.axiom.atom.engine.ui.widgets;

import android.view.MotionEvent;

import com.axiom.atom.engine.core.GameView;
import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.input.ScaleEvent;
import com.axiom.atom.engine.ui.listeners.ClickListener;

import java.util.ArrayList;

/**
 * Реализует базовый элемент UI - виджет пользовательского интерфейса (контейнер)
 */
public abstract class Widget {

    public boolean visible = true;           // Виден ли виджет (отображается/обрабатывает события)
    public boolean opaque = true;            // Является ли виджет непрозрачным
    protected float[] color = new float[4];  // Цвет компонента (если непрозрачный)
    protected int zOrder = 1000;             // Слой виджета при отрисовке рендером

    protected Widget parent;                 // Родительский виджет (null для корневых)
    protected ArrayList<Widget> children;    // Дочерние виджеты (вложенные)
    protected AABB localBounds;              // Границы виджета в координатах родительского виджета
    protected AABB worldBounds;              // Границы виджета в мировых координатах
    protected AABB scissorBounds;            // Границы отсечения в экранных координатах
    protected String tag = "";               // Метка виджета

    protected ClickListener clickListener;   // Обработчик нажатия на виджет
    protected boolean pressed = false;       // Есть ли сейчас нажатие на виджет


    /**
     * Создает корневой виджет сцены на весь экран
     */
    public Widget() {
        this(0,0, Camera.WIDTH, Camera.HEIGHT);
    }


    public Widget(float x, float y, float width, float height) {
        this.children = new ArrayList<Widget>();
        this.localBounds = new AABB(x,y, x + width, y + height);
        this.worldBounds = new AABB(x,y, x + width, y + height);
        this.scissorBounds = new AABB(0,0, 0, 0);
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
        widget.parent = this;
        adjustZOrder(this);
        return true;
    }

    protected void adjustZOrder(Widget parent) {
        zOrder = parent.zOrder + 1;
        for (int i=0; i<children.size(); i++) {
            children.get(i).adjustZOrder(this);
        }
    }

    public void removeChild(Widget widget) {
        if (widget!=null) {
            children.remove(widget);
            widget.parent = null;
        }
    }

    //--------------------------------------------------------------------------------------------

    public void setColor(float r, float g, float b, float alpha) {
        color[0] = r;
        color[1] = g;
        color[2] = b;
        color[3] = alpha;
    }

    public void setColor(int rgba) {
        color[3] = ((rgba >> 24) & 0xff) / 255.0f;
        color[0] = ((rgba >> 16) & 0xff) / 255.0f;
        color[1] = ((rgba >>  8) & 0xff) / 255.0f;
        color[2] = ((rgba      ) & 0xff) / 255.0f;
    }

    public void getColor(float[] color) {
        color[0] = this.color[0];
        color[1] = this.color[1];
        color[2] = this.color[2];
        color[3] = this.color[3];
    }

    public int getColor() {
        return ((int)(color[3]  * 255.0f) & 0xff) << 24 |
                ((int)(color[0] * 255.0f) & 0xff) << 16 |
                ((int)(color[1] * 255.0f) & 0xff) << 8 |
                ((int)(color[2] * 255.0f) & 0xff);
    }

    //--------------------------------------------------------------------------------------------

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


    public void setLocation(float x, float y) {
        setLocalBounds(x, y, localBounds.width, localBounds.height);
    }

    public void setSize(float width, float height) {
        if (width < 1) width = 1;
        if (height < 1) height = 1;
        setLocalBounds(localBounds.minX, localBounds.minY, width, height);
    }

    public float getX() {
        return localBounds.minX;
    }

    public float getY() {
        return localBounds.minY;
    }

    public float getWidth() {
        return localBounds.width;
    }

    public float getHeight() {
        return localBounds.height;
    }

    /**
     * Возвращает границы объекта в координатах родительского виджета
     * @return границы объекта
     */
    public AABB getLocalBounds() {
        return localBounds;
    }

    //--------------------------------------------------------------------------------------------


    /**
     * Возвращает границы виджета в мировых координатах (без учёта границ родительского виджета)
     * @return границы виджета в мировых координатах для отрисовки виджета
     */
    public AABB getWorldBounds() {
        if (parent != null) {
            AABB parentWorldBounds = parent.getWorldBounds();
            float x1, y1, x2, y2;
            x1 = parentWorldBounds.minX + localBounds.minX;
            y1 = parentWorldBounds.minY + localBounds.minY;
            x2 = parentWorldBounds.minX + localBounds.maxX;
            y2 = parentWorldBounds.minY + localBounds.maxY;
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
    public AABB getScissors() {
        // Весь алгоритм построен так, чтобы не выделять память дополнительно на каждом кадре
        // Берем сначала область виджета в мировых координатах
        Widget widget = this;
        scissorBounds.copy(widget.getWorldBounds());
        // Проходим от текущего виджета до самого корневого родительского
        // и вычисляем область пересечения в мировых координатах (видимый ли виджет вообще)
        while (widget != null) {
            if (scissorBounds.findIntersection(widget.getWorldBounds(), scissorBounds)==null) {
                return null;
            }
            widget = widget.parent;
        }
        // Пересчитываем с помощью камеры мировые координаты в физические экранные координаты
        Camera camera = Camera.getInstance();
        if (!camera.convertWorldToScreen(scissorBounds)) return null; // Возвращем null если не виден
        // Возвращаем видимую физическую экранную область
        return scissorBounds;
    }

    //--------------------------------------------------------------------------------------------


    /**
     * Отрисовывает виджет и дочерние виджеты
     * @param camera
     */
    public void draw(Camera camera) {
        // Если нет родителя двигать за камерой
        if (parent==null) camera.getCameraBounds(localBounds);
        if (visible) {
            Widget child;
            int size = children.size();
            for (int i = 0; i < size; i++) {
                child = children.get(i);
                child.draw(camera);
            }
        }
    }

    //---------------------------------------------------------------------------------------------

    public void setClickListener(ClickListener listener) {
        this.clickListener = listener;
    }

    /**
     * Обработчик событий ввода виджета
     * @param event данные события ввода (движение)
     * @param worldX пересчитанная X из экранной координаты в координату игрового мира
     * @param worldY пересчитанная Y из экранной координаты в координату игрового мира
     * @return удалить ли событие, чтобы не передавать дальше (true - да, false - нет)
     */
    public boolean onMotionEvent(MotionEvent event, float worldX, float worldY) {
        if (!visible) return false;
        boolean eventHandeled = false;
        // Доставляем события дочерним виджетам
        Widget widget;
        int size = children.size();
        for (int i=0; i<size; i++) {
            widget = children.get(i);
            // Если widget не null
            if (widget!=null) {
                if (widget.visible) {
                    // Взять экранную область дочернего виджета в физических координатах экрана
                    AABB box = widget.getScissors();
                    if (box==null) continue;
                    // Берём разрешение экрана
                    GameView view = GameView.getInstance();
                    // Если нажатие попадает в область дочернего виджета в физических координатах
                    // Переворачиваем Y координату так как система отсчёта GLES идёт снизу
                    if (box.collides(event.getX(), view.getHeight() - event.getY())) {
                        eventHandeled = widget.onMotionEvent(event, worldX, worldY);
                        // Если событие обработано уходим
                        if (eventHandeled) return true;
                    }
                }
            }
        }
        //-------------------------------------------------------------------
        // Если произошел клик и событие не обработано дочерними виджетами
        //-------------------------------------------------------------------
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            pressed = true;
        } else
        if (action == MotionEvent.ACTION_UP) {
            // И в виджета есть обработчик клика
            if (pressed && clickListener != null) {
                // Вызвать обработчик
                clickListener.onClick(this);
                // Указываем, что событие обработано
                eventHandeled = true;
            }
            pressed = false;
        }
        return eventHandeled;
    }

    public boolean onScaleEvent(ScaleEvent event, float worldX, float worldY) {
        return false;
    }

    //---------------------------------------------------------------------------------------------

    public void setTag(String tag) {
        if (tag==null) tag = "";
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

}
