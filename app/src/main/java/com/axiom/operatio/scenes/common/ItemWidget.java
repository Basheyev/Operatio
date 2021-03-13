package com.axiom.operatio.scenes.common;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.graphics.renderers.Text;
import com.axiom.atom.engine.ui.widgets.Button;

public class ItemWidget extends Button {

    private boolean active = true;

    public ItemWidget(String text) {
        super(text);
        textRenderer.setHorizontalAlignment(Text.ALIGN_RIGHT);
        textRenderer.setVerticalAlignment(Text.ALIGN_BOTTOM);
    }

    @Override
    public void draw(Camera camera) {
        if (parent==null || !visible) return;
        AABB bounds = getWorldBounds();
        AABB parentScissor = parent.getScissors();

        if (opaque) {
            GraphicsRender.setZOrder(zOrder);
            GraphicsRender.setColor(color[0], color[1], color[2], color[3]);
            GraphicsRender.drawRectangle(bounds, parentScissor);
        }

        if (!active) return;

        // Исключаем конкурентное изменение текста и изображения
        synchronized (this) {
            if (background != null) {
                background.setZOrder(zOrder + 1);
                background.draw(camera, bounds, parentScissor);
            }
            if (text != null) {
                textRenderer.setZOrder(zOrder + 2);
                textRenderer.setColor(textColor[0], textColor[1], textColor[2], textColor[3]);
                textRenderer.draw(camera, text, bounds.max.x - 2, bounds.min.y + 2, textScale, parentScissor);
            }
        }
    }

    @Override
    public void setText(String caption) {
        synchronized (this) {
            super.setText(caption);
        }
    }

    @Override
    public void setBackground(Sprite background) {
        synchronized (this) {
            super.setBackground(background);
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
