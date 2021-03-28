package com.axiom.atom.engine.ui.widgets;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.graphics.renderers.Text;

import static android.graphics.Color.RED;

public class Button extends Widget {

    protected Text textRenderer;
    protected Sprite background;
    protected CharSequence text;
    protected float[] textColor = {0,0,0,1};
    protected float textScale = 2.0f;

    public Button() {
        super();
        text = "";
        textRenderer = new Text("sans-serif");
        textRenderer.setHorizontalAlignment(Text.ALIGN_CENTER);
        textRenderer.setVerticalAlignment(Text.ALIGN_CENTER);
        setSize(64, 64);
        setColor(0.5f, 0.7f, 0.5f, 0.9f);
    }

    public Button(Sprite background, CharSequence text) {
        this();
        this.background = background;
        this.text = text;
    }

    public Button(CharSequence text) {
        this(null, text);
    }

    public Button(Sprite background) {
        this(background, null);
    }

    @Override
    public void draw(Camera camera) {
        if (parent==null || !visible) return;
        AABB bounds = getWorldBounds();
        AABB parentScissor = parent.getScissors();
        float spaceWidth = textRenderer.getCharWidth(' ', textScale);

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
            float x = bounds.minX + spaceWidth;
            float y = bounds.centerY;
            if (getHorizontalAlignment()==Text.ALIGN_CENTER) x = bounds.centerX;
            if (getHorizontalAlignment()==Text.ALIGN_RIGHT) x = bounds.maxY - spaceWidth;

            textRenderer.setZOrder(zOrder + 2);
            textRenderer.setColor(textColor[0], textColor[1], textColor[2], textColor[3]);
            textRenderer.draw(camera, text, x, y, textScale, parentScissor);
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

    public void setText(CharSequence caption) {
        if (caption!=null) this.text = caption;
    }

    public void setHorizontalAlignment(int alignment) {
        this.textRenderer.setHorizontalAlignment(alignment);
    }

    public int getHorizontalAlignment() {
        return textRenderer.getHorizontalAlignment();
    }

}
