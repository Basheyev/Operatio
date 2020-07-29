package com.axiom.atom.engine.core;

import android.util.Log;
import android.view.MotionEvent;

import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;

import java.util.concurrent.ArrayBlockingQueue;


/**
 * Ядро движка выполняющее главный игровой цикл и вызывающий методы GameScene
 * с периодичностью стремящейся к 60 вызовов в секунду.
 * <br><br>
 * (С) Atom Engine, Bolat Basheyev 2020
 */
public class GameLoop extends Thread {

    public static final int INPUT_EVENTS_BUFFER_SIZE = 128;

    protected GameView gameView;
    protected SceneManager sceneManager;
    protected boolean running;
    protected float deltaTime;
    protected long lastCycleTime = 0;

    //--------------------------------------------------------------------------------
    // Очередь событий ввода от пользователя (дополняется из UIThread)
    //--------------------------------------------------------------------------------
    public static ArrayBlockingQueue<MotionEvent> inputEventQueue;

    /**
     * Конструктор игрового цикла
     * @param view
     * @param sceneManager
     */
    public GameLoop(GameView view, SceneManager sceneManager)  {
        super("GameLoop");
        this.gameView = view;
        this.sceneManager = sceneManager;
        inputEventQueue = new ArrayBlockingQueue<MotionEvent>(INPUT_EVENTS_BUFFER_SIZE);
    }

    /**
     * Основной цикл игры
     */
    public void run() {
        long startTime = System.nanoTime();
        long now, waitTime;
        running = true;

        lastCycleTime = System.nanoTime();

        while(running)  {

            // Вызываем обработку сцены
            gameCycle();

            if (sceneManager.isGameFinished()) setRunning(false);

            now = System.nanoTime();
            waitTime = (now - startTime) / 1000000;

            if (waitTime < 10)  {
                try {
                    sleep(5);
                } catch(InterruptedException e)  {
                    Log.e("GAME LOOP", "Thread interrupted", e);
                }
            }
            startTime = System.nanoTime();
        }
        gameView.exitGame();
    }

    public void setRunning(boolean running)  {
        this.running = running;
    }


    /**
     * Шаг игрового цикла
     * Вызывает метод оновления всех активных объектов сцены.
     * Затем вызывает метод обновления сцены
     */
    protected void gameCycle() {

        GameScene scene = sceneManager.getActiveScene();
        if (scene==null) return;

        //------------------------------------------------------------------------------------------
        // Вызываем обработчик событий ввода
        //------------------------------------------------------------------------------------------
        processInputEvent(scene);

        //------------------------------------------------------------------------------------------
        // Вызываем обработчик каждого активного объекта сцены
        //------------------------------------------------------------------------------------------
        GameObject obj;
        for (int i=0; i< scene.objects.size(); i++) {        // Не используем итераторы
            obj = scene.objects.get(i);                      // Для каждого объекты сцены
            if (obj.active) obj.update(deltaTime);           // Вывызваем обработчик объекта
        }

        //------------------------------------------------------------------------------------------
        // Удаляем помеченные объекты и добавляем новые по списку вне цикла обработчика
        //------------------------------------------------------------------------------------------
        GameObject deleted;
        for (int i=0; i< scene.deletedObjects.size(); i++) {  // Не используем итераторы
            deleted = scene.deletedObjects.get(i);            // Удаляем объекты по списку
            scene.objects.remove(deleted);
        }

        if (scene.addedObjects.size()>0)                      // Если были добавлены объекты
        scene.objects.addAll(scene.addedObjects);             // Добавляем объекты по списку

        //------------------------------------------------------------------------------------------
        // Очистить списки добавленных и удаленных объектов
        //------------------------------------------------------------------------------------------
        scene.addedObjects.clear();
        scene.deletedObjects.clear();

        //------------------------------------------------------------------------------------------
        // Вызываем обновление сцены
        //------------------------------------------------------------------------------------------
        deltaTime = (System.nanoTime() - lastCycleTime) / 1000000000f;
        scene.updateScene(deltaTime);
        lastCycleTime = System.nanoTime();

    }


    /**
     * Обработчик событий ввода с переводом экранных координат в координаты игрового мира
     * @param scene игровая сцена в обработчик которой нужно доставить события
     */
    private void processInputEvent(GameScene scene) {
        // Если есть события ввода
        while (inputEventQueue.size() > 0) {
            MotionEvent event = inputEventQueue.poll(); // Берем очередное событие ввода
            if (event!=null) {
                // Вычисляем нормированные экранные координаты (0-1)
                // и переворачиваем координату Y (экран/GLES)
                Camera camera = GraphicsRender.getCamera();
                float worldX = event.getX() / (float) gameView.getWidth();
                float worldY = 1 - (event.getY() / (float) gameView.getHeight());

                worldX *= Camera.SCREEN_WIDTH;          // переводим в логические координаты камеры
                worldY *= Camera.SCREEN_HEIGHT;         // переводим в логические координаты камеры

                worldX += camera.x1;     // добавляем горизонтальное смещение камеры
                worldY += camera.y1;     // добавляем вертикальное смещение камеры

                scene.onMotion(event, worldX, worldY);  // Вызываем обработчик события игровой сцены
            }
        }
    }


}
