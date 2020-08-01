package com.axiom.operatio.renderers;

import android.content.res.Resources;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;

public class BarChart {

    protected float[] values;
    protected Sprite barSprite;

    public BarChart(Resources r, float[] values) {
        this.values = values;
        barSprite = new Sprite(r, R.drawable.barchart);
    }


    public void draw(Camera camera, float x, float y, float width, float height) {
        float stride = width/values.length;
        for (int i=0; i<values.length; i++) {
            barSprite.draw(camera, x + i * stride, y, stride, y+values[i] * height);
        }
    }


    public void draw(Camera camera, AABB aabb) {
        draw(camera, aabb.min.x, aabb.min.y, aabb.width, aabb.height);
    }

}
