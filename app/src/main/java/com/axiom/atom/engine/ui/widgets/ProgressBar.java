package com.axiom.atom.engine.ui.widgets;

import android.graphics.Color;

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
        textRenderer = new Text("sans-serif");
        textRenderer.setHorizontalAlignment(Text.ALIGN_CENTER);
        textRenderer.setVerticalAlignment(Text.ALIGN_CENTER);
        setSize(180, 60);
        text = new StringBuffer();
    }

    @Override
    public void draw(Camera camera) {
        if (parent==null || !visible) return;

        AABB bounds = getWorldBounds();
        AABB parentScissor = scissorsEnabled ? parent.getScissors() : null;

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


    public int getProgress() {
        return progress;
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
        setTextColor(((rgba >> 16) & 0xff) / 255.0f,
                ((rgba >>  8) & 0xff) / 255.0f,
                ((rgba      ) & 0xff) / 255.0f,
                ((rgba >> 24) & 0xff) / 255.0f);
    }


    public void setBackgroundColor(float r, float g, float b, float a) {
        backgroundColor[0] = r;
        backgroundColor[1] = g;
        backgroundColor[2] = b;
        backgroundColor[3] = a;
    }


    public void setBackgroundColor(int rgba) {
        setBackgroundColor(((rgba >> 16) & 0xff) / 255.0f,
                ((rgba >>  8) & 0xff) / 255.0f,
                ((rgba      ) & 0xff) / 255.0f,
                ((rgba >> 24) & 0xff) / 255.0f);
    }

}
