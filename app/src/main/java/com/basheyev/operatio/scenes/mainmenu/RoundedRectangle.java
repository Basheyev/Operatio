package com.basheyev.operatio.scenes.mainmenu;

import android.opengl.GLES20;

import com.basheyev.atom.engine.graphics.gles2d.Camera;
import com.basheyev.atom.engine.graphics.gles2d.Program;
import com.basheyev.atom.engine.graphics.gles2d.Shader;
import com.basheyev.atom.engine.graphics.renderers.BatchRender;
import com.basheyev.atom.engine.graphics.renderers.Quad;

public class RoundedRectangle extends Quad {


    protected static Program program = null;

    //-----------------------------------------------------------------------------------
    // Код вершинного шейдера
    //-----------------------------------------------------------------------------------
    private final String vertexShaderCode =
            "uniform mat4 " + Program.MATRIX + "; " +
            "attribute vec4 " + Program.VERTICES + "; " +
            "attribute vec2 " + Program.TEXCOORDIN + ";" +
            "varying vec2 " + Program.TEXCOORDOUT + ";" +
            "void main() { " +
            "    gl_Position = " + Program.MATRIX + " * " + Program.VERTICES + "; " +
            "    " + Program.TEXCOORDOUT + " = " + Program.TEXCOORDIN + "; " +
            "}";

    //-----------------------------------------------------------------------------------
    // Код пиксельного шейдера отрисовывающего текстуру
    //-----------------------------------------------------------------------------------
    private final String fragmentShaderCode =
            "precision mediump float;\n" +
            "uniform vec4 " + Program.COLOR + ";\n" +
            "varying vec2 " + Program.TEXCOORDOUT + ";\n" +
            "float roundedBox(vec2 point, vec2 Size, float Radius) {\n" +
            "    return length(max(abs(point) - Size + Radius, 0.0)) - Radius;\n" +
            "}\n\n" +
            "void main() {\n" +
            "  vec2 size = vec2(0.5,0.5);\n" +
            "  float radius = 0.1;\n" +
            "  float l = roundedBox(" + Program.TEXCOORDOUT + " - size, size, radius);\n" +
            "  gl_FragColor = " + Program.COLOR + " * (1.0 - step(0.0, l));\n" +
            "}";

    public RoundedRectangle() {
        if (program==null) program = new Program(
                new Shader(GLES20.GL_VERTEX_SHADER, vertexShaderCode),
                new Shader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode));
    }


    public void draw(Camera camera, float x, float y, float width, float height) {
        if (!camera.isVisible(x,y, x+width,y+height)) return;
        float sx = x + width * 0.5f;
        float sy = y + height * 0.5f;
        initializeVertices();
        if (rotation!=0) evaluateRotation(rotation);
        evaluateScale(width, height);
        evaluateOffset(sx, sy);
        BatchRender.addTexturedQuad(program, null, vertices, texCoords, color, zOrder, null);
    }

}
