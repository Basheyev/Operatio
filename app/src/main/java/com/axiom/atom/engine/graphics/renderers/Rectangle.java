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
public class Rectangle {

    public int zOrder;
    protected static Program program = null;
    protected static float[] vertices = new float[18];
    protected float[] color = { 0.3f, 0.5f, 0.9f, 1.0f };

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

    public void setColor(float r, float g, float b, float a) {
        color[0] = r;
        color[1] = g;
        color[2] = b;
        color[3] = a;
    }


    public void draw(Camera camera, float x, float y, float width, float height, AABB scissor) {
        if (!camera.isVisible(x,y, x+width,y+height)) return;

        float sx = x + width / 2;
        float sy = y + height / 2;

        // Triangle 1
        vertices[0] = -0.5f * width + sx;
        vertices[1] = 0.5f * height + sy;
        vertices[3] = -0.5f * width + sx;
        vertices[4] = -0.5f * height + sy;
        vertices[6] = 0.5f * width + sx;
        vertices[7] = 0.5f * height + sy;
        // Triangle 2
        vertices[9] = -0.5f * width + sx;
        vertices[10] = -0.5f * height + sy;
        vertices[12] = 0.5f * width + sx;
        vertices[13] = 0.5f * height + sy;
        vertices[15] = 0.5f * width + sx;
        vertices[16] = -0.5f * height + sy;

        BatchRender.addQuad(program, vertices, zOrder, color, scissor);

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
