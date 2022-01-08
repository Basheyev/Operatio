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
 * <b>Пакетный рендер</b><br>
 * Многократно повышает производительность отрисовки 2D-графики
 * путём минимизации количества вызовов отрисовки (draw calls).
 * Группирует список отрисовываемых элементов (quads) в однородные
 * партии по слою (z-order), текстуре, шейдеру и цвету. Отрисовывает
 * партию одним вызовом. <br><br>
 * (С) Atom Engine, Bolat Basheyev 2020-2022
 */
public class BatchRender {

    //------------------------------------------------------------------------------------------
    public static final int MAX_QUADS = 8192;     // Максимальное количество элементов на экране
    protected static int drawCallsCounter = 0;    // Счётчик вызовов отрисовки (меньше лучше)
    protected static int scissorCounter = 0;      // Счётчик случаев обрезки экранных областей
    //------------------------------------------------------------------------------------------
    protected static Quad[] quads;                // Буфер всех элементов на отрисовку
    protected static Quad.Comparator comparator;  // Класс для сравнения элементов буфера
    protected static int entriesCounter = 0;      // Количество элементов на отрисовку
    protected static VertexBuffer verticesBatch;  // Буфер вершинных координат партии элементов
    protected static VertexBuffer texCoordBatch;  // Буфер текстурных координат партии элементов
    private static boolean batchRendered;         // Флаг отрисовки партии
    private static Quad previous = new Quad();    // Последний обработанный элемент
    //------------------------------------------------------------------------------------------
    protected static int quadsProcessed = 0;      // Обработано элементов за один кадр
    protected static int drawCallsMade = 0;       // Количество вызовов отрисовки за один кадр
    protected static int scissorsApplied = 0;     // Количество случаев обрезки экранной области
    //-------------------------------------------------------------------------------------------
    // Методы для пакетирования
    //-------------------------------------------------------------------------------------------

    /**
     * Инициализация пакетного рендера <br>
     * Единовременное выделение памяти под массив элементов отрисовки (quads),
     * а также буфера вершин и текстурных координат для сгруппированной партии
     */
    private static void initialize() {
        quads = new Quad[MAX_QUADS];
        for (int i = 0; i< quads.length; i++) quads[i] = new Quad();
        verticesBatch = new VertexBuffer(MAX_QUADS * 6, 3);
        texCoordBatch = new VertexBuffer(MAX_QUADS * 6, 2);
        comparator = new Quad.Comparator();
    }

    //-------------------------------------------------------------------------------------------

    /**
     * Начать упаковки партий элементов на отрисовку <br>
     * Инциализация при необходимости и обнуление счётчика отрисованных элементов
     */
    public static synchronized void beginBatching() {
        if (quads == null) initialize();      // Если вызывается впервые, то инициализируемся
        quadsProcessed = entriesCounter;      // Количество отрисованных quads на прошлом кадре
        entriesCounter = 0;                   // Обнуляем счётчик
    }

    //-------------------------------------------------------------------------------------------

    /**
     * Добавить в список новый элемент на отрисовку
     * @param program шейдер элемента
     * @param vert координаты вершин элемента
     * @param color цвет элемента
     * @param zOrder слой отрисовки
     * @param scissor экранная область обрезки
     */
    public static synchronized void addQuad(Program program,
                                            float[] vert, float[] color, int zOrder, AABB scissor) {
        // Проверяем есть ли ещё место
        if (entriesCounter + 1 >= quads.length) {
            Log.e("BATCH RENDERER", "Max sprites count reached " + MAX_QUADS);
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

    /**
     * Добавить в список новый текстурированный элемент на отрисовку
     * @param program шейдер элемента
     * @param texture текстура элемента
     * @param vert координаты вершин элемента
     * @param texcoord текстурные координаты
     * @param color цвет элемента
     * @param zOrder слой отрисовки
     * @param scissor экранная область обрезки
     */
    public static synchronized void addTexturedQuad (Program program,
                                                     Texture texture, float[] vert, float[] texcoord,
                                                     float[] color, int zOrder, AABB scissor) {
        // Проверяем есть ли ещё место
        if (entriesCounter + 1 >= quads.length) {
            Log.w("WARNING", "Max sprites count reached " + MAX_QUADS);
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

    /**
     * Сортирует список всех элементов в списке на отрисовку (текстуре, слою, цвету и т.д)
     * и генерирует меш из однородных прямоугольников для минимизации количества вызовов OpenGL
     * @param camera камера
     */
    public static synchronized void finishBatching(Camera camera) {
        if (entriesCounter==0) return;

        // Сортируем список, чтобы подготовить к группировке в партии
        Arrays.sort(quads,0, entriesCounter, comparator);
        clearBatch();                  // Начинаем новую партию элементов на отрисовку
        Quad quad = quads[0];          // Берём первый элемент в отсортированном списке отрисовки
        addQuadToBatch(quad);          // Добавляем в партию на отрисовку
        copyQuad(quad, previous);      // Сохраняем элемент как предыдущий обработанный

        drawCallsCounter = 0;
        scissorCounter = 0;
        boolean equals, lastEntry;

        // Последовательно проходим по отсортированному списку элементов на отрисовку
        for (int i=1; i<entriesCounter; i++) {
            quad = quads[i];
            // Сравниваем текстуру, z order, цвет и область обрезки с предыдущим элементом
            equals = comparator.compare(quad, previous) == 0 && quad.scissor == previous.scissor;
            // Если текущий и предыдущий элемент равны добавляем в одну партию элментов
            if (equals) {
                addQuadToBatch(quad);
                copyQuad(quad, previous);
            } else {
                // Если предыдущий элекмент отличается, отрисовываем партию элементов
                renderBatch(camera.getCameraMatrix(), previous);
                // Начинаем новую партию элементов на отрисовку
                clearBatch();
                addQuadToBatch(quad);
                copyQuad(quad, previous);
            }
        }

        // Если последняя партия элементов не была отрисована, то отрисовываем
        if (!batchRendered) renderBatch(camera.getCameraMatrix(), previous);
        // Сохраняем статистику из счётчиков
        drawCallsMade = drawCallsCounter;
        scissorsApplied = scissorCounter;
    }


    /**
     * Отрисовываем сгруппированную партию (меш) однородных прямоугольников одним разом
     * @param cameraMatrix матрица камеры для передачи в шейдер
     * @param quad свойства для отрисовки меша берем из этой записи
     */
    private static void renderBatch(float[] cameraMatrix, Quad quad) {
        batchRendered = true;

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

    public static int getScissorsApplied() { return scissorsApplied; }

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
        scissorCounter++;
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
        batchRendered = false;
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
