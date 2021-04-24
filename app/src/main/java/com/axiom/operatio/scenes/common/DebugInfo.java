package com.axiom.operatio.scenes.common;

import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.BatchRender;

public class DebugInfo {

    protected static StringBuffer fps = new StringBuffer(128);


    public static void drawDebugInfo(Camera camera, int color) {
        fps.delete(0, fps.length());
        fps.append("FPS:").append(GraphicsRender.getFPS())
                .append(" Quads:").append(BatchRender.getEntriesCount())
                .append(" Calls:").append(BatchRender.getDrawCallsCount())
                .append(" Time:").append(GraphicsRender.getRenderTime())
                .append("ms");
        float x = camera.getMinX();
        float y = camera.getMinY();
        GraphicsRender.setZOrder(Integer.MAX_VALUE);
        GraphicsRender.setColor(color);
        GraphicsRender.drawText(fps, x + 25,y + 20, 1.2f);
    }


}
