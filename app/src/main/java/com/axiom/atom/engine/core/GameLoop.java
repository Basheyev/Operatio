package com.axiom.atom.engine.core;

import android.util.Log;
import android.view.MotionEvent;

import com.axiom.atom.engine.data.events.GameEvent;
import com.axiom.atom.engine.data.events.GameEventQueue;
import com.axiom.atom.engine.data.events.GameEventSubscriber;
import com.axiom.atom.engine.data.structures.Channel;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.input.ScaleEvent;


/**
 * Ядро движка выполняющее главный игровой цикл и вызывающий методы GameScene
 * с периодичностью стремящейся к 60 вызовов в секунду.
 * <br><br>
 * (С) Atom Engine, Bolat Basheyev 2020
 */
public class GameLoop extends Thread {

    public static final int INPUT_EVENTS_BUFFER_SIZE = 128;

    private static GameLoop gameLoop;

    protected GameView gameView;
    protected SceneManager sceneManager;
    protected boolean running;
    protected float deltaTime;
    protected long lastCycleTime = 0;

    //--------------------------------------------------------------------------------
    // Очередь событий ввода от пользователя (дополняется из UIThread)
    //--------------------------------------------------------------------------------
    private final Channel<Object> inputEventQueue;
    private final GameEventQueue gameEvents;

    /**
     * Возвращает единственный экземпляр потока игрового цикла (Singleton)
     * @param view экземпляр GameView
     * @param sceneManager экземпляр SceneManager
     * @return единственный экземпляр потока игрового цикла (Singleton)
     */
    public static GameLoop getInstance(GameView view, SceneManager sceneManager) {
        if (gameLoop==null) gameLoop = new GameLoop(view, sceneManager);
        return gameLoop;
    }

    public static GameLoop getInstance() {
        return gameLoop;
    }

    /**
     * Конструктор игрового цикла
     * @param view панель отрисовки
     * @param sceneManager менеджер цвен
     */
    private GameLoop(GameView view, SceneManager sceneManager)  {
        super("GameLoop");
        this.gameView = view;
        this.sceneManager = sceneManager;
        inputEventQueue = new Channel<>(INPUT_EVENTS_BUFFER_SIZE);
        gameEvents = new GameEventQueue();
    }



    /**
     * Основной цикл игры
     */
    public void run() {
        long startTime = System.nanoTime();
        long now, waitTime;
        running = true;

        lastCycleTime = System.nanoTime();

        while (running) {

            // Вызываем обработку сцены
            try {
                gameCycle();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                break;
            }

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
        // Вызываем обработчик игровых событий
        //------------------------------------------------------------------------------------------
        gameEvents.processEvent();

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
        deltaTime = (System.nanoTime() - lastCycleTime) / 1_000_000_000f;
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
            Object event = inputEventQueue.poll(); // Берем очередное событие ввода
            if (event!=null) {
                try {
                    if (event instanceof MotionEvent) processMotionEvent(scene, (MotionEvent) event);
                    if (event instanceof ScaleEvent) processScaleEvent(scene, (ScaleEvent) event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void processMotionEvent(GameScene scene, MotionEvent event) {
        Camera camera = GraphicsRender.getCamera();
        // Вычисляем координаты игрового мира чтобы передать обработчику
        float worldX = camera.convertScreenToWorldX(event.getX());
        float worldY = camera.convertScreenToWorldY(event.getY());

        // Доставляем события корневому виджету сцены
        boolean eventHandled = scene.getSceneWidget().onMotionEvent(event, worldX, worldY);

        // Вызываем обработчик события игровой сцены
        if (!eventHandled) scene.onMotion(event, worldX, worldY);
    }


    private void processScaleEvent(GameScene scene, ScaleEvent event) {
        Camera camera = GraphicsRender.getCamera();
        // Вычисляем координаты игрового мира чтобы передать обработчику
        float worldX = camera.convertScreenToWorldX(event.focusX);
        float worldY = camera.convertScreenToWorldY(event.focusY);

        // Доставляем события корневому виджету сцены
        boolean eventHandled = scene.getSceneWidget().onScaleEvent(event, worldX, worldY);

        // Вызываем обработчик события игровой сцены
        if (!eventHandled) scene.onScale(event, worldX, worldY);

    }

    public Channel<Object> getInputEventQueue() {
        return inputEventQueue;
    }

    public GameEventQueue getGameEvents() {
        return gameEvents;
    }


    public void fireGameEvent(GameEvent event) {
        gameEvents.push(event);
    }

    public void addGameEventSubscriber(GameEventSubscriber subscriber) {
        gameEvents.addSubscriber(subscriber, 0);
    }

    public void addGameEventSubscriber(GameEventSubscriber subscriber, int topic) {
        gameEvents.addSubscriber(subscriber, topic);
    }

    public void removeGameEventSubscriber(GameEventSubscriber subscriber) {
        gameEvents.removeSubscriber(subscriber);
    }

}
