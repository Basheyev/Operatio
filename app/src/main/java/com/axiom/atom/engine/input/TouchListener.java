package com.axiom.atom.engine.input;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.axiom.atom.engine.core.GameLoop;

/**
 * Захватывает и обрабатываем пользовательский ввод, передаем события в игровой цикл
 *
 * (C) Atom Engine, Bolat Basheyev 2020
 */
public class TouchListener implements View.OnTouchListener {

    protected ScaleGestureDetector scaleDetector;
    protected boolean scaling = false;

    public TouchListener(Context context) {
        super();
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // Выявляем жест масштабирования (pinch)
        scaleDetector.onTouchEvent(event);
        // Потому что warning
        if (event.getActionMasked()==MotionEvent.ACTION_UP) v.performClick();
        // Вызываем обработчик Touch Joystick если включен
        if (Input.enabled) Input.handleVirtualJoystick(event);
        // Добавляем копию события в очередь событий игрового цикла
        // Так как event используется как единственный экземпляр
        if (!scaling) GameLoop.inputEventQueue.add(MotionEvent.obtain(event));
        return true;
    }


    public class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            GameLoop.inputEventQueue.add(new ScaleEvent(detector, true));
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            GameLoop.inputEventQueue.add(new ScaleEvent(detector, true));
            scaling = true;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            GameLoop.inputEventQueue.add(new ScaleEvent(detector, false));
            scaling = false;
        }
    }

}