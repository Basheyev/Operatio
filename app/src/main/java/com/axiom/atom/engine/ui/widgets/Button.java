package com.axiom.atom.engine.ui.widgets;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;

public class Button extends Widget {

    protected Sprite background;
    protected char[] text;

    public Button(Sprite background, String text) {
        super();
        this.background = background;
        this.text = text!=null ? text.toCharArray() : null;
        setColor(0.5f, 0.7f, 0.5f, 0.9f);
    }

    public Button(String text) {
        this(null, text);
    }

    public Button(Sprite background) {
        this(background, null);
    }

    @Override
    public void draw(Camera camera) {
        if (parent==null) return;
        AABB bounds = getWorldBounds();
        AABB scissors = getScissors();
        AABB parentScissor = parent.getScissors();

        if (opaque) {
            GraphicsRender.setZOrder(zOrder);
            GraphicsRender.setColor(color[0], color[1], color[2], color[3]);
            GraphicsRender.drawRectangle(bounds, parentScissor);
        }

        if (background !=null) {
            background.zOrder = zOrder + 1;
            background.draw(camera, bounds, parentScissor);
        }

        if (text!=null) {
            GraphicsRender.setZOrder(zOrder + 2);
            float textWidth = GraphicsRender.getTextWidth(text, 1);
            GraphicsRender.drawText(text, bounds.center.x - textWidth/2, bounds.center.y, 1, scissors);
        }

        super.draw(camera);
    }


}
