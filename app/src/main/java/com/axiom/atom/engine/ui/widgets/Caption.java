package com.axiom.atom.engine.ui.widgets;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameView;
import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Text;
import com.axiom.operatio.model.common.FormatUtils;

import static android.graphics.Color.BLACK;

import android.graphics.Typeface;

import androidx.core.content.res.ResourcesCompat;

public class Caption extends Widget {

    private Text textRenderer;
    private CharSequence captionText;
    private StringBuffer textBuffer;
    private float[] textColor = {0,0,0,1};
    private float scale = 1.0f;

    public Caption(CharSequence text) {
        this(text,Text.DEFAULT_FONT);
    }

    public Caption(CharSequence text, String fontname) {
        super();
        textRenderer = new Text(fontname);
        initialize(text);
    }

    public Caption(CharSequence text, Typeface font) {
        super();
        textRenderer = new Text(font);
        initialize(text);
    }


    private void initialize(CharSequence text) {
        textRenderer.setHorizontalAlignment(Text.ALIGN_LEFT);
        textRenderer.setVerticalAlignment(Text.ALIGN_CENTER);
        captionText = text;
        textBuffer = new StringBuffer();
        copyToInternalBuffer(captionText);
        setColor(BLACK);
        opaque = false;
    }



    @Override
    public void draw(Camera camera) {
        if (parent==null || !visible) return;

        AABB bounds = getWorldBounds();
        AABB parentScissor = scissors ? parent.getScissors() : null;

        if (opaque) {
            GraphicsRender.setZOrder(zOrder);
            GraphicsRender.setColor(color[0], color[1], color[2], color[3]);
            GraphicsRender.drawRectangle(bounds, parentScissor);
        }

        if (textBuffer != null) {

            float xpos = bounds.minX; // ALIGN_LEFT
            if (getHorzinontalAlignment()==Text.ALIGN_RIGHT) xpos = bounds.maxX;
            if (getHorzinontalAlignment()==Text.ALIGN_CENTER) xpos = bounds.centerX;

            float ypos = bounds.maxY; // ALIGN_TOP
            if (getVerticalAlignment()==Text.ALIGN_BOTTOM) ypos = bounds.minY;
            if (getVerticalAlignment()==Text.ALIGN_CENTER) ypos = bounds.centerY;

            textRenderer.setZOrder(zOrder + 2);
            textRenderer.setColor(textColor[0], textColor[1], textColor[2], textColor[3]);

            if (!FormatUtils.isEqual(textBuffer, captionText)) copyToInternalBuffer(captionText);

            synchronized (textBuffer) {
                if (textBuffer.length()==0) return;
                textRenderer.draw(camera, textBuffer, xpos, ypos, scale, parentScissor);
            }
        }

        super.draw(camera);
    }


    public void setTypeface(Typeface typeface) {
        textRenderer.setTypeface(typeface);
    }

    public void setTypeface(String fontName) {
        textRenderer.setTypeface(fontName);
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

    public void setText(CharSequence caption) {
        if (caption!=null) {
            captionText = caption;
            copyToInternalBuffer(caption);
        }
    }

    public void setTextScale(float scale) {
        this.scale = scale;
    }

    public void setHorizontalAlignment(int alignment) {
        textRenderer.setHorizontalAlignment(alignment);
    }

    public int getHorzinontalAlignment() {
        return textRenderer.getHorizontalAlignment();
    }

    public void setVerticalAlignment(int alignment) {
        textRenderer.setVerticalAlignment(alignment);
    }

    public int getVerticalAlignment() {
        return textRenderer.getVerticalAlignment();
    }


    private void copyToInternalBuffer(CharSequence text) {
        synchronized (textBuffer) {
            textBuffer.setLength(0);
            textBuffer.append(text);
        }
    }

}
