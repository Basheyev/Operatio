package com.axiom.atom.engine.graphics.renderers;


import android.opengl.GLES20;


import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.gles2d.Program;
import com.axiom.atom.engine.graphics.gles2d.Shader;
import com.axiom.atom.engine.core.geometry.AABB;

/**
 * Отрисовывает прямоугольник
 * (С) Atom Engine, Bolat Basheyev 2020
 */
public class Rectangle extends Quad {

    public int zOrder;
    protected static Program program = null;

    private final String vertexShaderCode =
            "uniform mat4 u_MVPMatrix; " +
            "attribute vec4 vPosition;" +
            "void main() { " +
            "    gl_Position = u_MVPMatrix * vPosition; " +
            "}";

    public Rectangle() {
         if (program==null) program = new Program(
                new Shader(GLES20.GL_VERTEX_SHADER, vertexShaderCode),
                new Shader(GLES20.GL_FRAGMENT_SHADER, Shader.DEFAULT_FRAGMENT_SHADER_CODE));
    }


    public void draw(Camera camera, float x, float y, float width, float height, AABB scissor) {
        if (!camera.isVisible(x,y, x+width,y+height)) return;
        float sx = x + width * 0.5f;
        float sy = y + height * 0.5f;
        initializeVertices();
        if (rotation!=0) evaluateRotation(rotation);
        evaluateScale(width, height);
        evaluateOffset(sx, sy);
        BatchRender.addQuad(program, vertices, color, zOrder, scissor);
    }

    public void draw(Camera camera, float x, float y, float width, float height) {
        draw(camera,x,y,width,height,null);
    }

    public void draw(Camera camera, AABB aabb, AABB scissor) {
        draw(camera, aabb.min.x, aabb.min.y, aabb.width, aabb.height, scissor);
    }

    public void draw(Camera camera, AABB aabb) {
        draw(camera, aabb.min.x, aabb.min.y, aabb.width, aabb.height, null);
    }

}
