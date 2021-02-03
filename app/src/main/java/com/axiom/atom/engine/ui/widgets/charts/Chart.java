package com.axiom.atom.engine.ui.widgets.charts;

import android.graphics.Color;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.gameplay.Utils;

/**
 * todo Компонент - линейная диаграмма
 * по горизонтали - время (количество отсчётов)
 * по вертикали - значение
 * оси подписаны по значениям и нанесена сетка (по 5-10 значений каждой оси)
 * дополнительно:
 * - количество рядов данных может быть несколько (разные цвета)
 * - Учитывать многопоточность: данные обновляются асинхронно
 */
public class Chart extends Widget {

    private static int padding = 16;

    // Здесь храним ряды данных для потока отрисовки
    private final static int MAX_VALUES = 32;
    private final double[] values = new double[MAX_VALUES];
    private double maxValue = Double.MIN_VALUE;
    private double minValue = Double.MAX_VALUE;
    private double range = 1;
    private int count = 0;

    /**
     * Обновить значения ряда даных
     * @param newValues
     */
    public void updateValues(double[] newValues, int size) {

        if (newValues==null || size <= 0) {
            count = 0;
            return;
        }

        synchronized (values) {
            if (size > newValues.length) size = newValues.length;
            // Если длина массива новых значений меньше или равна буфера
            if (size <= values.length) {
                // просто копируем и сохраняем длину массива
                System.arraycopy(newValues, 0, values, 0, newValues.length);
                count = size;
            } else {
                // Если длина массива новых значний больше буфера - копируем ближайшие значения
                count = values.length;
                float lengthRatio = ((float) size) / ((float) values.length);
                for (int i=0; i<values.length; i++) {
                    int nearestIndex = Math.round(i * lengthRatio);
                    values[i] = newValues[nearestIndex];
                }
            }
            // находим максимальное и минимальное значение в ряде данных
            maxValue = Float.MIN_VALUE;
            minValue = Float.MAX_VALUE;
            for (int i=0; i<count; i++) {
                double value = values[i];
                if (value > maxValue) maxValue = value;
                if (value < minValue) minValue = value;
            }
            range = maxValue - minValue;
        }

    }


    @Override
    public void draw(Camera camera) {
        super.draw(camera);

        AABB wBounds = getWorldBounds();
        GraphicsRender.setZOrder(zOrder + 1);

        float graphWidth = getWidth() - padding * 2;
        float graphHeight = getHeight() - padding * 2;
        float x = wBounds.min.x + padding;
        float y = wBounds.min.y + padding;
        float oldX = x;
        float oldY;

        // Отрисуем черный фон
        GraphicsRender.setColor(1,1,1,0.8f);
        GraphicsRender.setZOrder(zOrder + 1);
        GraphicsRender.drawRectangle(x - padding, y - padding, graphWidth + padding * 2,  graphHeight + padding * 2);

        synchronized (values) {
            y += (int) (normalized(values[0]) * graphHeight);
            oldY = y;
            GraphicsRender.setZOrder(zOrder + 2);
            GraphicsRender.setColor(Color.BLACK);

            // Линия нуля
            float zeroValue = (int) (normalized(0) * graphHeight);
            GraphicsRender.setLineThickness(2);
            GraphicsRender.drawLine(wBounds.min.x + padding,wBounds.min.y + padding + zeroValue,
                    wBounds.min.x + padding + graphWidth, wBounds.min.y + padding + zeroValue);

            // Рисуем сам график
            GraphicsRender.setColor(0.6f,0.6f,0,1);
            GraphicsRender.setLineThickness(4);
            for (int i = 0; i < count; i++) {
                x = wBounds.min.x + padding + i * (graphWidth / count + 1);
                y = wBounds.min.y + padding + (int) (normalized(values[i]) * graphHeight);
                GraphicsRender.drawLine(oldX, oldY, x, y);
                oldX = x;
                oldY = y;
            }
        }

        GraphicsRender.setColor(Color.BLACK);
        GraphicsRender.drawText("" + maxValue, wBounds.min.x + padding, wBounds.min.y + graphHeight, 1f);
        GraphicsRender.drawText("" + minValue, wBounds.min.x + padding, wBounds.min.y + padding, 1f);
    }


    /**
     * Нормализует значение в рамках диапазона ряда данных
     * @param value
     * @return 0.0-1.0
     */
    private double normalized(double value) {
        return (value - minValue) / range;
    }

}
