package com.axiom.operatio.scenes.common;

import android.content.res.Resources;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.graphics.renderers.Text;
import com.axiom.atom.engine.ui.widgets.Button;

/**
 * Компонент отображающий материал и текст к нему
 */
public class ItemWidget extends Button {

    protected static Sprite allMachines = null;            // Спрайт всех блоков
    protected Sprite exclamationSprite;                                // Значек сбоя

    private boolean active = true;
    private boolean exclamation = false;
    private float alpha = 1;


    public ItemWidget(String text) {
        super(text);
        textRenderer.setHorizontalAlignment(Text.ALIGN_RIGHT);
        textRenderer.setVerticalAlignment(Text.ALIGN_BOTTOM);
        if (allMachines==null) {
            Resources resources = SceneManager.getResources();
            allMachines = new Sprite(resources, R.drawable.blocks, 8, 16);
        }
        exclamationSprite = allMachines.getAsSprite(71);
    }

    @Override
    public void draw(Camera camera) {
        if (parent==null || !visible) return;
        AABB bounds = getWorldBounds();
        AABB parentScissor = scissorsEnabled ? parent.getScissors() : null;

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
                float temp = background.getAlpha();
                background.setAlpha(alpha);
                background.draw(camera, bounds, parentScissor);
                background.setAlpha(temp);
            }
            if (text != null) {
                textRenderer.setZOrder(zOrder + 2);
                textRenderer.setColor(textColor[0], textColor[1], textColor[2], textColor[3]);
                textRenderer.draw(camera, text, bounds.maxX - 2, bounds.minY + 2, textScale, parentScissor);
            }
        }

        if (exclamation) {
            float fw = bounds.width;
            float fh = bounds.height;
            exclamationSprite.setZOrder(zOrder + 3);
            exclamationSprite.draw(camera, bounds.minX, bounds.maxY - fh, fw, fh);
        }
    }

    @Override
    public void setText(CharSequence caption) {
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

    public void setExclamation(boolean state) {
        exclamation = state;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setSpriteAlpha(float alpha) {
        this.alpha = alpha;
    }
}
