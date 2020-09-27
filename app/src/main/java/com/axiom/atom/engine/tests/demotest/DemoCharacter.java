package com.axiom.atom.engine.tests.demotest;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameObject;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;

public class DemoCharacter extends GameObject {

    public float direction = 0;
    private int walking;
    private int death;
    private int crouch;

    public DemoCharacter(GameScene gameScene, float scale) {
        super(gameScene);
        sprite = new Sprite(gameScene.getResources(), R.drawable.player, 6, 3);
        sprite.zOrder = 1;
        walking = sprite.addAnimation(0,5, 6, true);
        death = sprite.addAnimation(6,11, 6, true);
        crouch = sprite.addAnimation(14,14,6, true);
        sprite.setActiveAnimation(walking);

        x = Camera.WIDTH / 2;
        y = Camera.HEIGHT / 2;
        this.scale = scale;
        float hw = sprite.getWidth() * scale / 2;
        float hh = sprite.getHeight() * scale / 2;

        setLocalBounds(0-hw, 0-hh, hw, hh);
    }

    @Override
    public void update(float deltaTime) {

    }

    private float step = 0.005f;
    @Override
    public void draw(Camera camera) {
        if (direction==-1) {
            sprite.flipHorizontally(true);
        }
        else if (direction==1) {
            sprite.flipHorizontally(false);
        }
        sprite.setRotation(sprite.getRotation() + 0.01f);
        float alpha = sprite.getAlpha() - step;
        if (alpha<0 || alpha>1) {
            step = -step;
        }
        sprite.setAlpha(alpha);
        sprite.draw(camera, x, y, scale);
    }


    @Override
    public void onCollision(GameObject object) {

    }


    public void walk() {
        if (sprite.getActiveAnimation()==walking) return;
        sprite.setActiveAnimation(walking);
    }

    public void crouch() {
        if (sprite.getActiveAnimation()==crouch) return;
        sprite.setActiveAnimation(crouch);
    }

    public void death() {
        if (sprite.getActiveAnimation()==death) return;
        sprite.setActiveAnimation(death);
    }
}
