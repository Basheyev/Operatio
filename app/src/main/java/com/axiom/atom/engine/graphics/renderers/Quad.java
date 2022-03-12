package com.axiom.atom.engine.graphics.renderers;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Program;
import com.axiom.atom.engine.graphics.gles2d.Texture;

/**
 * Базовый класс для всей 2D графики - четырехугольник.
 * Содержит информацию о шейдере, текстуре, вершинах, текстурных координатах, цвете,
 * слою отрисовке и экранной области обрезки.
 */
public class Quad {

    protected int zOrder;                       // Слой отрисовки (Z-порядок)
    protected Program program;                  // Используемая программа шейдеров
    protected Texture texture;                  // Текстура прямоугольника (null если её нет)
    protected AABB scissor;                     // Область отсечения в физических координатах экрана

    protected float[] vertices =                // Вершины прямоугольника (2 треугольника - 18 коорд)
            {      -0.5f,  0.5f,  0.0f,         // левый верхний угол
                   -0.5f, -0.5f,  0.0f,         // левый нижний угол
                    0.5f,  0.5f,  0.0f,         // правый верхний угол

                   -0.5f, -0.5f,  0.0f,         // левый нижний угол
                    0.5f,  0.5f,  0.0f,         // правый верхний угол
                    0.5f, -0.5f,  0.0f,         // правый нижний угол
            };

    protected float[] texCoords =               // текстурные координаты спрайта
            {
                    0.0f, 1.0f,                 // левый верхний угол
                    0.0f, 0.0f,                 // левый нижний угол
                    1.0f, 1.0f,                 // правый верхний угол

                    0.0f, 0.0f,                 // левый нижний угол
                    1.0f, 1.0f,                 // правый верхний угол
                    1.0f, 0.0f                  // правый нижний угол
            };

    protected float[] color =                   // цвет примитива если нет текстуры
            { 0.3f, 0.5f, 0.9f, 1.0f };

    protected float rotation = 0;               // Угол поворота в радианах
    protected float centerX = 0;                // Точка поворота по X = -0.5-0.5
    protected float centerY = 0;                // Точка поворота по Y = -0.5-0.5

    //----------------------------------------------------------------------------------
    // Класс для сравнения элементов буфера при сортировке (по текстуре и z-order)
    //----------------------------------------------------------------------------------
    public static class Comparator implements java.util.Comparator<Quad> {
        @Override
        public int compare(Quad a, Quad b) {
            // Сравниваем по слою
            if (a.zOrder==b.zOrder) {
                // сравиваем по текстуре
                if (a.texture==b.texture) {
                    // сравниваем по шейдеру
                    if (a.program==b.program) {
                        // сравниваем по цвету
                        return compareColor(a.color, b.color);
                    }
                    if (a.program.getProgramID() < b.program.getProgramID()) return -1; else return 1;
                }
                if (a.texture==null) return -1;
                if (b.texture==null) return 1;
                if (a.texture.getTextureID() < b.texture.getTextureID())
                    return -1;
                else
                    return 1;
            } else if (a.zOrder < b.zOrder) return -1; else return 1;
        }
    }

    //----------------------------------------------------------------------------------
    // Методы управления цветом
    //----------------------------------------------------------------------------------

    /**
     * Сравнивает два цвета по компонентам
     * @param a массив цвета #1 (float R,G,B,A)
     * @param b массив цвета #2 (float R,G,B,A)
     * @return 0 - если равны, 1 - если первый больше, -1 - если второй больше
     */
    public static int compareColor(float[] a, float[] b) {
        for (int i=0; i<4; i++) {
            if (a[i] > b[i]) return 1;
            if (a[i] < b[i]) return -1;
        }
        return 0;
    }


    /**
     * Применяет цвет по компоннетам
     * @param r красный
     * @param g зеленый
     * @param b синий
     * @param a альфа-канал
     */
    public void setColor(float r, float g, float b, float a) {
        color[0] = r;
        color[1] = g;
        color[2] = b;
        color[3] = a;
    }

    /**
     * Возвращает текущий цвет
     * @param dstColor куда будет записан четыре компонента цвета (float)
     */
    public void getColor(float[] dstColor) {
        dstColor[0] = color[0];
        dstColor[1] = color[1];
        dstColor[2] = color[2];
        dstColor[3] = color[3];
    }

    /**
     * Применяет цвет в виде целого числа
     * @param rgba цвет в виде целого числа
     */
    public void setColor(int rgba) {
        GraphicsRender.colorIntToFloat(rgba, color);
    }

