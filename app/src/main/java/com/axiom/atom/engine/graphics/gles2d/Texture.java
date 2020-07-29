package com.axiom.atom.engine.graphics.gles2d;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.axiom.atom.engine.graphics.GraphicsRender;

import java.util.HashMap;

/**
 * Реализует класс работы с текстурой
 * (C) Atom Engine, Bolat Basheyev 2020
 */
public class Texture implements GLESObject {

    protected int textureID;            // ID загружнной в GPU текстуры
    protected float width;              // Ширина текстуры в пикселях
    protected float height;             // Высота текстуры в пикселях
    private Bitmap flippedBitmap;       // Перевернутое изображения для загрузки в GPU


    public Texture(Resources resources, int resource) {
        //------------------------------------------------------------------------
        // Загружаем исходное изображение
        //------------------------------------------------------------------------
        Bitmap bitmap = BitmapFactory.decodeResource(resources, resource);
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        //------------------------------------------------------------------------
        // Переворачиваем изображение по вертикали для загрузки в OpenGL
        //------------------------------------------------------------------------
        Matrix matrix = new Matrix();
        matrix.postScale(1,-1);
        flippedBitmap = Bitmap.createBitmap(bitmap,0,0,(int)width,(int)height,matrix,false);
        bitmap.recycle();
        //------------------------------------------------------------------------
        // Добавляем в очередь загрузки на GPU
        //------------------------------------------------------------------------
        GraphicsRender.addToLoadQueue(this);
    }

    /**
     * Загружает объект GLES в память GPU в потоке GLThread
     */
    @Override
    public void loadObjectToGPU() {
        int[] temp = new int[1];
        // Создаём текстуру
        GLES20.glGenTextures (1, temp, 0);
        textureID = temp[0];
        // Загружаем изображение в текстуру
        GLES20.glBindTexture (GLES20.GL_TEXTURE_2D, textureID);
        GLES20.glTexParameterf (GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf (GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf (GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf (GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D (GLES20.GL_TEXTURE_2D, 0, flippedBitmap, 0);
        // высвобождаем ресурсы изображения из оперативной памяти, так как уже загрузили в GPU
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
