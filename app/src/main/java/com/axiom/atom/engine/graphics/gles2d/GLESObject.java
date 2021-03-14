package com.axiom.atom.engine.graphics.gles2d;


/**
 * Интерфейс для пометки объектов, которые должны загружаться в потоке/контексте OpenGL,
 * такие как программы, шейдеры и текстуры
 * <br><br>
 * (С) Atom Engine, Bolat Basheyev 2020
 */
public interface GLESObject {

    /**
     * Ввиду того, что шейдеры, программы и текстуры могут загружаться только
     * в потоке GLThread (один поток = один контекст) мы добавляем задачи в очередь
     * чтобы потом вызвать initializeOnGLThread() из потока GLThread
     */
    void loadToGPU();

    void deleteFromGPU();

}
