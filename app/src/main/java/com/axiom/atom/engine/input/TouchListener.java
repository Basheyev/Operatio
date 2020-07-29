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
        if (event.getActionMasked()==MotionEvent.ACTION_UP) v.performClick(); // Потому что warning
        Input.handleVirtualJoystick(event);      // Вызываем обработчик Touch Joystick
        // Fixme будет жрать память, но надо придумать механизм
        GameLoop.inputEventQueue.add(MotionEvent.obtain(event));     // Добавляем в очередь событий игрового цикла
        return true;
    }

}