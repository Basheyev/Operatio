package com.axiom.atom.engine.ui.widgets;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.MotionEvent;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Text;

public class CheckBox extends Widget {

    protected CharSequence text;
    protected Text textRenderer;
    protected float[] textColor = {0,0,0,1};
    protected float textScale = 2.0f;
    protected boolean isChecked;

    public CheckBox(CharSequence caption, boolean isChecked) {
        super();
        this.text = caption;
        this.isChecked = isChecked;
        this.textRenderer = new Text();
    }

    @Override
    public void draw(Camera camera) {
        if (parent==null || !visible) return;
        AABB bounds = getWorldBounds();
        AABB parentScissor = scissors ? parent.getScissors() : null;

        GraphicsRender.setZOrder(zOrder);
        GraphicsRender.setColor(Color.BLACK);
        GraphicsRender.drawRectangle(bounds.minX + 5, bounds.minY + 5, 30, 30, parentScissor);

        if (isChecked) {
            GraphicsRender.setZOrder(zOrder + 1);
            GraphicsRender.setColor(Color.WHITE);
            GraphicsRender.drawRectangle(bounds.minX + 10, bounds.minY + 10, 20, 20, parentScissor);
        }

        if (text!=null) {
            textRenderer.setZOrder(zOrder);
            textRenderer.setColor(textColor[0], textColor[1], textColor[2], textColor[3]);
            textRenderer.draw(camera, text, bounds.minX + 50, bounds.minY + 10, textScale, parentScissor);
        }

        super.draw(camera);

    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
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

    public void setTextScale(float scale) {
        this.textScale = scale;
    }

    public void setText(String caption) {
        if (caption!=null) this.text = caption;
    }


    @Override
    public boolean onMotionEvent(MotionEvent event, float worldX, float worldY) {
        boolean eventHandeled = false;
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            pressed = true;
        } else
        if (action == MotionEvent.ACTION_UP) {
            isChecked = !isChecked;
            if (pressed && clickListener != null) {
                clickListener.onClick(this);
                eventHandeled = true;
            }
            pressed = false;
        }
        return eventHandeled;
    }
}
