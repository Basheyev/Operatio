package com.axiom.atom.engine.input;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;

/**
 * Имитирует на экране джойстик: оси XY и кнопки A,B (multitouch)
 * Оси (любые зажим-движение) в любой части левой половины экрана
 * Кнопка B (c 0.6-0.8 по оси X)
 * Кнопка A (c 0.8-1.0 по оси X)
 *   ________________________________________
 *  |                  |                    |
 *  |        |         |                    |
 *  |    --- O ---     |    ----------------|
 *  |        |         |   |    B   |   A   |
 *  |__________________|___|________|_______|
 *
 * (C) Atom Engine, Bolat Basheyev 2020
 */
public class Input {
    //-------------------------------------------------------------------------------------
    public static float xAxis = 0.0f;
    public static float yAxis = 0.0f;
    public static boolean AButton = false;
    public static boolean BButton = false;
    //-------------------------------------------------------------------------------------
    private static float screenResolutionX;
    private static float screenResolutionY;
    private static float motionStartX, motionCurrentX;
    private static float motionStartY, motionCurrentY;
    private static boolean axisMotion = false;
    private static int motionPointerID = -1;
    private static int APointerID = -1;
    private static int BPointerID = -1;
    private static boolean initialized = false;
    //-------------------------------------------------------------------------------------

    /**
     * Инициализирует подсистему ввода, в частности, выясняет разрешение экрана
     * @param context
     */
    public static void initialize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenResolutionX = size.x;
        screenResolutionY = size.y;
        //----------------------------------
        initialized = true;
    }

    /**
     * Обработка событий ввода для симуляции джойстика на экране
     * @param event событие ввода
     */
    public static void handleVirtualJoystick(MotionEvent event) {
        if (!initialized) return;
        int index = event.getActionIndex();
        if (event.getX(index) / screenResolutionX < 0.5f) {
            handleAxisMotion(event, event.getPointerId(index), event.getX(index) , event.getY(index));
        } else {
            handleButtons(event, event.getPointerId(index), event.getX(index), event.getY(index));
        }
    }

    /**
     * Обработка движения пальца по осям на левой части экрана
     * @param event
     * @param ID
     * @param eventX
     * @param eventY
     */
    private static void handleAxisMotion(MotionEvent event, int ID, float eventX, float eventY) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN:
                motionStartX = eventX / screenResolutionX;
                motionStartY = eventY / screenResolutionY;
                motionCurrentX = motionStartX;
                motionCurrentY = motionStartY;
                motionPointerID = ID;
                axisMotion = true;
                break;
            case MotionEvent.ACTION_MOVE:
                evaluateXYAxis(event);
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                pointerIsUp(ID);
                break;
        }
    }

    /**
     * Обрабатывает нажатие кнопок A, B на правой нижней части экрана
     * @param event
     * @param eventX
     * @param eventY
     */
    private static void handleButtons(MotionEvent event, int ID, float eventX, float eventY) {
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN:
                if (event.getY() / screenResolutionY > 0.5f) {
                    float mX = eventX / screenResolutionX;
                    if (mX > 0.8) { AButton = true; APointerID = ID; }
                    else if (mX > 0.6) { BButton = true; BPointerID = ID; }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                evaluateXYAxis(event);
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                pointerIsUp(ID);
                break;
        }
    }

    /**
     * Вычисляет положение по осям X, Y
     * @param event
     */
    private static void evaluateXYAxis(MotionEvent event) {
        // Будем считать, что джойстик имеет площадь 10% экрана
        if (axisMotion) {
            for (int i=0; i < event.getPointerCount(); i++) {
                if (event.getPointerId(i)==motionPointerID) {
                    motionCurrentX = event.getX(i) / screenResolutionX;
                    motionCurrentY = event.getY(i) / screenResolutionY;
                }
            }
            xAxis = (motionCurrentX - motionStartX) / 0.10f;
            yAxis = (motionCurrentY - motionStartY) / 0.10f;
            if (xAxis<-1.0f) xAxis = -1.0f;
            if (xAxis>1.0f) xAxis = 1.0f;
            if (yAxis<-1.0f) yAxis = -1.0f;
            if (yAxis>1.0f) yAxis = 1.0f;
            yAxis *= -1; // перевернуть по Y - ведь экран сверх вниз, а GLES снизу верх
        }
    }

    /**
     * Обрабатывает событие когда поднимается один из пальцев
     * @param ID
     */
    private static void pointerIsUp(int ID) {
        if (ID==APointerID) {
            AButton = false;
            APointerID = -1;
        } else
        if (ID==BPointerID) {
            BButton = false;
            BPointerID = -1;
        } else
        if (ID==motionPointerID) {
            axisMotion = false;
            motionPointerID = -1;
            xAxis = 0;
            yAxis = 0;
        }
    }


}
