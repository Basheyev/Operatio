package com.axiom.atom.engine.physics.geometry;

/**
 * 2D вектор и операции над ним<BR>
 * (C) Atom Engine, Bolat Basheyev 2020
 */
public class Vector {

    public float x = 0.0f;
    public float y = 0.0f;

    public Vector() { }

    public Vector(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void add(Vector vec) {
        x += vec.x;
        y += vec.y;
    }

    public void sub(Vector vec) {
        x -= vec.x;
        y -= vec.y;
    }

    public void mul(float m) {
        x *= m;
        y *= m;
    }

    public float dotProduct(Vector vec) {
        return (x*vec.x + y*vec.y);
    }

    public void copy(Vector vec) {
        x = vec.x;
        y = vec.y;
    }

    public float length() {
        return (float) Math.sqrt(x*x + y*y);
    }

    public void setValue(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
