package com.basheyev.atom.engine.graphics;

import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.basheyev.atom.R;
import com.basheyev.atom.engine.core.GameView;
import com.basheyev.atom.engine.core.GameScene;
import com.basheyev.atom.engine.core.SceneManager;
import com.basheyev.atom.engine.data.structures.Channel;
import com.basheyev.atom.engine.graphics.gles2d.Camera;
import com.basheyev.atom.engine.graphics.gles2d.GLESObject;
import com.basheyev.atom.engine.graphics.renderers.Line;
import com.basheyev.atom.engine.graphics.renderers.Rectangle;
import com.basheyev.atom.engine.graphics.renderers.BatchRender;
import com.basheyev.atom.engine.graphics.renderers.Text;
import com.basheyev.atom.engine.core.geometry.AABB;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;



/**
 *  Графический рендер - отрисовывает всю графику
 *  (C) Atom Engine, Bolat Basheyev 2020
 */
public class GraphicsRender implements GLSurfaceView.Renderer {

    //-------------------------------------------------------------------------------------
    // Список объектов которые должны быть инициализированы в потоке/контексте OpenGL
    //-------------------------------------------------------------------------------------
    public static final int LAZY_LOAD_QUEUE_LENGTH = 1024;   // Максимальная длина очереди
    public static final int LOAD_TIME_PER_FRAME = 50;        // Время загрузки на один кадр
    protected static Channel<GLESObject> loadQueue;          // Очередь "ленивой" загрузки
    protected static Channel<GLESObject> loadedObjects;      // Список загруженных объектов

    //-------------------------------------------------------------------------------------
    // Основные объекты графического рендера
    //-------------------------------------------------------------------------------------
    protected static GraphicsRender render;            // Наш единственный рендер
    protected static Camera camera;                    // Единственная камера игровго мира
    protected GameView gameView;                       // Наш View на котором делаем рендер
    protected SceneManager sceneManager;               // Менеджер игровых сцен
    //-------------------------------------------------------------------------------------
    private Text textRender;                           // Рендер текста
    private Line lineRender;                           // Рендер линии
    private Rectangle rectangleRender;                 // Рендер прямоугольников
    //-------------------------------------------------------------------------------------
    private int framesCounter = 0;                     // Счётчик отрисованных кадров
    private int fps = 0;                               // Количество кадров в секунду (FPS)
    private long totalRenderTime = 0;                  // Общее время рендеринга в секунду
    private long averageRenderTime;                    // Среднее время рендеринга одного кадра
    private long fpsLastEvaluationTime = 0;            // Последнее время расчёта FPS (нс)
    private boolean exitGameFlag = false;

    /**
     * Возвращает единственный экземпляр графического рендера (Singleton)
     * @param gameView экземпляр GameView (где рисуем и откуда забираем ввод)
     * @param sceneManager экземпляр менеджера сцен
     * @return единственный экземпляр графического рендера (Singleton)
     */
    public static GraphicsRender getInstance(GameView gameView, SceneManager sceneManager) {
        if (render==null) render = new GraphicsRender(gameView,sceneManager);
        return render;
    }


