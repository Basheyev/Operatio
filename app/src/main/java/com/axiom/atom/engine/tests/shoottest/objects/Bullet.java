package com.axiom.atom.engine.tests.shoottest.objects;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameObject;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Rectangle;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.physics.geometry.AABB;
import com.axiom.atom.engine.physics.PhysicsRender;
import com.axiom.atom.engine.tests.shoottest.ShooterScene;


/**
 */
public class Bullet extends GameObject {

    public static final float bulletSpeed = 8f;

    public Sprite sprite = null;

    private long startTime;
    private int flying, hit;
    private Rectangle debugRectangle;

    public Bullet(GameScene gameScene, float scale) {
        super(gameScene);
        bodyType = PhysicsRender.BODY_KINEMATIC;

        sprite = new Sprite(gameScene.getResources(), R.drawable.fireball,4, 1);
        sprite.zOrder = 7;
        flying = sprite.addAnimation(0,0, 6,true);
        hit = sprite.addAnimation(1,3, 6,false);
        sprite.setActiveAnimation(flying);

        this.scale = scale;
        setLocalBounds(-8 * scale,-2 * scale, 8 * scale, 2 * scale);

        debugRectangle = new Rectangle();
        debugRectangle.setColor(0.3f, 0.5f, 0.9f, 0.7f);
        debugRectangle.zOrder = 8;
        startTime = System.nanoTime();
    }

    @Override
    public void draw(Camera camera) {
        if (((ShooterScene)scene).debug) {
            AABB bounds = getWorldBounds();
            debugRectangle.draw(camera, bounds.min.x, bounds.min.y, bounds.width, bounds.height);
        }
        sprite.draw(camera, x, y, scale);
    }

    @Override
    public void update(float deltaTime) {
        if (System.nanoTime()-startTime > 1000000000l) {
            active = false;
            scene.removeObject(this);
        }
        if (sprite.getActiveAnimation()==hit && sprite.getTimesPlayed()>0) {
            scene.removeObject(this);
        }

    }

    @Override
    public void onCollision(GameObject object) {
        if (object instanceof Player) {
            ShooterScene sc = (ShooterScene) scene;
            Player plr = (Player) object;
            if (!plr.alive) {
                velocity.x = 0;
                velocity.y = 0;
                bodyType = PhysicsRender.BODY_VOID;
                sprite.setActiveAnimation(hit);
                return;
            }
            if (plr.sprite.getActiveAnimation()!=sc.player.die) {
                plr.damage(20);
                velocity.x = 0;
                velocity.y = 0;
                bodyType = PhysicsRender.BODY_VOID;
                sprite.setActiveAnimation(hit);
            }
        } else {
            velocity.x = 0;
            velocity.y = 0;
            bodyType = PhysicsRender.BODY_VOID;
            sprite.setActiveAnimation(hit);
        }
    }

}
