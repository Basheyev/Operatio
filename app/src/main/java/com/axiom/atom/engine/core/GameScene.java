package com.axiom.atom.engine.core;

import android.content.res.Resources;
import android.view.DragEvent;
import android.view.MotionEvent;

import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.input.ScaleEvent;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;

import java.util.ArrayList;


/**
 * Основной класс игры с которым работает игровой ицкл, графика и ввод
 * (C) Atom Engine, Bolat Basheyev 2020
 */
public abstract class GameScene {

    protected SceneManager sceneManager;
    protected Resources resources;
    public boolean started = false;

    protected Widget sceneWidget = new Widget() { };  // Корневой виджет сцены

    public abstract String getSceneName();
    public abstract void startScene();                // Вызывается при запуске сцены
    public abstract void disposeScene();              // Вызывается из потока GameLoop
    public abstract void changeScene();               // Вызывается при смене сцены на другую

    //----------------------------------------------------------------------------------------
    // Важно: избегайте создание объектов в этих методах, так они вызываются 60 раз в секунду
    // это будет вызывать задержки при работе Garbage Collector (сборщика мусора
    //----------------------------------------------------------------------------------------
    public abstract void updateScene(float deltaTime);   // Вызывается из потока GameLoop
    public abstract void render(Camera camera);          // Вызывается из потока GLThread
    public abstract void onMotion(MotionEvent event, float worldX, float worldY); // GameLoop
    public void onScale(ScaleEvent event, float worldX, float worldY) {}

    public Widget getSceneWidget() {
       return sceneWidget;
    }

    public Resources getResources() {
        return resources;
    }

    public SceneManager getSceneManager() { return sceneManager; }

}
