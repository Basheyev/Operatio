package com.axiom.atom.engine.ui.widgets;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;

public class Panel extends Widget {


    public Panel() {
        super();
        setColor(0.9f, 0.9f, 0.9f, 0.8f);
    }

    @Override
    public void draw(Camera camera) {
        if (parent==null || !visible) return;

        if (opaque) {
            GraphicsRender.setZOrder(zOrder);
            GraphicsRender.setColor(color[0], color[1], color[2], color[3]);
            AABB bnds = getWorldBounds();
            GraphicsRender.drawRectangle(bnds, parent.getScissors());
        }

        super.draw(camera);
    }

}
