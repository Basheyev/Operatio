package com.axiom.atom.engine.graphics.renderers;

import android.opengl.GLES20;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.gles2d.Program;
import com.axiom.atom.engine.graphics.gles2d.Shader;


// TODO
public class Circle extends Quad {

    protected static Program program = null;

    //-----------------------------------------------------------------------------------
    // Код вершинного шейдера спрайта
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
    // Код пиксельного шейдера спрайта отрисовывающего текстуру
    //-----------------------------------------------------------------------------------
    private final String fragmentShaderCode =
            "precision mediump float;\n" +
            "uniform vec4 " + Program.COLOR + ";\n" +
            "varying vec2 " + Program.TEXCOORDOUT + ";\n" +
            "void main() {\n" +
            "  float l = length(" + Program.TEXCOORDOUT + " - vec2(0.5,0.5));\n" +
            "  if (l > 0.5) discard;\n" +
            "  gl_FragColor = " + Program.COLOR + ";\n" +
            "}";

    public Circle() {
        if (program==null) program = new Program(
                new Shader(GLES20.GL_VERTEX_SHADER, vertexShaderCode),
                new Shader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode));
    }


    public void draw(Camera camera, float x, float y, float width, float height, AABB scissor) {
        if (!camera.isVisible(x,y, x+width,y+height)) return;
        float sx = x + width * 0.5f;
        float sy = y + height * 0.5f;
        initializeVertices();
        if (rotation!=0) evaluateRotation(rotation);
        evaluateScale(width, height);
        evaluateOffset(sx, sy);
        BatchRender.addTexturedQuad(program, null, vertices, texCoords, color, zOrder, scissor);
    }

    public void draw(Camera camera, float x, float y, float width, float height) {
        draw(camera,x,y,width,height,null);
    }

    public void draw(Camera camera, AABB aabb, AABB scissor) {
        draw(camera, aabb.minX, aabb.minY, aabb.width, aabb.height, scissor);
    }

    public void draw(Camera camera, AABB aabb) {
        draw(camera, aabb.minX, aabb.minY, aabb.width, aabb.height, null);
    }
}
