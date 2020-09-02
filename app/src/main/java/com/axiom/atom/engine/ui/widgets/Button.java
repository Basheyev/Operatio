package com.axiom.atom.engine.ui.widgets;

import android.view.MotionEvent;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.ui.listeners.ClickListener;

public class Button extends Widget {

    protected Sprite image;
    protected char[] text;

    public Button(String text) {
        super();
        this.text = text.toCharArray();
    }

    public Button(Sprite image) {
        super();
        this.image = image;
    }

    @Override
    public void draw(Camera camera) {
        AABB clippingArea = getScreenClippingAABB();
        if (clippingArea==null) return;

        AABB bnds = getWorldBounds();

        GraphicsRender.setZOrder(zOrder);

        if (opaque) {
            GraphicsRender.setColor(color[0], color[1], color[2], color[3]);
            GraphicsRender.drawRectangle(bnds, clippingArea);
        }

        if (image!=null) {
            image.zOrder = zOrder + 1;
            image.draw(camera, bnds.min.x, bnds.min.y, bnds.width, bnds.height, clippingArea);
        }

        if (text!=null) {
            GraphicsRender.drawText(text, bnds.center.x, bnds.center.y, 1, clippingArea);
        }

        super.draw(camera);
    }


}
