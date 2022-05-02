package com.basheyev.atom.engine.graphics.gles2d;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Буфер вершин хранящийся в native Heap (не Java Heap) для передачи данных в GPU
 * (C) Atom Engine, Bolat Basheyev 2020
 */
public class VertexBuffer
{
    protected FloatBuffer buffer;            // Буфер в нативной памяти содержащий вершины
    protected float[] vertexCoord;           // Массив float в котором хранятся вершины
    protected int vertexStride;              // Шаг в байтах на одну вершину
    protected int vertexCoordCount;          // Количество вершин
    protected int coordinatesPerVertex;      // Количество координат на одну вершину

    /**
     * Конструктор, который загружает вершины буфер из готового float-массива (Java to Native Heap)
     * @param vertexCoord float массив содержащий данные о вершинах
     * @param coordinatesPerVertex количество координат на вершину (например: 2 или 3)
     */
    public VertexBuffer(float[] vertexCoord, int coordinatesPerVertex)
    {
        this.vertexCoord = vertexCoord;
        this.coordinatesPerVertex = coordinatesPerVertex;
        this.vertexCoordCount = vertexCoord.length / coordinatesPerVertex;   // Фактическое количество вершин
        this.vertexStride = coordinatesPerVertex * 4;              // байт на одну вершину
        // Float имеет 4 байта, береём по 4 байта на каждую координату (элемент float массива)
        buffer = allocateNativeMemory(vertexCoord.length * 4);    // размер native буфера в байтах
        this.prepare ();                                           // Загрузить данные
    }


    public VertexBuffer(int maxVertexCount, int coordinatesPerVertex) {
        this.coordinatesPerVertex = coordinatesPerVertex;
        this.vertexStride = coordinatesPerVertex * 4;
        // Создаём буфер в Java heap
        this.vertexCoord = new float[maxVertexCount * coordinatesPerVertex];
        // Создаём буфера в Native heap
        buffer = allocateNativeMemory(vertexCoord.length * 4);
        // Фактическое количество вершин
        this.vertexCoordCount = 0;
    }


    private FloatBuffer allocateNativeMemory(int bytes) {
        ByteBuffer factory = ByteBuffer.allocateDirect(bytes);
        factory.order (ByteOrder.nativeOrder());
        // Возвращает выделенную память как FloatBuffer
        return factory.asFloatBuffer();
    }


    /**
     * Загружает данные содержащиеся в vertex (Java Heap) в буфер (Native Heap)
     */
    public void prepare ()
    {
        // Копирует данные из массива вершин (float) в выделенный буфер нативной памяти
        buffer.put(vertexCoord);
        // Устанавливает курсор на начало буфера
        buffer.position(0);
    }

    public FloatBuffer getBuffer() {
        return buffer;
    }

    public int getVertexCount() {
        return vertexCoordCount / coordinatesPerVertex;
    }

    //---------------------------------------------------------------------------------------
    // Поддержка динамического буфера вершин
    //---------------------------------------------------------------------------------------

    public void clear() {
        vertexCoordCount = 0;
    }

    public boolean pushVertices(float[] vertCoordinates) {

        int freeSpace = this.vertexCoord.length - vertexCoordCount;
        if (freeSpace < (vertCoordinates.length / coordinatesPerVertex)) return false;

        System.arraycopy(vertCoordinates, 0, vertexCoord, vertexCoordCount, vertCoordinates.length);

        vertexCoordCount += vertCoordinates.length;
        return true;
    }

}
