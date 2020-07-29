package com.axiom.atom.engine.graphics.renderers;


import android.opengl.GLES20;
import android.util.Log;

import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.gles2d.Texture;
import com.axiom.atom.engine.graphics.gles2d.VertexBuffer;

import java.util.Arrays;
import java.util.Comparator;


/**
 * Многократно повышает производительность рендерига графики
 * путём группировки очереди отрисовываемых спрайтов и прямоугольников
 * по z-order, текстуре спрайтов и цвету прямогульников в пакеты
 * для минимизации количества вызовов отрисовки (draw calls).
 * <br><br>
 * (С) Atom Engine, Bolat Basheyev 2020
 */
public class Batcher {
    //------------------------------------------------------------------------------------------
    // Единица хранения очереди отрисовки: спрайт или прямоугольник
    //------------------------------------------------------------------------------------------
    protected static class Entry {
        public int zOrder;                          // Слой отрисовки (критерий группировки)
        public Texture texture;                     // Текстура спрайта (критерий группировки)
        public float[] vertices = new float[18];    // Вершины спрайта (2 треугольника - 18 коорд)
        public float[] coordinates = new float[12]; // текстурные координаты спрайта
        public float[] color = new float[4];        // цвет примитива если нет текстуры
    }
    //------------------------------------------------------------------------------------------
    public static final int MAX_SPRITES = 2048;   // Максимальное количество спрайтов на экране
    protected static int drawCallsCounter = 0;    // Счётчик вызовов отрисовки (меньше лучше)
    //------------------------------------------------------------------------------------------
    protected static Entry[] entries;             // Буфер всех элементов на отрисовку
    protected static int entriesCounter = 0;      // Количество элементов на отрисовку
    protected static VertexBuffer verticesBatch;  // Вершинные координаты спрайтов пакета
    protected static VertexBuffer texCoordBatch;  // Текстурные координаты спрайтов пакета
    protected static EntryComparator comparator;  // Класс для сравнения элементов буфера

    //----------------------------------------------------------------------------------
    // Класс для сравнения элементов буфера при сортировке (по текстуре и z-order)
    //----------------------------------------------------------------------------------
    protected static class EntryComparator implements Comparator<Entry> {
        @Override
        public int compare(Entry a, Entry b) {
            // Сравниваем по слою
            if (a.zOrder==b.zOrder) {
                // сравиваем по текстуре
                if (a.texture==b.texture) {
                    // сравниваем по цвету
                    if (a.texture==null) return compareColor(a.color, b.color);
                    return 0;
                }
                if (a.texture==null) return -1;
                if (b.texture==null) return 1;
                if (a.texture.getTextureID() < b.texture.getTextureID())
                    return -1;
                else
                    return 1;
            } else if (a.zOrder < b.zOrder) return -1; else return 1;
        }
    }

    //-------------------------------------------------------------------------------------------
    // Методы для пакетирования
    //-------------------------------------------------------------------------------------------

    public static void beginBatching() {
        // Если ещё не инициализировались
        if (entries==null) {
            entries = new Entry[MAX_SPRITES];
            for (int i=0; i<entries.length; i++) entries[i] = new Entry();
            verticesBatch = new VertexBuffer(MAX_SPRITES * 6, 3);
            texCoordBatch = new VertexBuffer(MAX_SPRITES * 6, 2);
            comparator = new EntryComparator();
        }
        entriesCounter = 0;
    }

    //-------------------------------------------------------------------------------------------
    public static void addRectangle(float[] vert, int zOrder, float[] color) {
        // Проверяем есть ли ещё место
        if (entriesCounter + 1 >= entries.length) {
            Log.w("WARNING", "Max sprites count reached " + MAX_SPRITES);
            return;
        }
        // Копируем данные в соответствующую запись
        Entry entry = entries[entriesCounter];
        entry.color[0] = color[0];
        entry.color[1] = color[1];
        entry.color[2] = color[2];
        entry.color[3] = color[3];
        entry.texture = null;
        entry.zOrder = zOrder;
        System.arraycopy(vert, 0, entry.vertices, 0, 18);
        entriesCounter++;
    }

