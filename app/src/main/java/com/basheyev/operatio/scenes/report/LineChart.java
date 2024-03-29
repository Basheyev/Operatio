package com.basheyev.operatio.scenes.report;

import android.graphics.Color;

import com.basheyev.atom.engine.core.geometry.AABB;
import com.basheyev.atom.engine.graphics.GraphicsRender;
import com.basheyev.atom.engine.graphics.gles2d.Camera;
import com.basheyev.atom.engine.ui.widgets.Widget;
import com.basheyev.operatio.model.common.FormatUtils;

/**
 * Линейная диаграмма
 * по горизонтали - время (количество отсчётов)
 * по вертикали - значение
 * оси подписаны по значениям и нанесена сетка (по 5-10 значений каждой оси)
 * дополнительно:
 * - количество рядов данных может быть несколько (разные цвета)
 * - Учитывать многопоточность: данные обновляются асинхронно
 */
public class LineChart extends Widget {

    public static final int GRAPH_BACKGROUND = 0x80000000;

    // Ряд данных
    public class DataSeries {
        private final static int MAX_VALUES = 30;
        private final double[] values = new double[MAX_VALUES];
        private double maxValue = 0;
        private double minValue = 0;
        private double range = 0;
        private int count = 0;
        private int color;
    }

    private DataSeries[] dataSeries;
    private double totalMax = 0;
    private double totalMin = 0;
    private StringBuffer totalMaxStr;
    private StringBuffer totalMinStr;
    private static int padding = 16;

    /**
     * Конструктор линейной диаграммы
     * @param dataSeriesCount количество рядов данных
     */
    public LineChart(int dataSeriesCount) {
        super();
        if (dataSeriesCount<1) dataSeriesCount = 1;
        dataSeries = new DataSeries[dataSeriesCount];
        for (int i=0; i<dataSeries.length; i++) dataSeries[i] = new DataSeries();
        totalMax = Double.MIN_VALUE;
        totalMin = Double.MAX_VALUE;
        totalMaxStr = new StringBuffer();
        totalMinStr = new StringBuffer();
    }

    /**
     * Обновляет значения ряда данных
     * @param index номер ряда
     * @param newValues массив новых значений
     * @param size количество значений
     */
    public void updateData(int index, double[] newValues, int size, int color) {

        // Проверяем не вышли ли мы за пределы
        if (index < 0 || index >= dataSeries.length) return;
        if (newValues == null || size <= 0) return;

        // Берём соответствующий ряд данных
        DataSeries data = dataSeries[index];

        // Копируем данные с учетом конкурентного чтения/записи
        synchronized (data.values) {
            if (size > newValues.length) size = newValues.length;
            // Если длина массива новых значений меньше или равна буфера
            if (size <= data.values.length) {
                // просто копируем и сохраняем длину массива
                System.arraycopy(newValues, 0, data.values, 0, newValues.length);
                data.count = size;
            } else {
                // Если длина массива новых значний больше буфера - копируем ближайшие значения
                data.count = data.values.length;
                float lengthRatio = ((float) size) / ((float) data.values.length);
                for (int i=0; i<data.values.length; i++) {
                    int nearestIndex = Math.round(i * lengthRatio);
                    data.values[i] = newValues[nearestIndex];
                }
            }
            // находим максимальное и минимальное значение в ряде данных
            data.maxValue = Double.MIN_VALUE;
            data.minValue = Double.MAX_VALUE;
            for (int i=0; i<data.count; i++) {
                double value = data.values[i];
                if (value > data.maxValue) data.maxValue = value;
                if (value < data.minValue) data.minValue = value;
            }
            data.range = data.maxValue - data.minValue;
            data.color = color;

            calculateTotalMinMax();

        }

    }


    @Override
    public void draw(Camera camera) {
        super.draw(camera);

        AABB wBounds = getWorldBounds();
        GraphicsRender.setZOrder(zOrder + 1);

        float graphWidth = getWidth() - padding * 2;
        float graphHeight = getHeight() - padding * 2;
        float x = wBounds.minX + padding;
        float y = wBounds.minY + padding;
        float oldX = x;
        float oldY;

        // Отрисуем черный фон
        GraphicsRender.setColor(0,0,0,0.5f);
        GraphicsRender.setZOrder(zOrder + 1);
        GraphicsRender.drawRectangle(x - padding, y - padding, graphWidth + padding * 2,  graphHeight + padding * 2);

        // Отрисовываем ряды данных
        GraphicsRender.setZOrder(zOrder + 2);
        GraphicsRender.setColor(GRAPH_BACKGROUND);

        // Отрисовываем каждый ряд данных
        for (DataSeries data : dataSeries) {
            // Берём соответствующий ряд данных
            synchronized (data.values) {
                x = wBounds.minX + padding;
                oldX = x;
                y = wBounds.minY + padding;
                y += (int) (normalized(data.values[0]) * graphHeight);
                oldY = y;
                // Рисуем сам график
                GraphicsRender.setColor(data.color);
                GraphicsRender.setLineThickness(4);
                for (int i = 0; i < data.count; i++) {
                    x = wBounds.minX + padding + i * (graphWidth / data.count + 1);
                    y = wBounds.minY + padding + (int) (normalized(data.values[i]) * graphHeight);
                    GraphicsRender.drawLine(oldX, oldY, x, y);
                    GraphicsRender.drawRectangle(x - 5, y - 5, 10, 10);
                    oldX = x;
                    oldY = y;
                }
            }

        }

        GraphicsRender.setColor(Color.WHITE);
        if (totalMax != Double.MIN_VALUE && totalMin != Double.MAX_VALUE) {
            GraphicsRender.drawText(totalMaxStr, wBounds.minX + padding, wBounds.minY + graphHeight, 1f);
            GraphicsRender.drawText(totalMinStr, wBounds.minX + padding, wBounds.minY + padding, 1f);
        }

    }

    //----------------------------------------------------------------------------------------------


    /**
     * Нормализует значение в рамках диапазона ряда данных
     * @param value
     * @return 0.0-1.0
     */
    private double normalized(double value) {
        //return (value - totalMin) / (totalMax - totalMin);
        return value / totalMax;
    }


    private void calculateTotalMinMax() {
        int longestDataSeries = 0;

        for (DataSeries dataSery : dataSeries) {
            if (dataSery.count > longestDataSeries) longestDataSeries = dataSery.count;
        }

        totalMax = Double.MIN_VALUE;
        totalMin = Double.MAX_VALUE;
        double value;
        for (int i=0; i<longestDataSeries; i++) {
            for (int j=0; j<dataSeries.length; j++) {
                if (i < dataSeries[j].count) {
                    value = dataSeries[j].values[i];
                    if (value > totalMax) totalMax = value;
                    if (value < totalMin) totalMin = value;
                }
            }
        }

        totalMaxStr.setLength(0);
        totalMinStr.setLength(0);
        totalMaxStr = FormatUtils.formatMoneyAppend(Math.round(totalMax), totalMaxStr);
        totalMinStr.append("0");
      //  totalMinStr = FormatUtils.formatMoneyAppend(Math.round(totalMin), totalMinStr);
    }

}
