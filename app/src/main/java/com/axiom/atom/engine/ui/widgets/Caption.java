package com.axiom.atom.engine.ui.widgets;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Text;

public class Caption extends Widget {

    private Text textRenderer;
    private CharSequence caption;
    private float[] textColor = {0,0,0,1};
    private float scale = 1.0f;
    private int alignment = Text.ALIGN_LEFT;

    public Caption(String text) {
        super();
        textRenderer = new Text("sans-serif");
        caption = text;
    }

    @Override
    public void draw(Camera camera) {
        if (parent==null || !visible) return;
        if (caption != null) {

            AABB bounds = getWorldBounds();
            AABB scissors = parent.getScissors();

            float textWidth = textRenderer.getTextWidth(caption, scale);
            float textHeight = textRenderer.getTextHeight(caption, scale);

            float xpos = bounds.min.x;
            if (alignment==Text.ALIGN_RIGHT) xpos = bounds.max.x - textWidth;
            if (alignment==Text.ALIGN_CENTER) xpos = bounds.center.x - (textWidth / 2);

            // fixme добавить выравнивание по середине и по право краю при многострочном

            textRenderer.setZOrder(zOrder + 2);
            textRenderer.setColor(textColor[0], textColor[1], textColor[2], textColor[3]);
            textRenderer.draw(camera, caption, xpos, bounds.center.y - textHeight/2, scale, scissors);
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

    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    public int getAlignment() {
        return alignment;
    }
}
