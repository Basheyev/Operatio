package com.axiom.atom.engine.graphics.renderers;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.gles2d.Program;
import com.axiom.atom.engine.graphics.gles2d.Texture;

/**
 * Базовый класс для всей 2D графики - четырехугольник.
 * Содержит информацию о шейдере, текстуре, вершинах, текстурных координатах, цвете,
 * слою отрисовке и экранной области обрезки.
 */
public class Quad {

    public int zOrder;                          // Слой отрисовки (Z-порядок)
    public Program program;                     // Используемая программа шейдеров
    public Texture texture;                     // Текстура прямоугольника (null если её нет)
    public AABB scissor;                        // Область отсечения в физических координатах экрана

    public float[] vertices =                   // Вершины прямоугольника (2 треугольника - 18 коорд)
            {      -0.5f,  0.5f,  0.0f,         // левый верхний угол
                   -0.5f, -0.5f,  0.0f,         // левый нижний угол
                    0.5f,  0.5f,  0.0f,         // правый верхний угол

                   -0.5f, -0.5f,  0.0f,         // левый нижний угол
                    0.5f,  0.5f,  0.0f,         // правый верхний угол
                    0.5f, -0.5f,  0.0f,         // правый нижний угол
            };

    public float[] texCoords =                  // текстурные координаты спрайта
            {
                    0.0f, 1.0f,                 // левый верхний угол
                    0.0f, 0.0f,                 // левый нижний угол
                    1.0f, 1.0f,                 // правый верхний угол

                    0.0f, 0.0f,                 // левый нижний угол
                    1.0f, 1.0f,                 // правый верхний угол
                    1.0f, 0.0f                  // правый нижний угол
            };

    public float[] color =                      // цвет примитива если нет текстуры
            { 0.3f, 0.5f, 0.9f, 1.0f };

    protected float rotation = 0;               // Угол поворота в радианах

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
                    // сравниваем по цвету
                    return compareColor(a.color, b.color);
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
        setColor(((rgba >> 16 ) & 0xff) / 255.0f,
                ((rgba >>  8  ) & 0xff) / 255.0f,
                ((rgba        ) & 0xff) / 255.0f,
                ((rgba >> 24) & 0xff) / 255.0f);
    }

    /**
     * Возвращает цвет в виде целого числа
     * @return цвет в виде целого числа
     */
    public int getColor() {
        int r = (int) (color[0] * 255.0f) & 0xff;
        int g = (int) (color[1] * 255.0f) & 0xff;
        int b = (int) (color[2] * 255.0f) & 0xff;
        int a = (int) (color[3] * 255.0f) & 0xff;
        return (a << 24 | r << 16 | g << 8 | b);
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
     * Инициализирует массив координат вершин
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
     * Поворот вершин на заданный угол в радианах
     * @param rotation угол поворота в радианах
     */
    protected void evaluateRotation(float rotation) {
        float cosR = (float) Math.cos(rotation);
        float sinR = (float) Math.sin(rotation);
        float x, y;
        for (int i=0; i<18; i+=3) {
            x = vertices[i];
            y = vertices[i+1];
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
     * Вернуть угол вращения
     * @return угол вращения в радианах
     */
    public float getRotation() {
        return rotation;
    }

}
