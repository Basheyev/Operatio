package com.axiom.atom.engine.ui.widgets;

import android.view.MotionEvent;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;


/**
 * Виджет панель
 */
public class Panel extends Widget {

    protected boolean passEventsToUnderlayingWidget = false;

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


    public void setInputTransparent(boolean pass) {
        passEventsToUnderlayingWidget = pass;
    }


    @Override
    public boolean onMotionEvent(MotionEvent event, float worldX, float worldY) {
        super.onMotionEvent(event, worldX, worldY);
        return !passEventsToUnderlayingWidget;
    }
}
