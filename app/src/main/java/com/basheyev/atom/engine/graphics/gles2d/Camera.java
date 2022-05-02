package com.basheyev.atom.engine.graphics.gles2d;

import android.content.Context;
import android.graphics.Point;
import android.opengl.Matrix;
import android.view.Display;
import android.view.WindowManager;

import com.basheyev.atom.engine.core.GameView;
import com.basheyev.atom.engine.core.geometry.AABB;

/**
 * Камера - вычисляет ортогнальную матрицу для наблюдения за точкой в игровом мире.
 * Формирует логическое разрешение экрана для всех устройств 1920x1080 (HD 16:9)
 * <br><br>
 * (С) Atom Engine, Bolat Basheyev 2020
 */
public class Camera {

    // Логическое разрешение экрана для всех устройств 1920x1080 (HD 16:9)
    public static final float WIDTH = 1920;
    public static final float HEIGHT = 1080;

    private static Camera camera;
    private float x1, y1, x2, y2;
    private float x, y;
    private final float[] cameraMatrix = new float[16];
    private float oldX, oldY;
    private float displayWidth;
    private float displayHeight;

    /**
     * Возвращает единственный экземпляр камеры (Singleton)
     * @return единственный экземпляр камеры (Singleton)
     */
    public static Camera getInstance(GameView view) {
        if (camera==null) camera = new Camera(view);
        return camera;
    }

    public static Camera getInstance() {
        return camera;
    }

    /**
     * Конструктор камеры
     */
    private Camera(GameView view) {
        Context context = view.getContext();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        displayWidth = size.x;
        displayHeight = size.y;
        // По умолчанию смотрим в центр экрана
        lookAt(WIDTH / 2.0f, HEIGHT / 2.0f);
        // Инициализируем объекты для отрисовки прямоугольника
    }

    /**
     * Направляет камеру в необходимую точку игрового мира
     * @param x координата в игровом мире
     * @param y координата в игровом мире
     */
    public void lookAt(float x, float y) {
        if (x==oldX && y==oldY) return;
        // Ортогональная проекция 1920x1080 (HD)
        // удостовериться, что не меняем матрицу во время отрисовки кадра
        synchronized (cameraMatrix) {
            x1 = x - (WIDTH) / 2.0f;
            y1 = y - (HEIGHT) / 2.0f;
            x2 = x + (WIDTH) / 2.0f;
            y2 = y + (HEIGHT) / 2.0f;
            this.x = x; // Camera center X
            this.y = y; // Camera center Y
            Matrix.orthoM(cameraMatrix, 0, x1, x2, y1, y2, -1, 1);
            oldX = x;
            oldY = y;
        }
    }

    /**
     * Определяет находится ли прямоугольник в обзоре камеры (мировые координаты).
     * Используется для оптимизации рендеринга и отсечения невидимых спрайтов.
     * @param minX мировая координата
     * @param minY мировая координата
     * @param maxX мировая координата
     * @param maxY мировая координата
     * @return true - если виден, иначе false;
     */
    public boolean isVisible(float minX, float minY, float maxX, float maxY) {
        if (y2 < minY || y1 > maxY) return false;
        if (x2 < minX || x1 > maxX) return false;
        return true;
    }

    public boolean isVisible(AABB rect) {
        return isVisible(rect.minX, rect.minY, rect.maxX, rect.maxY);
    }

    /**
     * Определяет находится ли точка в обзоре камеры
     * @param x координата точки
     * @param y координата точки
     * @return true - если точка в обзоре камере, false - иначе
     */
    public boolean isVisible(float x, float y) {
        if (x < x1 || x > x2) return false;
        if (y < y1 || y > y2) return false;
        return true;
    }

    /**
     * Возвращает последнюю вычисленную матрицу камеры
     * @return ортогональная матрица камеры (mat4 = float 4x4)
     */
    public float[] getCameraMatrix() {
        return cameraMatrix;
    }

    public float getX() {
        synchronized (cameraMatrix) {
            return x;
        }
    }

    public float getY() {
        synchronized (cameraMatrix) {
            return y;
        }
    }


    public float getMinX() {
        synchronized (cameraMatrix) {
            return x1;
        }
    }

    public float getMinY() {
        synchronized (cameraMatrix) {
            return y1;
        }
    }


    public float getMaxX() {
        synchronized (cameraMatrix) {
            return x2;
        }
    }

    public float getMaxY() {
        synchronized (cameraMatrix) {
            return y2;
        }
    }

    public void getCameraBounds(AABB dest) {
        synchronized (cameraMatrix) {
            dest.setBounds(x1, y1, x2, y2);
        }
    }


    /**
     * Конвертировать координаты AABB в физические экранные координаты
     * @param box объект который необходимо конвертировать в него же и записать
     * @return true - если он видимый на экране, false - если нет
     */
    public boolean convertWorldToScreen(AABB box) {
        // Вычисляем видна ли область на экране
        boolean isVisibleOnScreen = box.collides(x1,y1,x2,y2);
        // В любом случае конвертируем
        float minX = (box.minX - camera.x1) / WIDTH * displayWidth;
        float minY = (box.minY - camera.y1) / HEIGHT * displayHeight;
        float maxX = (box.maxX - camera.x1) / WIDTH * displayWidth;
        float maxY = (box.maxY - camera.y1) / HEIGHT * displayHeight;
        box.setBounds(minX, minY, maxX, maxY);
        return isVisibleOnScreen;
    }

    public float convertScreenToWorldX(float screenX) {
        return screenX / displayWidth * WIDTH + x1;
    }

    public float convertScreenToWorldY(float screenY) {
        return (1 - screenY / displayHeight) * HEIGHT + y1;
    }

    public float getDisplayWidth() {
        return displayWidth;
    }

    public float getDisplayHeight() {
        return displayHeight;
    }

}
