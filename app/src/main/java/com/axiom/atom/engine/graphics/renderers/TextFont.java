package com.axiom.atom.engine.graphics.renderers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.gles2d.Camera;



// TODO Сделать поддержку генерируемых шрифтов
// TODO Изменяемый цвет текста (свой шейдер)

public class TextFont {

    public int zOrder = 0;
    protected Sprite font;
    protected float spacing;

    public TextFont(String fontName) {
        this.font = generateFontTexture(fontName);
        this.spacing = 1;
    }


    /**
     * Генерирует текстуру шрифта на базе указанного
     * @param fontName
     * @return
     */
    protected Sprite generateFontTexture(String fontName) {
        Bitmap bmp;
        Canvas canvas;
        Typeface typeface;

        // TODO Сделать генерацию текстуры

        return null;
    }


    public void draw(Camera camera, String text, float x, float y, float scale) {
        draw(camera,text,x,y,scale, null);
    }

    public void draw(Camera camera, StringBuffer text, float x, float y, float scale) {
        draw(camera,text,x,y,scale, null);
    }

    public void draw(Camera camera, String text, float x, float y, float scale, AABB scissor) {

    }


    public void draw(Camera camera, StringBuffer text, float x, float y, float scale, AABB scissor) {

    }


    public float getTextWidth(String text, float scale) {
        return 0;
    }

}
