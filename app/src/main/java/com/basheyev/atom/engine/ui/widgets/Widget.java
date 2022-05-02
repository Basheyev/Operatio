package com.basheyev.atom.engine.ui.widgets;

import android.view.MotionEvent;

import com.basheyev.atom.engine.core.GameView;
import com.basheyev.atom.engine.core.geometry.AABB;
import com.basheyev.atom.engine.graphics.GraphicsRender;
import com.basheyev.atom.engine.graphics.gles2d.Camera;
import com.basheyev.atom.engine.input.ScaleEvent;
import com.basheyev.atom.engine.ui.listeners.ClickListener;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Виджет пользовательского интерфейса
 */
public abstract class Widget {

    public static final int DEFAULT_BACKGROUND = 0xCCE6E6E6;     // Стандартный цвет фона
    public static final int UI_LAYER = Integer.MAX_VALUE / 2;    // с какого слоя начиается UI

    //---------------------------------------------------------------------------------------------
    protected boolean visible = true;        // Виден ли виджет (отображается/обрабатывает события)
    protected boolean opaque = true;         // Является ли виджет непрозрачным
    protected boolean scissors = false;      // Обрезать ли область виджета на уровне рендера
    protected float[] color = new float[4];  // Цвет виджета (если непрозрачный)
    protected int zOrder = UI_LAYER;         // Слой учитываемый при отрисовке рендером
    //---------------------------------------------------------------------------------------------
    protected Widget parent;                 // Родительский виджет (null для корневых)
    protected ArrayList<Widget> children;    // Дочерние виджеты (вложенные)
    protected AABB localBounds;              // Границы виджета в координатах родительского виджета
    protected AABB worldBounds;              // Границы виджета в мировых координатах
    protected AABB scissorBounds;            // Границы отсечения в экранных координатах
    protected String tag = "";               // Метка виджета
    //---------------------------------------------------------------------------------------------
    protected ClickListener clickListener;   // Обработчик нажатия на виджет
    protected boolean pressed = false;       // Есть ли сейчас нажатие на виджет
    //---------------------------------------------------------------------------------------------
    protected Comparator<Widget> widgetComparator = new Comparator<Widget>() {
        @Override
        public int compare(Widget widget, Widget t1) {
            return Integer.compare(widget.zOrder, t1.zOrder);
        }
    };


    /**
     * Создает корневой виджет сцены на весь экран
     */
    public Widget() {
        this(0,0, Camera.WIDTH, Camera.HEIGHT);
    }


    /**
     * Создает виджет с заданными координатами
     * @param x левая координата
     * @param y нижняя координата
     * @param width ширина
     * @param height высота
     */
    public Widget(float x, float y, float width, float height) {
        this.children = new ArrayList<Widget>();
        this.localBounds = new AABB(x,y, x + width, y + height);
        this.worldBounds = new AABB(x,y, x + width, y + height);
        this.scissorBounds = new AABB(0,0, 0, 0);
        GraphicsRender.colorIntToFloat(DEFAULT_BACKGROUND, color);
    }

    //-------------------------------------------------------------------------------------------
    // Методы управления дочерними виджетами
    //-------------------------------------------------------------------------------------------

    /**
     * Получить родительский виджет
     * @return родительский виджет или null если виджет корневой
     */
    public Widget getParent() {
        return parent;
    }


