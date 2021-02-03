package com.axiom.atom.engine.ui.widgets;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Text;

public class Caption extends Widget {

    protected Text textRenderer;
    protected String caption;
    protected float[] textColor = {0,0,0,1};
    protected float scale = 1.0f;

    public Caption(String text) {
        super();
        textRenderer = new Text("sans-serif");
        caption = text;
    }

    @Override
    public void draw(Camera camera) {
        if (parent==null || !visible) return;
        AABB bounds = getWorldBounds();
        AABB scissors = parent.getScissors();

        float textHeight = textRenderer.getTextHeight(caption, scale);

        if (caption != null) {
            textRenderer.zOrder = zOrder + 2;
            textRenderer.setColor(textColor[0], textColor[1], textColor[2], textColor[3]);
            // fixme зафиксировано выравнивание по левому краму (надо дробить на строки и выравнивать каждую)
            textRenderer.draw(camera, caption, bounds.min.x, bounds.center.y - textHeight/2, scale, scissors);
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

    public void setText(String caption) {
        if (caption!=null) this.caption = caption;
    }

    public void setTextScale(float scale) {
        this.scale = scale;
    }

}
