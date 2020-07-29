package com.axiom.atom.engine.core;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;

import androidx.appcompat.app.AppCompatActivity;

import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.input.Input;
import com.axiom.atom.engine.input.TouchListener;


/**
 * Отображение рендеринга OpenGL
 * <br><br>
 * С) Atom Engine, Bolat Basheyev 2020
 */
public class GameView extends GLSurfaceView {

    protected GameLoop gameLoop;
    protected SceneManager sceneManager;
    protected GraphicsRender renderer;

    public GameView(Context context, GameScene gameScene) {
        super(context);
        initializeGameEngine(gameScene, context);
    }

    /**
     * Инициализирует и запускает движок с указанной сценой
     * @param gameScene игровая сцена
     */
    private void initializeGameEngine(GameScene gameScene, Context context) {
        //------------------------------------------------------------------------------------
        // Инициализируем OpenGL ES 2.0
        //------------------------------------------------------------------------------------
        setEGLContextClientVersion(2);
        setPreserveEGLContextOnPause(true);
        //------------------------------------------------------------------------------------
        // Инициализация ввода
        //------------------------------------------------------------------------------------
        setOnTouchListener(new TouchListener());    // Добавляем обработчик событий ввода
        Input.initialize(context);                  // Инициализируем обработчик джойстика
        //------------------------------------------------------------------------------------
        // Создаём менеджер сцен и передаем доступ к ресурсам
        //------------------------------------------------------------------------------------
        sceneManager = new SceneManager(getResources());
        //------------------------------------------------------------------------------------
        // Запускаем поток графического рендера
        //------------------------------------------------------------------------------------
        renderer = new GraphicsRender(this, sceneManager);
        setRenderer(renderer);
        //------------------------------------------------------------------------------------
        // Запускаем поток основного цикла игры
        //------------------------------------------------------------------------------------
        gameLoop = new GameLoop(this, sceneManager);
        gameLoop.start();
        //------------------------------------------------------------------------------------
        // Запускаем сцену
        //------------------------------------------------------------------------------------
        sceneManager.addGameScene(gameScene);;
        sceneManager.setActiveScene(gameScene.getSceneName());
    }

    public void exitGame() {
        //-----------------------------------------------------------------------------------
        // Закрываем нашу активность и все её дочерние активности (освобождаем GL Context)
        //-----------------------------------------------------------------------------------
        AppCompatActivity mainActivity = (AppCompatActivity) getContext();
        mainActivity.finishAffinity();
        System.runFinalization();
        System.exit(0);
    }

}
