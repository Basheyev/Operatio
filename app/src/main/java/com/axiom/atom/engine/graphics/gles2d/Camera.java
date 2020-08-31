package com.axiom.atom.engine.graphics.gles2d;

import android.opengl.Matrix;

import com.axiom.atom.engine.core.GameView;
import com.axiom.atom.engine.core.geometry.AABB;

/**
 * Камера - вычисляет ортогнальную матрицу для наблюдения за точкой в игровом мире.
 * Формирует логическое разрешение экрана для всех устройств 1920x1080 (HD 16:9)
 * <br><br>
 * (С) Atom Engine, Bolat Basheyev 2020
 */
public class Camera {

    // Логическое разрешение экрана для всех устройств 1920x1080 (HD 16:9)
    public static final float SCREEN_WIDTH = 1920;
    public static final float SCREEN_HEIGHT = 1080;

    private static Camera camera;
    private float x1, y1, x2, y2;
    private float x, y;
    private float[] cameraMatrix = new float[16];
    private float oldX, oldY;


    /**
     * Возвращает единственный экземпляр камеры (Singleton)
     * @return единственный экземпляр камеры (Singleton)
     */
    public static Camera getInstance() {
        if (camera==null) camera = new Camera();
        return camera;
    }

    /**
     * Конструктор камеры
     */
    private Camera() {
        // По умолчанию смотрим в центр экрана
        lookAt(SCREEN_WIDTH / 2.0f,SCREEN_HEIGHT / 2.0f);
        // Инициализируем объекты для отрисовки прямоугольника
    }

    /**
     * Направляет камеру в необходимую точку игрового мира
     * @param x координата в игровом мире
     * @param y координата в игровом мире
     * @return ортогональная матрица камеры (mat4 = float 4x4)
     */
    public float[] lookAt(float x, float y) {
        if (x==oldX && y==oldY) return cameraMatrix;
        // Ортогональная проекция 1920x1080 (HD)
        // удостовериться, что не меняем матрицу во время отрисовки кадра
        synchronized (cameraMatrix) {
            x1 = x - (SCREEN_WIDTH) / 2.0f;
            y1 = y - (SCREEN_HEIGHT) / 2.0f;
            x2 = x + (SCREEN_WIDTH ) / 2.0f;
            y2 = y + (SCREEN_HEIGHT ) / 2.0f;
            this.x = x; // Camera center X
            this.y = y; // Camera center Y
            Matrix.orthoM(cameraMatrix, 0, x1, x2, y1, y2, -1, 1);
            oldX = x;
            oldY = y;
        }
        return cameraMatrix;
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
        return isVisible(rect.min.x, rect.min.y, rect.max.x, rect.max.y);
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

    /**
     * Конвертировать координаты AABB в физические экранные координаты
     * @param box объект который необходимо конвертировать в него же и записать
     * @return true - если он видимый на экране, false - если нет
     */
    public boolean convertToScreen(AABB box) {
        // Вычисляем видна ли область на экране
        boolean isVisibleOnScreen = box.collides(x1,y1,x2,y2);
        // В любом случае конвертируем
        GameView view = GameView.getInstance();
        float resolutionX = view.getWidth();
        float resolutionY = view.getHeight();
        float minX = (box.min.x - camera.x1) / SCREEN_WIDTH * resolutionX;
        float minY = (box.min.y - camera.y1) / SCREEN_HEIGHT * resolutionY;
        float maxX = (box.max.x - camera.x1) / SCREEN_WIDTH * resolutionX;
        float maxY = (box.max.y - camera.y1) / SCREEN_HEIGHT * resolutionY;
        box.setBounds(minX, minY, maxX, maxY);
        return isVisibleOnScreen;
    }

}
