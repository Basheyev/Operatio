package com.axiom.operatio.model.block;

import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;

// TODO Рендерить под блоком конвейер - как основу движения материалов
public abstract class BlockRenderer {

    public abstract void draw(Camera camera, float x, float y, float width, float height);


    public void drawInOut(Camera camera, int inDir, int outDir,
                          float x, float y, float width, float height, int zOrder) {

        float ux = x, uy = y + height / 2 - 5;
        float dx = x, dy = y - height / 2 + 5;
        float lx = x - width / 2 + 5, ly = y;
        float rx = x + width / 2 - 5, ry = y;
        float inputX = x, inputY = y;
        float outputX = x, outputY = y;


        // отрисовать направления входа/выхода
        switch (inDir) {
            case Block.UP:    inputX = ux; inputY = uy; break;
            case Block.DOWN:  inputX = dx; inputY = dy; break;
            case Block.LEFT:  inputX = lx; inputY = ly; break;
            case Block.RIGHT: inputX = rx; inputY = ry; break;
        }
        switch (outDir) {
            case Block.UP:    outputX = ux; outputY = uy; break;
            case Block.DOWN:  outputX = dx; outputY = dy; break;
            case Block.LEFT:  outputX = lx; outputY = ly; break;
            case Block.RIGHT: outputX = rx; outputY = ry; break;
        }

        GraphicsRender.setZOrder(zOrder);
        GraphicsRender.setColor(1,0,0,1);
        GraphicsRender.drawRectangle(inputX - 5 + width/2, inputY -5 + height/2, 10,10, null);
        GraphicsRender.setZOrder(zOrder);
        GraphicsRender.setColor(0,1,0,1);
        GraphicsRender.drawRectangle(outputX - 5 + width/2, outputY -5+ height/2, 10,10, null);

    }

}
