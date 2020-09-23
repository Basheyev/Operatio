package com.axiom.atom.engine.input;

import android.view.MotionEvent;
import android.view.View;

import com.axiom.atom.engine.core.GameLoop;

/**
 * Захватывает и обрабатываем пользовательский ввод, передаем события в игровой цикл
 *
 * (C) Atom Engine, Bolat Basheyev 2020
 */
public class TouchListener implements View.OnTouchListener {

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // Потому что warning
        if (event.getActionMasked()==MotionEvent.ACTION_UP) v.performClick();
        // Вызываем обработчик Touch Joystick если включен
        if (Input.enabled) Input.handleVirtualJoystick(event);
        // Добавляем копию события в очередь событий игрового цикла
        // Так как event используется как единственный экземпляр
        GameLoop.inputEventQueue.add(MotionEvent.obtain(event));
        return true;
    }

}