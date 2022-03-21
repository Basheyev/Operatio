package com.axiom.atom.engine.ui.widgets;

import android.graphics.Color;
import android.graphics.Typeface;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Text;

public class ProgressBar extends Widget {

    private Text textRenderer;
    private StringBuffer text;
    private float[] backgroundColor = {0,0,0,1};
    private float[] textColor = {0.5f,0.5f,0.5f,1};
    private float textScale = 2.0f;
    private int progress = 0;

    public ProgressBar() {
        super();
        setColor(Color.YELLOW);
        textRenderer = new Text();
        textRenderer.setHorizontalAlignment(Text.ALIGN_CENTER);
        textRenderer.setVerticalAlignment(Text.ALIGN_CENTER);
        setSize(180, 60);
        text = new StringBuffer();
    }

    @Override
    public void draw(Camera camera) {
        if (parent==null || !visible) return;

        AABB bounds = getWorldBounds();
        AABB parentScissor = scissors ? parent.getScissors() : null;

        // Нарисовать фон
        GraphicsRender.setZOrder(zOrder);
        GraphicsRender.setColor(backgroundColor[0], backgroundColor[1], backgroundColor[2], backgroundColor[3]);
        GraphicsRender.drawRectangle(bounds, parentScissor);

        // Нарисовать прогресс
        GraphicsRender.setZOrder(zOrder + 1);
        GraphicsRender.setColor(color[0], color[1], color[2], color[3]);
        GraphicsRender.drawRectangle(bounds.minX, bounds.minY,
                bounds.width * (progress / 100.0f), bounds.height, parentScissor);

        // Нарисовать текст
        textRenderer.setZOrder(zOrder + 2);
        textRenderer.setColor(textColor[0], textColor[1], textColor[2], textColor[3]);
        textScale = bounds.height / textRenderer.getRasterizedFontSize() * 0.5f;
        textRenderer.draw(camera, text, bounds.centerX, bounds.centerY, textScale, parentScissor);

    }

    @Override
    protected int getRenderLayersCount() {
        return 3;
    }

    public int getProgress() {
        return progress;
    }


    public void setTypeface(Typeface typeface) {
        textRenderer.setTypeface(typeface);
    }

    public void setTypeface(String fontName) {
        textRenderer.setTypeface(fontName);
    }

    public void setProgress(int progress) {
        if (progress < 0) progress = 0;
        if (progress > 100) progress = 100;
        this.progress = progress;
        text.setLength(0);
        text.append(progress);
        text.append("%");
    }


    public void setTextColor(float r, float g, float b, float a) {
        textColor[0] = r;
        textColor[1] = g;
        textColor[2] = b;
        textColor[3] = a;
    }


    public void setTextColor(int rgba) {
        GraphicsRender.colorIntToFloat(rgba, textColor);
    }


    public void setBackgroundColor(float r, float g, float b, float a) {
        backgroundColor[0] = r;
        backgroundColor[1] = g;
        backgroundColor[2] = b;
        backgroundColor[3] = a;
    }


    public void setBackgroundColor(int rgba) {
        GraphicsRender.colorIntToFloat(rgba, backgroundColor);
    }

}
