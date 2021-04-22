package com.axiom.atom.engine.graphics.renderers;


import android.opengl.GLES20;
import android.util.Log;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.gles2d.Program;
import com.axiom.atom.engine.graphics.gles2d.Texture;
import com.axiom.atom.engine.graphics.gles2d.VertexBuffer;

import java.util.Arrays;

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
    public static final int MAX_SPRITES = 8192;   // Максимальное количество спрайтов на экране
    protected static int drawCallsCounter = 0;    // Счётчик вызовов отрисовки (меньше лучше)
    //------------------------------------------------------------------------------------------
    protected static Quad[] quads;                // Буфер всех элементов на отрисовку
    protected static Quad.Comparator comparator;  // Класс для сравнения элементов буфера
    protected static int entriesCounter = 0;      // Количество элементов на отрисовку
    protected static VertexBuffer verticesBatch;  // Вершинные координаты спрайтов пакета
    protected static VertexBuffer texCoordBatch;  // Текстурные координаты спрайтов пакета

    protected static int quadsProcessed = 0;
    protected static int drawCallsMade = 0;

    //-------------------------------------------------------------------------------------------
    // Методы для пакетирования
    //-------------------------------------------------------------------------------------------

    public static synchronized void beginBatching() {
        // Если ещё не инициализировались
        if (quads == null) {
            quads = new Quad[MAX_SPRITES];
            for (int i = 0; i< quads.length; i++) quads[i] = new Quad();
            verticesBatch = new VertexBuffer(MAX_SPRITES * 6, 3);
            texCoordBatch = new VertexBuffer(MAX_SPRITES * 6, 2);
            comparator = new Quad.Comparator();
        }
        quadsProcessed = entriesCounter;
        entriesCounter = 0;
    }

    //-------------------------------------------------------------------------------------------
    public static synchronized void addQuad(Program program,
                                            float[] vert, float[] color, int zOrder, AABB scissor) {
        // Проверяем есть ли ещё место
        if (entriesCounter + 1 >= quads.length) {
            Log.e("BATCH RENDERER", "Max sprites count reached " + MAX_SPRITES);
            return;
        }
        // Копируем данные в соответствующую запись
        Quad quad = quads[entriesCounter];
        quad.color[0] = color[0];
        quad.color[1] = color[1];
        quad.color[2] = color[2];
        quad.color[3] = color[3];
        quad.program = program;
        quad.texture = null;
        quad.zOrder = zOrder;
        quad.scissor = scissor;
        System.arraycopy(vert, 0, quad.vertices, 0, 18);
        entriesCounter++;
    }

    //-------------------------------------------------------------------------------------------
    public static synchronized void addTexturedQuad (Program program,
                                                     Texture texture, float[] vert, float[] texcoord,
                                                     float[] color, int zOrder, AABB scissor) {
        // Проверяем есть ли ещё место
        if (entriesCounter + 1 >= quads.length) {
            Log.w("WARNING", "Max sprites count reached " + MAX_SPRITES);
            return;
        }
        // Копируем данные в соответствующую запись
        Quad quad = quads[entriesCounter];
        quad.color[0] = color[0];
        quad.color[1] = color[1];
        quad.color[2] = color[2];
        quad.color[3] = color[3];
        quad.program = program;
        quad.texture = texture;
        quad.zOrder = zOrder;
        quad.scissor = scissor;
        System.arraycopy(vert, 0, quad.vertices, 0, 18);
        System.arraycopy(texcoord, 0, quad.texCoords, 0, 12);
        entriesCounter++;
    }

    //-------------------------------------------------------------------------------------------
    private static Quad previous = new Quad();

    /**
     * Сортирует список всех прямоугольников в очереди на отрисовку (текстуре, слою, цвету и т.д)
     * и генерирует меш из однородных прямоугольников для минимизации количества вызовов OpenGL
     * @param camera камера
     */
    public static synchronized void finishBatching(Camera camera) {
        if (entriesCounter==0) return;

        // Сортируем всё, чтобы подготовить к группировке в пакеты
        Arrays.sort(quads,0, entriesCounter, comparator);

        // Начинаем новый пакет
        clearBatch();
        Quad quad = quads[0];
        addQuadToBatch(quad);
        copyQuad(quad, previous);

        drawCallsCounter = 0;
        boolean equals, lastEntry;

        for (int i=0; i<entriesCounter; i++) {
            quad = quads[i];
            // Сравниваем текстуру, z order, цвет и ножницы с предыдущей
            equals = comparator.compare(quad, previous) == 0 && quad.scissor == previous.scissor;
            // Если текущий и предыдущий элемент равны добавляем в один пакет
            if (equals) {
                addQuadToBatch(quad);
                copyQuad(quad, previous);
            } else {
                renderBatch(camera.getCameraMatrix(), previous);
                // начинаем новый пакет
                clearBatch();
                addQuadToBatch(quad);
                copyQuad(quad, previous);
            }
            lastEntry = (i == entriesCounter - 1);
            if (lastEntry) renderBatch(camera.getCameraMatrix(), previous);
        }

        drawCallsMade = drawCallsCounter;
    }


    /**
     * Отрисовываем сгруппированную партию (меш) однородных прямоугольников одним разом
     * @param cameraMatrix матрица камеры для передачи в шейдер
     * @param quad свойства для отрисовки меша берем из этой записи
     */
    private static void renderBatch(float[] cameraMatrix, Quad quad) {

        loadBatchToBuffer();

        if (verticesBatch.getVertexCount()==0) return;

        if (quad.scissor!=null) enableScissors(quad.scissor);

        Program program = quad.program;
        if (program==null) return;
        program.use();

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        if (quad.texture==null) {
            int vertexHandler = program.setAttribVertexArray("vPosition", verticesBatch);
            program.setUniformVec4Value("vColor", quad.color);
            program.setUniformMat4Value("u_MVPMatrix", cameraMatrix);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, verticesBatch.getVertexCount());
            program.disableVertexArray(vertexHandler);
        } else {
            quad.texture.bind();
            int vertexHandler = program.setAttribVertexArray("vPosition", verticesBatch);
            int textureHandler = program.setAttribVertexArray("TexCoordIn", texCoordBatch);
            program.setUniformVec4Value("vColor", quad.color);
            program.setUniformIntValue("sampler", 0);
            program.setUniformMat4Value("u_MVPMatrix", cameraMatrix);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, verticesBatch.getVertexCount());
            program.disableVertexArray(textureHandler);
            program.disableVertexArray(vertexHandler);
        }

        GLES20.glDisable(GLES20.GL_BLEND);

        if (quad.scissor!=null) disableScissor();

        drawCallsCounter++;
    }



    public static int getEntriesCount() {
        return quadsProcessed;
    }

    public static int getDrawCallsCount() {
        return drawCallsMade;
    }

    protected static void enableScissors(AABB clip) {
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(
                Math.round(clip.minX),
                Math.round(clip.minY),
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

    private static void addQuadToBatch(Quad quad) {
        verticesBatch.pushVertices(quad.vertices);
        texCoordBatch.pushVertices(quad.texCoords);
    }

    private static void loadBatchToBuffer() {
        verticesBatch.prepare();
        texCoordBatch.prepare();
    }

    private static void copyQuad(Quad src, Quad dst) {
        dst.program = src.program;
        dst.texture = src.texture;
        dst.zOrder = src.zOrder;
        dst.scissor = src.scissor;
        System.arraycopy(src.color, 0, dst.color, 0, 4);
    }

}