    /**
     * Добавить дочерний виджет
     * @param widget добавляемый дочерний виджет
     * @return true - если добавлен, false - добавить нельзя
     */
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
        // Перерасчитать порядок отрисовки нового дочернего элемента
        int newZOrder = zOrder + getRenderLayersCount();
        int difference = newZOrder - widget.zOrder;
        widget.zOrder = newZOrder;
        widget.adjustChildZOrder(difference);
        return true;
    }


    /**
     * Получить список дочерних виджетов
     * @return список дочерних виджетов
     */
    public ArrayList<Widget> getChildren() {
        return children;
    }


    /**
     * Получить порядок отрисовки (Z-Order)
     * @return порядок отрисовки
     */
    public int getZOrder() {
        return zOrder;
    }


    /**
     * Установить Z-Order виджета - порядок отрисовки
     * @param zOrder номер слоя
     */
    public void setZOrder(int zOrder) {
        // Посчитать разницу между текущем слоем и новым
        int difference = zOrder - this.zOrder;
        // Установит порядок отрисовки
        this.zOrder = zOrder;
        // Перерасчитать порядок отрисовки дочерних элементов
        adjustChildZOrder(difference);
    }


    /**
     * При изменении Z-Order виджета пересчитывает Z-Order дочерних компонентов
     */
    protected void adjustChildZOrder(int difference) {
        Widget child;
        for (int i=0; i<children.size(); i++) {
            child = children.get(i);
            child.zOrder += difference;
            child.adjustChildZOrder(difference);
        };
    }


    /**
     * Количество слоев необходимых для отрисовки компонента
     * @return количество слоев необходимых для виджета
     */
    protected int getRenderLayersCount() {
        return 4;
    }


    /**
     * Удаляет дочерний виджет
     * @param widget дочерний виджет который необходимо удалить
     */
    public void removeChild(Widget widget) {
        if (widget!=null) {
            children.remove(widget);
            widget.parent = null;
        }
    }

    //-------------------------------------------------------------------------------------------
    // Управление размерами и границами
    //-------------------------------------------------------------------------------------------

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
     * Установить положение виджета
     * @param x левая координата
     * @param y нижняя координата
     */
    public void setLocation(float x, float y) {
        setLocalBounds(x, y, localBounds.width, localBounds.height);
    }


    /**
     * Установить ширину и высоту виджета
     * @param width ширина
     * @param height высота
     */
    public void setSize(float width, float height) {
        if (width < 1) width = 1;
        if (height < 1) height = 1;
        setLocalBounds(localBounds.minX, localBounds.minY, width, height);
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

    //--------------------------------------------------------------------------------------------
    // Управление цветом
    //--------------------------------------------------------------------------------------------

    public void setColor(float r, float g, float b, float alpha) {
        color[0] = r;
        color[1] = g;
        color[2] = b;
        color[3] = alpha;
    }

    public void setColor(int rgba) {
        GraphicsRender.colorIntToFloat(rgba, color);
    }


    public int getColor() {
        return GraphicsRender.colorFloatToInt(color);
    }


    //--------------------------------------------------------------------------------------------
    // Отрисовка виджета и его дочерних виджетов
    //--------------------------------------------------------------------------------------------

    /**
     * Отрисовывает виджет и дочерние виджеты
     * @param camera активная камера
     */
    public void draw(Camera camera) {
        // Если нет родителя двигать за камерой
        if (parent==null) camera.getCameraBounds(localBounds);
        // Если виджет видимый, то отрисовать дочерние виджеты
        if (visible) {
            Widget child;
            int size = children.size();
            for (int i = 0; i < size; i++) {
                child = children.get(i);
                if (child != null) child.draw(camera);
            }
        }
    }


    public boolean isVisible() {
        return visible;
    }


    public void setVisible(boolean visible) {
        this.visible = visible;
    }


    public boolean isOpaque() {
        return opaque;
    }


    public void setOpaque(boolean opaque) {
        this.opaque = opaque;
    }


    public boolean isScissorsEnabled() {
        return scissors;
    }


    public void setScissorsEnabled(boolean scissorsEnabled) {
        this.scissors = scissorsEnabled;
    }

    //--------------------------------------------------------------------------------------------
    // Обработка пользовательского ввода
    //--------------------------------------------------------------------------------------------

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

        // Берём разрешение экрана в физических координатах
        GameView view = GameView.getInstance();
        int viewHeight = view.getHeight();

        // Доставляем события дочерним виджетам в обратном порядке добавления
        Widget widget;
        int size = children.size();
        for (int i=size-1; i>=0; i--) {
            widget = children.get(i);
            if (widget==null) continue;
            if (!widget.visible) continue;
            // Взять экранную область дочернего виджета в физических координатах экрана
            AABB box = widget.getScissors();
            if (box==null) continue;
            // Если нажатие попадает в область дочернего виджета в физических координатах
            // Переворачиваем Y координату так как система отсчёта OpenGL идёт снизу
            if (box.collides(event.getX(), viewHeight - event.getY())) {
                eventHandeled = widget.onMotionEvent(event, worldX, worldY);
                // Если событие обработано уходим
                if (eventHandeled) return true;
            }
        }
        //-------------------------------------------------------------------
        // Если произошел клик и событие не обработано дочерними виджетами
        //-------------------------------------------------------------------
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            pressed = true;
        } else if (action == MotionEvent.ACTION_UP) {
            // И у виджета есть обработчик клика
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


    public void setClickListener(ClickListener listener) {
        this.clickListener = listener;
    }

    public boolean onScaleEvent(ScaleEvent event, float worldX, float worldY) {
        return false;
    }

    //---------------------------------------------------------------------------------------------
    // Выставление метки виджета для обработчиков ввода
    //---------------------------------------------------------------------------------------------

    public void setTag(String tag) {
        if (tag==null) tag = "";
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

}
