package com.axiom.operatio.scenes.production.view;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.ui.widgets.Button;

public class ItemWidget extends Button {


    public ItemWidget(String text) {
        super(text);
    }

    @Override
    public void draw(Camera camera) {
        if (parent==null || !visible) return;
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
            float textWidth = GraphicsRender.getTextWidth(text, textScale);
            float textHeight = GraphicsRender.getTextHeight(text,textScale);
            GraphicsRender.setColor(textColor[0], textColor[1], textColor[2], textColor[3]);
            GraphicsRender.drawText(text, bounds.max.x - textWidth - 1, bounds.min.y + 1, textScale, null);
        }

    }
}
