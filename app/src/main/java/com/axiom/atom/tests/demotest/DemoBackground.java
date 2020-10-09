package com.axiom.atom.tests.demotest;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameObject;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Rectangle;
import com.axiom.atom.engine.graphics.renderers.Sprite;

public class DemoBackground extends GameObject {

    Rectangle r;

    public DemoBackground(GameScene gameScene) {
        super(gameScene);
        sprite = new Sprite(gameScene.getResources(), R.drawable.background);
        r = new Rectangle();
        r.setColor(0.9f,0.8f,0.1f,1.0f);
    }

    @Override
    public void update(float deltaTime) {

    }

    @Override
    public void draw(Camera camera) {
        sprite.draw(camera, 0,0,1920,1080, null);
    }


    @Override
    public void onCollision(GameObject object) {

    }
}
