package com.axiom.atom.engine.graphics.renderers;


import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.gles2d.Camera;


/**
 * Отрисовывает текст, на основе шрифта в виде спрайта
 * (C) Atom Engine, Bolat Basheyev 2020
 */
public class TextSprite {


    public int zOrder = 0;
    protected Sprite font;
    protected float spacing;

    public TextSprite(Sprite font) {
        this.font = font;
        this.spacing = 1;
    }


    public TextSprite(Sprite font, float spacing) {
        this.font = font;
        this.spacing = spacing;
    }

    public void draw(Camera camera, String text, float x, float y, float scale) {
        draw(camera,text,x,y,scale, null);
    }

    public void draw(Camera camera, StringBuffer text, float x, float y, float scale) {
        draw(camera,text,x,y,scale, null);
    }

    public void draw(Camera camera, String text, float x, float y, float scale, AABB scissor) {
        float bx = x;
        float by = y;
        font.zOrder = zOrder;
        for (int i=0; i< text.length(); i++) {
            char symbol = text.charAt(i);
            if (symbol=='\n') {
                by -= font.getHeight() * scale;
                bx = x;
                continue;
            }
            font.setActiveFrame(symbol - ' ');
            font.draw(camera, bx, by, scale, scissor);
            bx += font.getWidth() * scale * spacing;
        }
    }


    public void draw(Camera camera, StringBuffer text, float x, float y, float scale, AABB scissor) {
        float bx = x;
        float by = y;
        font.zOrder = zOrder;
        for (int i=0; i< text.length(); i++) {
            char symbol = text.charAt(i);
            if (symbol=='\n') {
                by -= font.getHeight() * scale;
                bx = x;
                continue;
            }
            font.setActiveFrame(symbol - ' ');
            font.draw(camera, bx, by, scale, scissor);
            bx += font.getWidth() * scale * spacing;
        }
    }


    public float getTextWidth(String text, float scale) {
        return text.length() * font.getWidth() * scale * spacing;
    }



}
