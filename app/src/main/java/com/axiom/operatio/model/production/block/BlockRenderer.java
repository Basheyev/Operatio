package com.axiom.operatio.model.production.block;

import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.operatio.model.production.Production;

public abstract class BlockRenderer {

    public abstract void draw(Camera camera, float x, float y, float width, float height);


    public void drawInOut(Camera camera, int inDir, int outDir,
                          float x, float y, float width, float height, int zOrder) {

        float uy = y + height / 2 - 5;
        float dy = y - height / 2 + 5;
        float lx = x - width / 2 + 5;
        float rx = x + width / 2 - 5;
        float inputX = x, inputY = y;
        float outputX = x, outputY = y;


        // отрисовать направления входа/выхода
        switch (inDir) {
            case Block.UP:    inputX = x; inputY = uy; break;
            case Block.DOWN:  inputX = x; inputY = dy; break;
            case Block.LEFT:  inputX = lx; inputY = y; break;
            case Block.RIGHT: inputX = rx; inputY = y; break;
        }
        switch (outDir) {
            case Block.UP:    outputX = x; outputY = uy; break;
            case Block.DOWN:  outputX = x; outputY = dy; break;
            case Block.LEFT:  outputX = lx; outputY = y; break;
            case Block.RIGHT: outputX = rx; outputY = y; break;
        }

        GraphicsRender.setZOrder(zOrder);
        GraphicsRender.setColor(1,0,0,1);
        GraphicsRender.drawRectangle(inputX - 5 + width/2, inputY -5 + height/2, 10,10, null);
        GraphicsRender.setZOrder(zOrder);
        GraphicsRender.setColor(0,1,0,1);
        GraphicsRender.drawRectangle(outputX - 5 + width/2, outputY -5+ height/2, 10,10, null);

    }

}
