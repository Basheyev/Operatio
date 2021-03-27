package com.axiom.atom.engine.ui.widgets;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Text;

import static android.graphics.Color.RED;

public class Caption extends Widget {

    private Text textRenderer;
    private CharSequence caption;
    private float[] textColor = {0,0,0,1};
    private float scale = 1.0f;

    public Caption(String text) {
        super();
        textRenderer = new Text("sans-serif");
        textRenderer.setHorizontalAlignment(Text.ALIGN_LEFT);
        textRenderer.setVerticalAlignment(Text.ALIGN_CENTER);
        caption = text;
    }

    @Override
    public void draw(Camera camera) {
        if (parent==null || !visible) return;

        if (caption != null) {

            if (caption.length()==0) return;

            AABB bounds = getWorldBounds();
            AABB scissors = parent.getScissors();

            float xpos = bounds.minX; // ALIGN_LEFT
            if (getHorzinontalAlignment()==Text.ALIGN_RIGHT) xpos = bounds.maxX;
            if (getHorzinontalAlignment()==Text.ALIGN_CENTER) xpos = bounds.centerX;

            float ypos = bounds.maxY; // ALIGN_TOP
            if (getVerticalAlignment()==Text.ALIGN_BOTTOM) ypos = bounds.minY;
            if (getVerticalAlignment()==Text.ALIGN_CENTER) ypos = bounds.centerY;

            textRenderer.setZOrder(zOrder + 2);
            textRenderer.setColor(textColor[0], textColor[1], textColor[2], textColor[3]);
            textRenderer.draw(camera, caption, xpos, ypos, scale, scissors);
        }

        super.draw(camera);
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

    public void setText(CharSequence caption) {
        if (caption!=null) this.caption = caption;
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


}
