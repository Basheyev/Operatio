package com.axiom.atom.engine.graphics.gles2d;

import android.opengl.GLES20;

import com.axiom.atom.engine.graphics.GraphicsRender;

/**
 * Программа шейдеров
 * (С) Atom Engine, Bolat Basheyev 2020-2022
 */
public class Program implements GLESObject {

    public static final String VERTICES = "vPosition";
    public static final String TEXCOORD = "TexCoordIn";
    public static final String COLOR = "vColor";
    public static final String MATRIX = "u_MVPMatrix";

    protected int programID = -1;
    protected Shader vertexShader;
    protected Shader fragmentShader;
    protected boolean initialized;

    public Program(Shader vertexShader, Shader fragmentShader) {
        this.vertexShader = vertexShader;
        this.fragmentShader = fragmentShader;
        GraphicsRender.addToLoadQueue(this);
        initialized = false;
    }

    //---------------------------------------------------------------------------

    @Override
    public void loadToGPU() {
        programID = GLES20.glCreateProgram();
        GLES20.glAttachShader(programID, vertexShader.getShaderID());
        GLES20.glAttachShader(programID, fragmentShader.getShaderID());
        GLES20.glLinkProgram(programID);
        initialized = true;
    }

    @Override
    public void deleteFromGPU() {
        GLES20.glDeleteProgram(programID);
        initialized = false;
    }


    //---------------------------------------------------------------------------

    public int getProgramID() {
        return programID;
    }

    public void use() {
        if (!initialized) return;
        GLES20.glUseProgram(programID);
    }


    public int setAttribVertexArray(String name, VertexBuffer vertexBuffer) {
        if (!initialized) return -1;
        int handler = GLES20.glGetAttribLocation(programID, name);
        GLES20.glEnableVertexAttribArray(handler);
        GLES20.glVertexAttribPointer(handler,
                vertexBuffer.coordinatesPerVertex,
                GLES20.GL_FLOAT,
                false,
                vertexBuffer.vertexStride,
                vertexBuffer.buffer);
        return handler;
    }

    public int setUniformFloatValue(String name, float value) {
        int handler = GLES20.glGetUniformLocation(programID, name);
        GLES20.glUniform1f(handler, value);
        return handler;
    }

    public int setUniformIntValue(String name, int value) {
        int handler = GLES20.glGetUniformLocation(programID, name);
        GLES20.glUniform1i(handler, value);
        return handler;
    }

    public int setUniformVec4Value(String name, float[] value) {
        int handler = GLES20.glGetUniformLocation(programID, name);
        GLES20.glUniform4fv(handler, 1, value, 0);
        return handler;
    }

    public int setUniformMat4Value(String name, float[] value) {
        int handler = GLES20.glGetUniformLocation(programID, name);
        GLES20.glUniformMatrix4fv(handler, 1, false, value, 0);
        return handler;
    }

    public void disableVertexArray(int vertexHandler) {
        if (!initialized) return;
        GLES20.glDisableVertexAttribArray(vertexHandler);
    }



}