    /**
     * Возвращает цвет в виде целого числа
     * @return цвет в виде целого числа
     */
    public int getColor() {
        return GraphicsRender.colorFloatToInt(color);
    }

    /**
     * Устанавливает уровень прозрачности цвета 0.0-1.0 (альфа-канал)
     * @param alpha 0 - полностью прозрачный, 1 - полностью не прозрачный
     */
    public void setAlpha(float alpha) {
        if (alpha < 0) alpha = 0;
        if (alpha > 1) alpha = 1;
        color[3] = alpha;
    }

    /**
     * Возвращает уровень прозрачности цвета 0.0-1.0 (альфа канал)
     * @return 0 - полностью прозрачный, 1 - полностью не прозрачный
     */
    public float getAlpha() {
        return color[3];
    }

    //----------------------------------------------------------------------------------
    // Методы управления вершинами
    //----------------------------------------------------------------------------------


    /**
     * Инициализирует массив координат вершин (-0.5, -0.5)-(0.5, 0.5)
     */
    protected void initializeVertices() {
        // Треугольник 1
        vertices[0] = -0.5f;
        vertices[1] = 0.5f;
        vertices[3] = -0.5f;
        vertices[4] = -0.5f;
        vertices[6] = 0.5f;
        vertices[7] = 0.5f;
        // Треугольник 2
        vertices[9] = -0.5f;
        vertices[10] = -0.5f;
        vertices[12] = 0.5f;
        vertices[13] = 0.5f;
        vertices[15] = 0.5f;
        vertices[16] = -0.5f;
    }


    /**
     * Инициализирует массив координат вершин (x1, y1)-(x2, y2)
     */
    protected void initializeVertices(float x1, float y1, float x2, float y2) {
        // Треугольник 1
        vertices[0] = x1; //-0.5f;
        vertices[1] = y2; //0.5f;
        vertices[3] = x1; //-0.5f;
        vertices[4] = y1; //-0.5f;
        vertices[6] = x2; //0.5f;
        vertices[7] = y2; //0.5f;
        // Треугольник 2
        vertices[9] = x1;  //-0.5f;
        vertices[10] = y1; //-0.5f;
        vertices[12] = x2; //0.5f;
        vertices[13] = y2; //0.5f;
        vertices[15] = x2; //0.5f;
        vertices[16] = y1; //-0.5f;
    }


    /**
     * Рассчитать вершины с учетом размера спрайта
     * @param scaledWidth ширина спрайта
     * @param scaledHeight высота спрайта
     */
    protected void evaluateScale(float scaledWidth, float scaledHeight) {
        for (int i=0; i<18; i+=3) {
            vertices[i] *= scaledWidth;
            vertices[i+1] *= scaledHeight;
        }
    }


    /**
     * Смещение координат вершин
     * @param x смещение
     * @param y смещение
     */
    protected void evaluateOffset(float x, float y) {
        for (int i=0; i<18; i+=3) {
            vertices[i] += x;
            vertices[i+1] += y;
        }
    }


    /**
     * Поворот вершин на заданный угол в радианах вокруг точки вращения
     * @param rotation угол поворота в радианах
     */
    protected void evaluateRotation(float rotation) {
        float cosR = (float) Math.cos(rotation);
        float sinR = (float) Math.sin(rotation);
        float x, y;
        for (int i=0; i<18; i+=3) {
            x = vertices[i] - centerX;
            y = vertices[i+1] - centerY;
            vertices[i] = x * cosR - y * sinR;
            vertices[i+1] = x * sinR + y * cosR;
        }
    }

    //----------------------------------------------------------------------------------------
    // Управление углом поворота
    //----------------------------------------------------------------------------------------

    /**
     * Установить угол вращение вокруг центра
     * @param radians угол вращения в радианах
     */
    public void setRotation(float radians) {
        this.rotation = radians;
    }

    /**
     * Установить точку вращения спрайта (по умолчанию 0, 0)
     * @param x от -0.5 (левая координата) до 0.5 (правая координата)
     * @param y от -0.5 (нижняя координата) до 0.5 (верхняя координата)
     */
    public void setRotationPoint(float x, float y) {
        centerX = x;
        centerY = y;
    }

    /**
     * Вернуть угол вращения
     * @return угол вращения в радианах
     */
    public float getRotation() {
        return rotation;
    }


    public int getZOrder() {
        return zOrder;
    }

    public void setZOrder(int zOrder) {
        this.zOrder = zOrder;
    }

    public void copyFrom(Quad src) {
        this.program = src.program;
        this.texture = src.texture;
        this.zOrder = src.zOrder;
        this.scissor = src.scissor;
        System.arraycopy(src.color, 0, this.color, 0, 4);
    }
}
