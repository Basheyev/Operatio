package com.axiom.atom.engine.graphics.gles2d;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.axiom.atom.engine.graphics.GraphicsRender;

import javax.microedition.khronos.opengles.GL10;

/**
 * Реализует класс работы с текстурой
 * (C) Atom Engine, Bolat Basheyev 2020
 */
public class Texture implements GLObject {

    protected int textureID;
    protected float width;
    protected float height;
    private Bitmap flippedBitmap;

    public Texture(Resources resources, int resource) {
        Bitmap bitmap = BitmapFactory.decodeResource(resources, resource);
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(1,-1);  // Переворачиваем текстуру
        flippedBitmap = Bitmap.createBitmap(bitmap,0,0,(int)width,(int)height,matrix,false);
        bitmap.recycle();
        GraphicsRender.glTasksQueue.add(this);
    }

    @Override
    public void initializeOnGLThread() {
        int[] temp = new int[1];
        GLES20.glGenTextures (1, temp, 0);
        textureID = temp[0];

        GLES20.glBindTexture (GLES20.GL_TEXTURE_2D, textureID);
        GLES20.glTexParameterf (GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf (GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf (GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf (GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D (GLES20.GL_TEXTURE_2D, 0, flippedBitmap, 0);
        flippedBitmap.recycle();
    }

    public void bind() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public int getTextureID() {
        return textureID;
    }

}
