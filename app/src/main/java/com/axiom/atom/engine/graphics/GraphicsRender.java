package com.axiom.atom.engine.graphics;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameView;
import com.axiom.atom.engine.core.GameObject;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.gles2d.GLObject;
import com.axiom.atom.engine.graphics.renderers.Rectangle;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.graphics.renderers.Batcher;
import com.axiom.atom.engine.graphics.renderers.Text;
import com.axiom.atom.engine.physics.geometry.AABB;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;



/**
 *  Графический рендер - отрисовывает всю графику
 *  // TODO Навести порядок в этом классе
 *  (C) Atom Engine, Bolat Basheyev 2020
 */
public class GraphicsRender implements GLSurfaceView.Renderer {

    //-------------------------------------------------------------------------------------
    // Список объектов которые должны быть инициализированы в потоке/контексте OpenGL
    //-------------------------------------------------------------------------------------
    public static Camera camera;
    public static ArrayBlockingQueue<GLObject> glTasksQueue;  // Список "ленивой" загрузки
    public static GraphicsRender render;

    protected GameView gameView;
    protected SceneManager sceneManager;

    private int frames = 0, fps;
    private long totalRenderTime;
    private long lasttime = 0;

    private Text textRender;
    private Rectangle rectangleRender;

    public GraphicsRender(GameView gameView, SceneManager sceneManager) {
        super();
        this.gameView = gameView;
        this.sceneManager = sceneManager;
        // FIXME Не знаю какой длины должна быть очередь между UITHread, GameLoop, GLThread
        glTasksQueue = new ArrayBlockingQueue<GLObject>(1000);
        camera = new Camera();
        // FIXME статическая переменная инициализируется в конструкторе - не хорошо
        render = this;
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        textRender = new Text(new Sprite(gameView.getResources(), R.drawable.font, 15,8), 0.75f);
        rectangleRender = new Rectangle();
        Log.d("INFO:", "OpenGLES renderer thread created");
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glClearColor(0, 0, 0, 1.0f);
    }

    /**
     * Отрисовка кадра
     * @param gl
     */
    public void onDrawFrame(GL10 gl) {
        if (glTasksQueue.size()>0) lazyloader();
        renderSceneObjects();
    }

    /**
     * Отложенная загрузка шейдеров, программ и текстур
     * TODO Можно реализовать лучше, чтобы не зависала отрисовка
     */
    protected void lazyloader() {
        GLObject task;
        long startTime = System.currentTimeMillis();
        int tasksAmount = glTasksQueue.size();
        while (glTasksQueue.size() > 0) {
               task = glTasksQueue.poll();
               Log.i("LAZY TASK", task.toString());
               if (task!=null) task.initializeOnGLThread();
        }
        Log.i ("SCENE LAZY LOADER", tasksAmount + " tasks done by " +
                (System.currentTimeMillis() - startTime) + "ms");
    }

    /**
     * Вызывает метод отрисовки всех объектов сцены
     * @return
     */
    protected void renderSceneObjects() {
        long renderStartTime = System.nanoTime();

        //-------------------------------------------------------------------------------
        GameScene scene = sceneManager.getActiveScene();
        // Берём активную игровую сцену
        // удостоверимся, что MVP матрица (камера) не меняется во время отрисовки кадра
        // чтобы избежать отрисовки объектов на одном кадре под разными позициями камеры
        if (scene!=null) synchronized (camera.getCameraMatrix()) {

            // Начинаем пакетирование спрайтов
            Batcher.beginBatching();

            // Вызывается до отрисовки сцены
            scene.preRender(camera);
            // Отрисовка сцены
            ArrayList<GameObject> objects = scene.getSceneObjects();  // Берём объекты игровой сцены
            GameObject obj;


            for (int i=0; i < objects.size(); i++) {
                obj = objects.get(i);
                if (obj.active) {
                    obj.draw(camera);
                }
            }

            // Вызывается после отрисовки сцены
            scene.postRender(camera);

            // Завершаем и отрисовываем собранные пакеты спрайтов
            Batcher.finishBatching(camera);

        }

        //-------------------------------------------------------------------------------
        frames++;
        long now = System.nanoTime();
        float renderTime = (now - renderStartTime) / 1000000.0f;
        totalRenderTime += renderTime;

        //-------------------------------------------------------------------------------
        try {
             long sleepTime = (long) (10 - renderTime);
             if (sleepTime <=0) sleepTime = 1;
             Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
             e.printStackTrace();
        }


        if (now - lasttime > 1000000000) {
            lasttime = now;
            fps = frames;
            frames = 0;
            totalRenderTime /= fps;
            totalRenderTime = 0;
        }

    }

    public static int getFPS() {
        if (render==null) return 0;
        return render.fps;
    }

    //-------------------------------------------------------------------------------------
    // Методы для рендеринга текста и прямоугольников
    //-------------------------------------------------------------------------------------

    public static void clear() {
        GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
    }

    public static void drawText(char[] text, float x, float y, float scale) {
        if (render==null) return;
        render.textRender.draw(camera, text, x, y, scale);
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

    public static void drawRectangle(float x, float y, float width, float height) {
        if (render==null) return;
        render.rectangleRender.draw(camera,x,y,width,height);
    }

    public static void drawRectangle(AABB rect) {
        if (render==null) return;
        render.rectangleRender.draw(camera,rect);
    }

}