    //-------------------------------------------------------------------------------------------
    public static void addSprite(Texture texture, float[] vert, float[] texcoord, int zOrder) {
        // Проверяем есть ли ещё место
        if (entriesCounter + 1 >= entries.length) {
            Log.w("WARNING", "Max sprites count reached " + MAX_SPRITES);
            return;
        }
        // Копируем данные в соответствующую запись
        Entry entry = entries[entriesCounter];
        entry.color[0] = 0;
        entry.color[1] = 0;
        entry.color[2] = 0;
        entry.color[3] = 0;
        entry.texture = texture;
        entry.zOrder = zOrder;
        System.arraycopy(vert, 0, entry.vertices, 0, 18);
        System.arraycopy(texcoord, 0, entry.coordinates, 0, 12);
        entriesCounter++;
    }


    //-------------------------------------------------------------------------------------------
    public static void finishBatching(Camera camera) {
        if (entriesCounter==0) return;

        Arrays.sort(entries,0, entriesCounter, comparator);

        Entry entry = entries[0];
        int lastZOrder = entry.zOrder;
        Texture lastTexture = entry.texture;
        float[] lastColor = new float[4];
        boolean batchRendered = true;

        drawCallsCounter = 0;

        for (int i=0; i<entriesCounter; i++) {
            entry = entries[i];
            batchRendered = false;
            // если та же текстура и тот же z order упаковываем всё в один пакет
            if (entry.texture==lastTexture && entry.zOrder==lastZOrder && compareColor(lastColor,entry.color)==0) {
                verticesBatch.pushVertices(entry.vertices);
                texCoordBatch.pushVertices(entry.coordinates);
                lastTexture = entry.texture;
                lastZOrder = entry.zOrder;
                System.arraycopy(entry.color, 0, lastColor, 0, 4);
/*
                // если это прямоугольник, сразу отрисовываем
                if (entry.texture==null) {
                    verticesBatch.prepare();
                    texCoordBatch.prepare();
                    renderBatch(camera, lastTexture, lastColor);
                    batchRendered = true;
                    // начинаем новый пакет
                    verticesBatch.clear();
                    texCoordBatch.clear();
                }*/
            } else {
                // Загружаем данные пакета
                verticesBatch.prepare();
                texCoordBatch.prepare();
                // Отрисовываем прошлый пакет
                renderBatch(camera, lastTexture, lastColor);
                batchRendered = true;
                // начинаем новый пакет
                verticesBatch.clear();
                texCoordBatch.clear();
                verticesBatch.pushVertices(entry.vertices);
                texCoordBatch.pushVertices(entry.coordinates);
                lastTexture = entry.texture;
                lastZOrder = entry.zOrder;
                System.arraycopy(entry.color, 0, lastColor, 0, 4);
            }

        }

        if (!batchRendered) {
            // Загружаем данные пакета данные
            verticesBatch.prepare();
            texCoordBatch.prepare();
            // Отрисовываем прошлый пакет
            renderBatch(camera, lastTexture, entry.color);
            verticesBatch.clear();
            texCoordBatch.clear();
        }

    }


    protected static void renderBatch(Camera camera, Texture texture, float[] color) {
        // Если пустой пакет уходим
        if (verticesBatch.getVertexCount()==0) return;

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        if (texture==null) {
            Rectangle.program.use();
            int vertexHandler = Rectangle.program.setAttribVertexArray("vPosition", verticesBatch);
            Rectangle.program.setUniformVec4Value("vColor", color);
            Rectangle.program.setUniformMat4Value("u_MVPMatrix", camera.getCameraMatrix());
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, verticesBatch.getVertexCount());
            Rectangle.program.disableVertexArray(vertexHandler);
        } else {
            Sprite.program.use();
            texture.bind();
            int vertexHandler = Sprite.program.setAttribVertexArray("vPosition", verticesBatch);
            int textureHandler = Sprite.program.setAttribVertexArray("TexCoordIn", texCoordBatch);
            Sprite.program.setUniformIntValue("sampler", 0);
            Sprite.program.setUniformMat4Value("u_MVPMatrix", camera.getCameraMatrix());
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, verticesBatch.getVertexCount());
            Sprite.program.disableVertexArray(textureHandler);
            Sprite.program.disableVertexArray(vertexHandler);
        }

        GLES20.glDisable(GLES20.GL_BLEND);
        drawCallsCounter++;
    }

    //----------------------------------------------------------------------------
    public static int compareColor(float[] a, float[] b) {
        for (int i=0; i<4; i++) {
            if (a[i] > b[i]) return 1;
            if (a[i] < b[i]) return -1;
        }
        return 0;
    }

    public static int getEntriesCount() {
        return entriesCounter;
    }

    public static int getDrawCallsCount() {
        return drawCallsCounter;
    }

}
