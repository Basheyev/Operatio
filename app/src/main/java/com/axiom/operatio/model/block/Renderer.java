package com.axiom.operatio.model.block;

import com.axiom.atom.engine.graphics.gles2d.Camera;

public interface Renderer {

    void draw(Camera camera, float x, float y, float width, float height);

}
