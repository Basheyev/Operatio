package com.basheyev.atom.engine.graphics.gles2d;

import android.opengl.GLES20;
import android.util.Log;

import com.basheyev.atom.engine.graphics.GraphicsRender;

/**
 * Инкапсулирует вершинный или пиксельный шейдер.<br>
 * Реализует типовые операции над шейдерами.
 * (C) Atom Engine, Bolat Basheyev 2020
 */
public class Shader implements GLESObject {

    protected int shaderID;
    protected int shaderType;
    protected String shaderCode;

    /**
     * Создаёт и компилирует вершинный или пиксельный шейдер
     * @param type GLES20.GL_VERTEX_SHADER или GLES20.GL_FRAGMENT_SHADER
     * @param code исходный код шейдера
     */
    public Shader(int type, String code) {
        shaderType = type;
        shaderCode = code;
        if (shaderCode==null) {
            if (type==GLES20.GL_VERTEX_SHADER)
                shaderCode = DEFAULT_VERTEX_SHADER_CODE;
            if (type==GLES20.GL_FRAGMENT_SHADER)
                shaderCode = DEFAULT_FRAGMENT_SHADER_CODE;
        }
        GraphicsRender.addToLoadQueue(this);
    }

    @Override
    public void loadToGPU() {
        shaderID = GLES20.glCreateShader(shaderType);
        if (shaderID==0) Log.e ("SHADER", "Failed to create shader.");
        GLES20.glShaderSource(shaderID, shaderCode);
        GLES20.glCompileShader(shaderID);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shaderID, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0)
        {
            Log.e ("SHADER", "Failed to compile shader: " + shaderCode + "\n" +
                    GLES20.glGetShaderInfoLog(shaderID));
            GLES20.glDeleteShader(shaderID);
            shaderID = 0;
        }

    }


    @Override
    public void deleteFromGPU() {
        GLES20.glDeleteShader(shaderID);
    }

    /**
     * Возвращает ID шейдера
     * @return ID шейдера
     */
    public int getShaderID() {
        return shaderID;
    }


    public static final String DEFAULT_VERTEX_SHADER_CODE =
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    "    gl_Position = vPosition;" +
                    "}";

    public static final String DEFAULT_FRAGMENT_SHADER_CODE =
                    "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "    gl_FragColor = vColor;" +
                    "}";
}