package com.axiom.atom.engine.core;

import android.content.res.Resources;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * <b>Менеджер игровых сцен</b><br>
 * Является контейнером сцен для ядра движка.<br>
 * Осуществляет инициализацию и переключение между сценами.
 *
 * (C) Atom Engine, Bolat Basheyev 2020
 *
 */
public class SceneManager {

    private static SceneManager sceneManager;

    protected HashMap<String, GameScene> scenes;
    protected GameScene activeGameScene = null;
    protected Resources resources;
    protected boolean exitGame;


    /**
     * Отдаёт единственный экземпляр SceneManager (Singleton)
     * @param res ресурсы (чтобы сцены имели доступ к ним)
     * @return единственный экземпляр SceneManager (Singleton)
     */
    public static SceneManager getInstance(Resources res) {
        if (sceneManager==null) sceneManager = new SceneManager(res);
        return sceneManager;
    }


    /**
     * Конструктор менеджера сцен<br>
     */
    private SceneManager(Resources res) {
        scenes = new HashMap<String, GameScene>();
        exitGame = false;
        resources = res;
    }

    /**
     * Добавить игровую сцену в Менеджер игровых сцен
     * @param scn добавляемая игровая сцена
     */
    public void addGameScene(GameScene scn) {
        scenes.put(scn.getSceneName(), scn);
        scn.sceneManager = this;
        scn.resources = resources;
    }

    /**
     * Удалить игровую сцену из Менеджера игровых сцен<br>
     * Вызвать метод dispose() сцены для освобождения ресурсов
     * @param name название удаляемой игровой сцены
     */
    public void removeGameScene(String name) {
        GameScene scene = scenes.get(name);
        if (scene==null) return;
        if (scene==activeGameScene) activeGameScene = null;
        scene.disposeScene();
        scene.getSceneObjects().clear();
        scenes.remove(name);
    }

    /**
     * Установить активую игровую сцену по названию (ключу)
     * @param name название игровой сцены
     * @return true - если сцену получилось сделать активной и false - если такая сцена не найдена
     */
    public boolean setActiveScene(String name) {
        GameScene gameScene = scenes.get(name);
        if (gameScene ==null) {
            Log.e("SCENE MANAGER", "Can not set active scene \"" + name + "\"!");
            return false;
        }
        Log.i("SCENE MANAGER", "Changing scene to \"" + gameScene.getSceneName() + "\"");

        gameScene.sceneManager = this;
        gameScene.resources = resources;
        gameScene.startScene();
        gameScene.started = true;
        activeGameScene = gameScene;
        return true;
    }

    /**
     * Получить текущую активную игровую сцену
     * @return игровая сцена
     */
    public GameScene getActiveScene() {
        return activeGameScene;
    }


    /**
     * Установить флаг завершения работы игры<br>
     * Вызывает метод удаления сцены для каждой сцены
     */
    public void exitGame() {
        String key;
        for (Map.Entry<String,GameScene> entry:scenes.entrySet()) {
            key = entry.getKey();
            removeGameScene(key);
        }
        this.exitGame = true;
    }

    /**
     * Вернуть значение флага заверщения работы
     * @return true - если игра завершена, false - если нет.
     */
    public boolean isGameFinished() {
        return exitGame;
    }


    /**
     * Возвращает ссылку на ресурсы приложения
     * @return ссылка на ресурсы приложения
     */
    public static Resources getResources() {
        if (sceneManager==null) return null;
        return sceneManager.resources;
    }

}
