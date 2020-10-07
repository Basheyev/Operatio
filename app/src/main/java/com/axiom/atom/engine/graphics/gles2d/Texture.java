package com.axiom.atom.engine.graphics.gles2d;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.axiom.atom.engine.graphics.GraphicsRender;

import java.util.HashMap;

/**
 * Реализует класс работы с текстурой
 * (C) Atom Engine, Bolat Basheyev 2020
 */
public class Texture implements GLESObject {

    // Список всех загруженных текстур, для исключения повторной загрузки одной текстуры
    protected static HashMap<Long, Texture> loadedTextures = new HashMap<>();

    protected int textureID;            // ID загружнной в GPU текстуры
    protected float width;              // Ширина текстуры в пикселях
    protected float height;             // Высота текстуры в пикселях
    private Bitmap flippedBitmap;       // Перевернутое изображения для загрузки в GPU
    private boolean linearFiltering;    // Линейная фильтрация текстуры

    /**
     * Загружает текстуру, если не была загружена, и отдает её
     * @param resources ресурсы приложения
     * @param resource ID ресурсы изображения
     * @return текстура
     */
    public static Texture getInstance(Resources resources, int resource) {
        //----------------------------------------------------------------
        // Загружаем текстуру если она еще не была загружна
        //---------------------------------------------------------------
        Texture texture = loadedTextures.get((long)resource);
        if (texture==null) {
            // Текстуру загружаем только один раз
            texture = new Texture(resources, resource);
            loadedTextures.put((long)resource, texture);
        }
        return texture;
    }

    public static Texture getInstance(Resources resources, int resource, boolean linearFiltering) {
        //----------------------------------------------------------------
        // Загружаем текстуру если она еще не была загружна
        //---------------------------------------------------------------
        Texture texture = loadedTextures.get((long)resource);
        if (texture==null) {
            // Текстуру загружаем только один раз
            texture = new Texture(resources, resource, linearFiltering);
            loadedTextures.put((long)resource, texture);
        }
        return texture;
    }


    /**
     * Загружает текстуру на основе Bitmap
     * @param bitmap изображение
     * @return текстура
     */
    public static Texture getInstance(Bitmap bitmap, boolean linearFiltering) {
        // Так как ключом в HashMap текстур используется уникальный ResourceID
        // чтобы сгенерированный Bitmap не пересекался с ID ресурсов приложения
        // приходится использовать некоторое смещение и хэшкод объекта
        //-------------------------------------------------------------------------
        // An Android Resource id is a 32-bit integer. It comprises
        // an 8-bit Package id [bits 24-31]
        // an 8-bit Type id [bits 16-23]
        // a 16-bit Entry index [bits 0-15]
        //-------------------------------------------------------------------------
        // Ключ HashMap - это long (верхние 32 bit под Bitmap'ы, нижние под ресурсы)
        // Сместим HashCode bitmap в лево на 32 bit чтобы диапазоны не пересекались
        long resource = ((long) bitmap.hashCode()) << 32;

        Texture texture = loadedTextures.get(resource);
        if (texture==null) {
            //----------------------------------------------------------------
            // Загружаем текстуру если она еще не была загружна
            //---------------------------------------------------------------
            texture = new Texture(bitmap, linearFiltering);
            loadedTextures.put(resource, texture);
        }
        return texture;
    }

    public static Texture getInstance(Bitmap bitmap) {
        return getInstance(bitmap, false);
    }

    private Texture(Resources resources, int resource) {
        this(resources, resource, false);
    }

    private Texture(Resources resources, int resource, boolean linearFiltering) {
        //------------------------------------------------------------------------
        // Загружаем исходное изображение из ресурса
        //------------------------------------------------------------------------
        this(BitmapFactory.decodeResource(resources, resource), linearFiltering);
    }

    private Texture(Bitmap bitmap) {
        this(bitmap, false);
    }

    private Texture(Bitmap bitmap, boolean linearFiltering) {
        //------------------------------------------------------------------------
        // Переворачиваем изображение по вертикали для загрузки в OpenGL
        //------------------------------------------------------------------------
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(1,-1);
        flippedBitmap = Bitmap.createBitmap(bitmap,0,0,(int)width,(int)height,matrix,false);
        this.linearFiltering = linearFiltering;
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
        int filtering = linearFiltering ? GLES20.GL_LINEAR : GLES20.GL_NEAREST;
        // Создаём текстуру
        GLES20.glGenTextures (1, temp, 0);
        textureID = temp[0];
        // Загружаем изображение в текстуру
        GLES20.glBindTexture (GLES20.GL_TEXTURE_2D, textureID);
        GLES20.glTexParameterf (GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, filtering);
        GLES20.glTexParameterf (GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, filtering);
        GLES20.glTexParameterf (GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf (GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D (GLES20.GL_TEXTURE_2D, 0, flippedBitmap, 0);
        // высвобождаем ресурсы изображения из оперативной памяти, так как уже загрузили в GPU
        flippedBitmap.recycle();
        flippedBitmap = null;
    }

    /**
     * Биндит (применяет) текстуру перед отправкой Vertex Buffer
     */
    public void bind() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
    }

    /**
     * Возвращает ширину текстуры в пикселях
     * @return ширина текстуры в пикселях
     */
    public float getWidth() {
        return width;
    }

    /**
     * Возвращает высоту текстуры в пикселях
     * @return высота текстуры в пикселях
     */
    public float getHeight() {
        return height;
    }

    /**
     * Возвращает ID текстуры
     * @return ID текстуры
     */
    public int getTextureID() {
        return textureID;
    }

}
