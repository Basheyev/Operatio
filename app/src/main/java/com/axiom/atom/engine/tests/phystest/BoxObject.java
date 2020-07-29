package com.axiom.atom.engine.tests.phystest;

import com.axiom.atom.engine.core.GameObject;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Rectangle;
import com.axiom.atom.engine.physics.geometry.AABB;

public class BoxObject extends GameObject {

    private static Rectangle rectangle;
    boolean colliding;

    public BoxObject(GameScene gameScene) {
        super(gameScene);
        if (rectangle==null) rectangle = new Rectangle();
        restitution = 0.2f;
    }

    @Override
    public void draw(Camera camera) {
        AABB box = getWorldBounds();
        rectangle.setColor(0.3f,0.3f,0.6f,0.5f);
        rectangle.draw(camera, box);
        if (colliding)
            rectangle.setColor(1,0,0,0.5f);
        else
            rectangle.setColor(1,1,0,0.5f);
        rectangle.draw(camera,
                box.min.x+2,
                box.min.y+2,
                box.width-4,
                box.height-4);
        colliding = false;
    }

    @Override
    public void update(float deltaTime) {

    }

    @Override
    public void onCollision(GameObject object) {
        colliding = true;
    }
}
