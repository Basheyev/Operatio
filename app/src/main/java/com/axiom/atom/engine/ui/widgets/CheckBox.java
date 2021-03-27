package com.axiom.atom.engine.ui.widgets;

import android.graphics.Color;
import android.view.MotionEvent;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;

public class CheckBox extends Widget {

    protected String text;
    protected float[] textColor = {0,0,0,1};
    protected float textScale = 2.0f;
    protected boolean isChecked;

    public CheckBox(String caption, boolean isChecked) {
        super();
        this.text = caption;
        this.isChecked = isChecked;
    }

    @Override
    public void draw(Camera camera) {
        if (parent==null || !visible) return;
        AABB bounds = getWorldBounds();
        AABB parentScissor = parent.getScissors();

        GraphicsRender.setZOrder(zOrder);
        GraphicsRender.setColor(Color.BLACK);
        GraphicsRender.drawRectangle(bounds.minX + 5, bounds.minY + 5, 30, 30, parentScissor);

        if (isChecked) {
            GraphicsRender.setZOrder(zOrder + 1);
            GraphicsRender.setColor(Color.WHITE);
            GraphicsRender.drawRectangle(bounds.minX + 10, bounds.minY + 10, 20, 20, parentScissor);
        }

        if (text!=null) {
            GraphicsRender.setZOrder(zOrder);
            GraphicsRender.setColor(textColor[0], textColor[1], textColor[2], textColor[3]);
            GraphicsRender.drawText(text, bounds.minX + 50, bounds.minY + 10, textScale, parentScissor);
        }

        super.draw(camera);

    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
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