    /**
     * Конструктор графического рендера
     * @param gameView где рисуем и откуда забираем ввод
     * @param sceneManager менеджер сцен
     */
    private GraphicsRender(GameView gameView, SceneManager sceneManager) {
        super();
        this.gameView = gameView;
        this.sceneManager = sceneManager;
        // Инициализируем статические переменные
        loadQueue = new Channel<>(LAZY_LOAD_QUEUE_LENGTH);
        loadedObjects = new Channel<>(LAZY_LOAD_QUEUE_LENGTH);
        camera = Camera.getInstance(gameView);
    }


    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        textRender = new Text();
        lineRender = new Line();
        rectangleRender = new Rectangle();
    }


    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glClearColor(0, 0, 0, 1.0f);
    }

    //------------------------------------------------------------------------------------------
    // Методы для отложеной загрузки программ, шейдеров и текстур
    //------------------------------------------------------------------------------------------

    /**
     * Добавляет объекты в очередь загрузки в GPU из других потоков
     * @param obj программа, шейдер или текстура
     */
    public static void addToLoadQueue(GLESObject obj) {
        if (loadQueue!=null) GraphicsRender.loadQueue.push(obj);
    }

    /**
     * Отложенная загрузка программ, шейдеров и текстур в GPU
     */
    protected void loadObjectsToGPU() {
        GLESObject glObject;
        while (loadQueue.size() > 0) {
            glObject = loadQueue.poll();
            if (glObject!=null) {
                long startTime = System.currentTimeMillis();
                // Загружаем объект в видео память
                glObject.loadToGPU();
                loadedObjects.push(glObject);
                // Если загрузка объекта продилась больше LOAD_TIME_PER_FRAME
                // остальное загрузим на следующем кадре
                long elapsedTime = System.currentTimeMillis() - startTime;
                if (elapsedTime > LOAD_TIME_PER_FRAME) break;
            }
        }
    }


    protected void deleteObjectsFromGPU() {
        GLESObject glObject;
        while (loadedObjects.size() > 0) {
            glObject = loadedObjects.poll();
            if (glObject != null) {
                glObject.deleteFromGPU();
            }
        }
    }


    //------------------------------------------------------------------------------------------
    // Методы отрисовки кадра сцены
    //------------------------------------------------------------------------------------------
    /**
     * Отрисовка кадра сцены
     * @param gl контекст (не используется)
     */
    public void onDrawFrame(GL10 gl) {
        try {

            // Если выставлен флаг завершения игры - удалить объекты
            if (exitGameFlag) {
                deleteObjectsFromGPU();
                return;
            }

            // Если есть очередь "отложенных" загрузок, выполняем
            if (loadQueue.size() > 0) {
                loadObjectsToGPU();
                // Не рисуем сцену пока не загрузили все объекты
                return;
            }

            // Отрисовываем активную сцену
            renderScene();

        } catch (Exception e) {
            e.printStackTrace();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Вызывает метод отрисовки всех объектов сцены
     */
    protected void renderScene() {

        long renderStartTime = System.nanoTime(); // Время начала рендеринга

        //-------------------------------------------------------------------------------
        GameScene scene = sceneManager.getActiveScene();
        // Берём активную игровую сцену
        // удостоверимся, что MVP матрица (камера) не меняется во время отрисовки кадра
        // чтобы избежать отрисовки объектов на одном кадре под разными позициями камеры
        if (scene!=null) synchronized (camera.getCameraMatrix()) {
            // Начинаем пакетирование спрайтов
            BatchRender.beginBatching();
            // Вызывается отрисовка сцены
            scene.render(camera);
            // Отрисовка пользователского интерфейса
            scene.getSceneWidget().draw(camera);
            // Завершаем и отрисовываем собранные пакеты спрайтов
            BatchRender.finishBatching(camera);
        }

        //-------------------------------------------------------------------------------
        framesCounter++;
        long now = System.nanoTime();
        float renderTime = (now - renderStartTime) / 1_000_000.0f;
        totalRenderTime += renderTime;

        //-------------------------------------------------------------------------------
        // При 60 FPS время рендеринга должно быть до 16.6 мс/кадр
        // Если ренедеринг был выполнен быстрее 10 мс (оставляем запас на смену буфера),
        // то даём время отработать другим потокам и не тратить зря время CPU и батарею.
        //-------------------------------------------------------------------------------
        try {
            if (renderTime < 10)
                Thread.sleep(10 - (long)renderTime);
            else
                Thread.sleep(1);
        } catch (InterruptedException e) {
             e.printStackTrace();
        }

        if (now - fpsLastEvaluationTime > 1_000_000_000) {
            fpsLastEvaluationTime = now;
            fps = framesCounter;
            framesCounter = 0;
            averageRenderTime = totalRenderTime / fps;
            totalRenderTime = 0;
        }

    }

    /**
     * Возвращает среднее время отрисовки кадра в миллесекундах
     * @return среднее время отрисовки кадра в миллесекундах
     */
    public static int getRenderTime() {
        if (render==null) return 0;
        return (int) render.averageRenderTime;
    }

    /**
     * Возвращает фактическое количество отрисованных кадров в секунду
     * @return фактическое количество отрисованных кадров в секунду
     */
    public static int getFPS() {
        if (render==null) return 0;
        return render.fps;
    }

    public static void release() {
        if (render==null) return;
        render.exitGameFlag = true;
    }

    //-------------------------------------------------------------------------------------
    // Методы для упрощенной отрисовки текста и прямоугольников
    //-------------------------------------------------------------------------------------

    public static Camera getCamera() {
        return camera;
    }

    public static void clear() {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT
                | GLES20.GL_COLOR_BUFFER_BIT);
    }

    public static void drawText(CharSequence text, float x, float y, float scale) {
        if (render==null) return;
        render.textRender.draw(camera, text, x, y, scale);
    }

    public static void drawText(CharSequence text, float x, float y, float scale, AABB scissor) {
        if (render==null) return;
        render.textRender.draw(camera, text, x, y, scale, scissor);
    }


    public static void setTypeface(Typeface typeface) {
        if (render==null) return;
        render.textRender.setTypeface(typeface);
    }

    public static void setTypeface(String fontName) {
        if (render==null) return;
        render.textRender.setTypeface(fontName);
    }

    public static float getTextWidth(CharSequence text, float scale) {
        if (render==null) return 0;
        return render.textRender.getTextWidth(text, scale);
    }

    public static float getTextHeight(CharSequence text, float scale) {
        if (render==null) return 0;
        return render.textRender.getTextHeight(text, scale);
    }

    public static void setColor(int rgba) {
        setColor(((rgba >> 16 ) & 0xff) / 255.0f,
                ((rgba >>  8  ) & 0xff) / 255.0f,
                ((rgba        ) & 0xff) / 255.0f,
                ((rgba >> 24) & 0xff) / 255.0f);
    }

    public static void setColor(float r, float g, float b, float alpha) {
        if (render==null) return;
        render.rectangleRender.setColor(r, g, b, alpha);
        render.lineRender.setColor(r, g, b, alpha);
        render.textRender.setColor(r, g, b, alpha);
    }

    public static void setZOrder(int zOrder) {
        if (render==null) return;
        render.rectangleRender.setZOrder(zOrder);
        render.textRender.setZOrder(zOrder);
        render.lineRender.setZOrder(zOrder);
    }

    public static void setLineThickness(float w) {
        if (render==null) return;
        render.lineRender.setLineThickness(w);
    }

    public static void drawLine(float x1, float y1, float x2, float y2, AABB scissor) {
        if (render==null) return;
        render.lineRender.draw(camera, x1, y1, x2, y2, scissor);
    }

    public static void drawLine(float x1, float y1, float x2, float y2) {
        if (render==null) return;
        render.lineRender.draw(camera, x1, y1, x2, y2, null);
    }

    public static void drawRectangle(float x, float y, float width, float height, AABB scissor) {
        if (render==null) return;
        render.rectangleRender.draw(camera,x,y,width,height, scissor);
    }

    public static void drawRectangle(float x, float y, float width, float height) {
        if (render==null) return;
        render.rectangleRender.draw(camera,x,y,width,height, null);
    }

    public static void drawRectangle(AABB rect, AABB scissor) {
        if (render==null) return;
        render.rectangleRender.draw(camera,rect,scissor);
    }


    //-------------------------------------------------------------------------------------------
    // Конвертация цветов
    //-------------------------------------------------------------------------------------------

    public static void colorIntToFloat(int rgba, float[] color) {
        color[3] = ((rgba >> 24) & 0xff) / 255.0f;
        color[0] = ((rgba >> 16) & 0xff) / 255.0f;
        color[1] = ((rgba >>  8) & 0xff) / 255.0f;
        color[2] = ((rgba      ) & 0xff) / 255.0f;
    }


    public static int colorFloatToInt(float[] color) {
        return  ((int)(color[3] * 255.0f) & 0xff) << 24 |
                ((int)(color[0] * 255.0f) & 0xff) << 16 |
                ((int)(color[1] * 255.0f) & 0xff) << 8 |
                ((int)(color[2] * 255.0f) & 0xff);
    }

}
