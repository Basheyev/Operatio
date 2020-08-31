package com.axiom.atom.engine.graphics.ui;

import android.view.MotionEvent;

import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;

public class Button extends Widget {

    protected Sprite image;
    protected char[] text;

    @Override
    public void draw(Camera camera) {

        // Отрисовать дочерние виджеты
        super.draw(camera);

    }

    @Override
    public boolean onMotionEvent(MotionEvent event, float worldX, float worldY) {
        // Доставляет события дочерним виджетам
        super.onMotionEvent(event, worldX, worldY);

        return false;
    }


}
