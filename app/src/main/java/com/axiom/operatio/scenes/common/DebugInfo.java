package com.axiom.operatio.scenes.common;

import com.axiom.atom.BuildConfig;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.BatchRender;

public class DebugInfo {

    protected static StringBuffer fps = new StringBuffer(128);

    public static boolean showFPSInfo = true;

    public static void drawDebugInfo(Camera camera, int color) {
        if (!showFPSInfo) return;
        fps.setLength(0);
        fps.append("v").append(BuildConfig.VERSION_NAME);
        fps.append("  FPS:").append(GraphicsRender.getFPS())
            .append(" Quads:").append(BatchRender.getEntriesCount())
            .append(" Calls:").append(BatchRender.getDrawCallsCount())
            //.append(" Scissors:").append(BatchRender.getScissorsApplied())
            .append(" Render:").append(GraphicsRender.getRenderTime())
            .append("ms");
        float x = camera.getMinX();
        float y = camera.getMinY();
        GraphicsRender.setZOrder(Integer.MAX_VALUE);
        GraphicsRender.setColor(color);
        GraphicsRender.drawText(fps, x + 25,y + 20, 1.2f);
    }


}
