package com.axiom.atom.engine.ui.widgets;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;

import org.jetbrains.annotations.Nullable;

public class Button extends Widget {

    protected Sprite background;
    protected String text;
    protected float[] textColor = {0,0,0,1};
    protected float textScale = 2.0f;

    public Button() {
        super();
        text = "";
        setSize(64, 64);
        setColor(0.5f, 0.7f, 0.5f, 0.9f);
    }

    public Button(Sprite background, String text) {
        this();
        this.background = background;
        this.text = text;
    }

    public Button(String text) {
        this(null, text);
    }

    public Button(Sprite background) {
        this(background, null);
    }

    @Override
    public void draw(Camera camera) {
        if (parent==null || !visible) return;
        AABB bounds = getWorldBounds();
        // AABB scissors = getScissors();
        AABB parentScissor = parent.getScissors();

        if (opaque) {
            GraphicsRender.setZOrder(zOrder);
            GraphicsRender.setColor(color[0], color[1], color[2], color[3]);
            GraphicsRender.drawRectangle(bounds, parentScissor);
        }

        if (background !=null) {
            background.setZOrder(zOrder + 1);
            background.draw(camera, bounds, parentScissor);
        }

        if (text!=null) {
            GraphicsRender.setZOrder(zOrder + 2);
            float textWidth = GraphicsRender.getTextWidth(text, textScale);
            float textHeight = GraphicsRender.getTextHeight(text,textScale);
            GraphicsRender.setColor(textColor[0], textColor[1], textColor[2], textColor[3]);
            GraphicsRender.drawText(text, bounds.center.x - textWidth/2, bounds.center.y - (textHeight/2), textScale, parentScissor);
            // Пока не обрезаем текст для повышения производительности
            // GraphicsRender.drawText(text, bounds.center.x - textWidth/2, bounds.center.y - (textHeight/2), textScale, scissors);
        }

        super.draw(camera);
    }

    public void setBackground(Sprite background) {
        this.background = background;
    }

    public Sprite getBackground() { return this.background; }

    public void setTextColor(float r, float g, float b, float a) {
        textColor[0] = r;
        textColor[1] = g;
        textColor[2] = b;
        textColor[3] = a;
    }

    public void setTextColor(int rgba) {
        setTextColor(((rgba >> 16) & 0xff) / 255.0f,
                ((rgba >>  8) & 0xff) / 255.0f,
                ((rgba      ) & 0xff) / 255.0f,
                ((rgba >> 24) & 0xff) / 255.0f);
    }

    public void setTextScale(float scale) {
        this.textScale = scale;
    }

    public void setText(String caption) {
        if (caption!=null) this.text = caption;
    }

}
