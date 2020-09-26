package com.axiom.atom.engine.graphics;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameView;
import com.axiom.atom.engine.core.GameObject;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.data.Channel;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.gles2d.GLESObject;
import com.axiom.atom.engine.graphics.renderers.Rectangle;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.graphics.renderers.BatchRender;
import com.axiom.atom.engine.graphics.renderers.Text;
import com.axiom.atom.engine.core.geometry.AABB;

import java.util.ArrayList;

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
    public static final int LAZY_LOAD_QUEUE_LENGTH = 1024;     // Максимальная длина очереди
    protected static Channel<GLESObject> loadQueue;            // Очередь "ленивой" загрузки

    //-------------------------------------------------------------------------------------
    // Основные объекты графического рендера
    //-------------------------------------------------------------------------------------
    protected static GraphicsRender render;            // Наш единственный рендер
    protected static Camera camera;                    // Единственная камера игровго мира
    protected GameView gameView;                       // Наш View на котором делаем рендер
    protected SceneManager sceneManager;               // Менеджер игровых сцен
    //-------------------------------------------------------------------------------------
    private Text textRender;                           // Рендер текста
    private Rectangle rectangleRender;                 // Рендер прямоугольников
    //-------------------------------------------------------------------------------------
    private int framesCounter = 0;                     // Счётчик отрисованных кадров
    private int fps = 0;                               // Количество кадров в секунду (FPS)
    private long totalRenderTime = 0;                  // Общее время рендеринга в секунду
    private long averageRenderTime;                    // Среднее время рендеринга одного кадра
    private long fpsLastEvaluationTime = 0;            // Последнее время расчёта FPS (нс)


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
        camera = Camera.getInstance(gameView);
    }


    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        textRender = new Text(new Sprite(gameView.getResources(), R.drawable.font, 15,8), 0.75f);
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
        GraphicsRender.loadQueue.add(obj);
    }

    /**
     * Отложенная загрузка программ, шейдеров и текстур в GPU
     */
    protected void performLazyLoadToGPU() {
        GLESObject task;
        long startTime = System.currentTimeMillis();
        int tasksAmount = loadQueue.size();

        while (loadQueue.size() > 0) {
        //if (loadQueue.size() > 0) {
            task = loadQueue.poll();
            if (task!=null) {
                task.loadObjectToGPU();
                Log.i("LOAD TO GPU", task.toString());
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
            // Если есть очередь "отложенных" загрузок, выполняем
            if (loadQueue.size() > 0) performLazyLoadToGPU();
            // Отрисовываем активную сцену
            renderScene();
        } catch (Exception e) {
            e.printStackTrace();
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

            // Вызывается до отрисовки сцены
            scene.preRender(camera);

            // Отрисовка сцены
            // Берём объекты игровой сцены
            ArrayList<GameObject> objects = scene.getSceneObjects();
            GameObject obj;
            for (int i=0; i < objects.size(); i++) {
                obj = objects.get(i);
                if (obj.active) {
                    obj.draw(camera);
                }
            }

            // Вызывается после отрисовки сцены
            scene.postRender(camera);

            // Отрисовка пользователского интерфйса
            // Вызывается после всего рендеринга сцены
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

    //-------------------------------------------------------------------------------------
    // Методы для упрощенной отрисовки текста и прямоугольников
    //-------------------------------------------------------------------------------------

    public static Camera getCamera() {
        return camera;
    }

    public static void clear() {
        GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
    }

    public static void drawText(String text, float x, float y, float scale) {
        if (render==null) return;
        render.textRender.draw(camera, text, x, y, scale);
    }

    public static void drawText(StringBuffer text, float x, float y, float scale, AABB scissor) {
        if (render==null) return;
        render.textRender.draw(camera, text, x, y, scale, scissor);
    }

    public static void drawText(StringBuffer text, float x, float y, float scale) {
        if (render==null) return;
        render.textRender.draw(camera, text, x, y, scale);
    }

    public static void drawText(String text, float x, float y, float scale, AABB scissor) {
        if (render==null) return;
        render.textRender.draw(camera, text, x, y, scale, scissor);
    }

    public static float getTextWidth(String text, float scale) {
        if (render==null) return 0;
        return render.textRender.getTextWidth(text, scale);
    }

    public static void setColor(float r, float g, float b, float alpha) {
        if (render==null) return;
        render.rectangleRender.setColor(r, g, b, alpha);
    }

    public static void setZOrder(int zOrder) {
        if (render==null) return;
        render.rectangleRender.zOrder = zOrder;
        render.textRender.zOrder = zOrder;
    }

    public static void drawRectangle(float x, float y, float width, float height, AABB scissor) {
        if (render==null) return;
        render.rectangleRender.draw(camera,x,y,width,height, scissor);
    }

    public static void drawRectangle(AABB rect, AABB scissor) {
        if (render==null) return;
        render.rectangleRender.draw(camera,rect,scissor);
    }

}
