package com.basheyev.atom.engine.ui.widgets;

import android.view.MotionEvent;

import com.basheyev.atom.engine.core.geometry.AABB;
import com.basheyev.atom.engine.graphics.GraphicsRender;
import com.basheyev.atom.engine.graphics.gles2d.Camera;


/**
 * Виджет панель
 */
public class Panel extends Widget {

    protected boolean passEventsToUnderlyingWidget = false;

    public Panel() {
        super();
    }


    @Override
    public void draw(Camera camera) {
        if (parent==null || !visible) return;

        if (opaque) {
            GraphicsRender.setZOrder(zOrder);
            GraphicsRender.setColor(color[0], color[1], color[2], color[3]);
            AABB bounds = getWorldBounds();
            GraphicsRender.drawRectangle(bounds, scissors ? parent.getScissors() : null);
        }

        super.draw(camera);
    }


    @Override
    protected int getRenderLayersCount() {
        return 1;
    }

    /**
     * Флаг определяющий передавать ли события ввода нижележащим виджетам
     * @param pass
     */
    public void setInputTransparent(boolean pass) {
        passEventsToUnderlyingWidget = pass;
    }


    @Override
    public boolean onMotionEvent(MotionEvent event, float worldX, float worldY) {
        // Проверить обработали ли события дочерние виджеьы
        boolean isHandled = super.onMotionEvent(event, worldX, worldY);
        // Если дочерние виджеты не обработали и мы прозрачны для ввода - false
        // Если же дочерний виджеты обработали или не прозрачны для ввода - true
        return !passEventsToUnderlyingWidget | isHandled;
    }
}
