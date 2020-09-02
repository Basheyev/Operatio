package com.axiom.atom.engine.graphics.renderers;


import android.opengl.GLES20;
import android.util.Log;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.gles2d.Texture;
import com.axiom.atom.engine.graphics.gles2d.VertexBuffer;

import java.util.Arrays;
import java.util.Comparator;


/**
 * Многократно оптимизирует производительность рендерига графики
 * путём группировки очереди отрисовываемых спрайтов и прямоугольников
 * по z-order, текстуре спрайтов и цвету прямогульников в пакеты
 * для минимизации количества вызовов отрисовки (draw calls).
 * <br><br>
 * (С) Atom Engine, Bolat Basheyev 2020
 */
public class BatchRender {
    //------------------------------------------------------------------------------------------
    // Единица хранения очереди отрисовки: спрайт или прямоугольник
    //------------------------------------------------------------------------------------------
    protected static class Entry {
        public int zOrder;                          // Слой отрисовки (критерий группировки)
        public Texture texture;                     // Текстура спрайта (критерий группировки)
        public float[] vertices = new float[18];    // Вершины спрайта (2 треугольника - 18 коорд)
        public float[] coordinates = new float[12]; // текстурные координаты спрайта
        public float[] color = new float[4];        // цвет примитива если нет текстуры
        public AABB scissor;                       // Область отсечения в физических координатах экрана
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
    public static void addRectangle(float[] vert, int zOrder, float[] color, AABB scissor) {
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
        entry.scissor = scissor;
        System.arraycopy(vert, 0, entry.vertices, 0, 18);
        entriesCounter++;
    }

    //-------------------------------------------------------------------------------------------
    public static void addSprite(Texture texture, float[] vert, float[] texcoord, int zOrder, AABB scissor) {
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
        entry.scissor = scissor;
        System.arraycopy(vert, 0, entry.vertices, 0, 18);
        System.arraycopy(texcoord, 0, entry.coordinates, 0, 12);
        entriesCounter++;
    }

    //-------------------------------------------------------------------------------------------
    private static Entry previous = new Entry();

    public static void finishBatching(Camera camera) {
        if (entriesCounter==0) return;

        // Сортируем всё, чтобы подготовить к группировке в пакеты
        Arrays.sort(entries,0, entriesCounter, comparator);

        // Начинаем новый пакет
        clearBatch();
        Entry entry = entries[0];
        addEntryToBatch(entry);
        copyEntry(entry, previous);

        drawCallsCounter = 0;
        boolean equals, lastEntry;

        for (int i=0; i<entriesCounter; i++) {
            entry = entries[i];
            lastEntry = (i == entriesCounter - 1);
            // Сравниваем текстуру, z order, цвет и ножницы с предыдущей
            equals = comparator.compare(entry, previous) == 0 && entry.scissor == previous.scissor;
            // Если текущий и предыдущий пакет равны добавляем в один пакет
            if (equals) {
                addEntryToBatch(entry);
                copyEntry(entry, previous);
            }
            // если не равны или это последний элемент - отрисовываем пакет
            if (!equals || lastEntry) {
                renderBatch(camera.getCameraMatrix(), previous);
                // начинаем новый пакет
                clearBatch();
                addEntryToBatch(entry);
                copyEntry(entry, previous);
                if (lastEntry) renderBatch(camera.getCameraMatrix(), previous);
            }
        }

    }


    private static void renderBatch(float[] cameraMatrix, Entry entry) {

        loadBatchToBuffer();

        if (verticesBatch.getVertexCount()==0) return;

        if (entry.scissor!=null) enableScissors(entry.scissor);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        if (entry.texture==null) {
            Rectangle.program.use();
            int vertexHandler = Rectangle.program.setAttribVertexArray("vPosition", verticesBatch);
            Rectangle.program.setUniformVec4Value("vColor", entry.color);
            Rectangle.program.setUniformMat4Value("u_MVPMatrix", cameraMatrix);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, verticesBatch.getVertexCount());
            Rectangle.program.disableVertexArray(vertexHandler);
        } else {
            Sprite.program.use();
            entry.texture.bind();
            int vertexHandler = Sprite.program.setAttribVertexArray("vPosition", verticesBatch);
            int textureHandler = Sprite.program.setAttribVertexArray("TexCoordIn", texCoordBatch);
            Sprite.program.setUniformIntValue("sampler", 0);
            Sprite.program.setUniformMat4Value("u_MVPMatrix", cameraMatrix);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, verticesBatch.getVertexCount());
            Sprite.program.disableVertexArray(textureHandler);
            Sprite.program.disableVertexArray(vertexHandler);
        }

        GLES20.glDisable(GLES20.GL_BLEND);

        if (entry.scissor!=null) disableScissor();

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

    protected static void enableScissors(AABB clip) {
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(
                Math.round(clip.min.x),
                Math.round(clip.min.y),
                Math.round(clip.width),
                Math.round(clip.height)
        );
    }

    protected static void disableScissor() {
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
    }

    //-------------------------------------------------------------------------------------------
    // Вспомогательные методы для пакетирования
    //-------------------------------------------------------------------------------------------

    private static void clearBatch() {
        verticesBatch.clear();
        texCoordBatch.clear();
    }

    private static void addEntryToBatch(Entry entry) {
        verticesBatch.pushVertices(entry.vertices);
        texCoordBatch.pushVertices(entry.coordinates);
    }

    private static void loadBatchToBuffer() {
        verticesBatch.prepare();
        texCoordBatch.prepare();
    }

    private static void copyEntry(Entry src, Entry dst) {
        dst.texture = src.texture;
        dst.zOrder = src.zOrder;
        dst.scissor = src.scissor;
        System.arraycopy(src.color, 0, dst.color, 0, 4);
    }

}
