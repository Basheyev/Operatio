package com.axiom.atom.engine.graphics.renderers;

import android.opengl.GLES20;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.gles2d.Program;
import com.axiom.atom.engine.graphics.gles2d.Shader;

public class Line extends Quad {

    public int zOrder;

    private float lineThickness = 5f;

    protected static Program program = null;

    private final String vertexShaderCode =
            "uniform mat4 u_MVPMatrix; " +
                    "attribute vec4 vPosition;" +
                    "void main() { " +
                    "    gl_Position = u_MVPMatrix * vPosition; " +
                    "}";

    public Line() {
        if (program==null) program = new Program(
                new Shader(GLES20.GL_VERTEX_SHADER, vertexShaderCode),
                new Shader(GLES20.GL_FRAGMENT_SHADER, Shader.DEFAULT_FRAGMENT_SHADER_CODE));
    }


    public void draw(Camera camera, float x1, float y1, float x2, float y2, AABB scissor) {
        float tmp, minY, maxY;

        if (x1 > x2) {                                          // если x1 > x2
            tmp = x1; x1 = x2; x2 = tmp;                        // меняем координаты
            tmp = y1; y1 = y2; y2 = tmp;                        // точек местами
        }

        if (y1 > y2) {
            maxY = y1; minY = y2;
        } else {
            maxY = y2; minY = y1;
        }

        if (!camera.isVisible(x1,minY, x2,maxY)) return;

        float length = lineLength(x1, y1, x2, y2);              // Считаем длину линии
        float theta = (float) Math.atan((y2 - y1)/(x2 - x1));   // Считаем угол к оси X

        initializeVertices();                                   // Загружаем вершини
        evaluateOffset(0.5f, 0.0f);                        // Смещаем квадрат вправо
        evaluateScale(length, lineThickness);                   // Масштабируем на длину и тольщину
        evaluateRotation(theta);                                // Поворачаем на вычисленный угол
        evaluateOffset(x1, y1);                                 // Смещаем в координату x1, y1

        BatchRender.addQuad(program, vertices, color, zOrder, scissor);
    }


    public void draw(Camera camera, float x1, float y1, float x2, float y2) {
        draw(camera,x1,y1,x2,y2,null);
    }


    private float lineLength(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
    }

    public void setLineThickness(float w) {
        if (w<=0) w = 1;
        lineThickness = w;
    }

    public float getLineThickness() {
        return lineThickness;
    }

}
