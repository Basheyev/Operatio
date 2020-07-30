package com.axiom.atom.engine.tests.shoottest.objects;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameObject;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Rectangle;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.input.Input;
import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.physics.PhysicsRender;
import com.axiom.atom.engine.tests.shoottest.ShooterScene;
import com.axiom.atom.engine.core.GameScene;


/**
 *
 */
public class Player extends GameObject {

    public boolean alive = true;      // Флаг живой ли персонаж

    float moveForce = 0.05f;
    float maxVelocity = 5f;

    public boolean AI = false;         // Флаг искусственного интеллекта
    public boolean jumping = false;    // Флаг прыжка
    public boolean crouching = false;  // Флаг приседания
    public int direction = 1;          // направление (вправо)
    public int idle, die, jump;        // индекс анимации покоя
    public int running, crouch;        // индекс анимации бега
    public int wound;                  // индекс анимации ранения
    public int health = 100;

    // debug
    private Rectangle debugRectangle;
    // AI
    public int decision = 0;
    public long lastDecisionTime;
    public long lastShotTime;

    public long dieTime, groundTime;

    public AABB standBounds;
    public AABB crouchBounds;
    public AABB dieBounds;

    public Player(GameScene gameScene, float scale) {
        super(gameScene);

        restitution = 0.2f;

        debugRectangle = new Rectangle();
        debugRectangle.zOrder = 3;
        debugRectangle.setColor(0.3f, 0.5f, 0.9f, 0.7f);

        sprite = new Sprite(gameScene.getResources(), R.drawable.player,6, 3);
        sprite.zOrder = 4;
        idle = sprite.addAnimation(12, 13, 6,true);
        running = sprite.addAnimation( 0, 5,6, true);
        die = sprite.addAnimation(6, 11, 6,false);
        wound = sprite.addAnimation( 6, 7, 6, false);
        crouch = sprite.addAnimation( 14, 14,6, false);
        jump = sprite.addAnimation(15, 16,6, true);

        this.scale = scale;
        standBounds = new AABB(-10 * scale,-24 * scale,10 * scale,20 * scale);
        crouchBounds = new AABB(-8 * scale,-24 * scale,10 * scale,4 * scale);
        dieBounds = new AABB(-8 * scale, -24 * scale, +8 * scale, -14 * scale);
        bodyType = PhysicsRender.BODY_DYNAMIC;
        setLocalBounds(standBounds);

        sprite.setActiveAnimation(idle);
    }


    public void crouch() {
        if (!alive) return;
        if (!jumping && !crouching) {
            velocity.x = 0;
            crouching = true;
            sprite.setActiveAnimation(crouch);
            setLocalBounds(crouchBounds);
        }
    }

    public void left() {
        if (!alive) return;
        direction = -1;
        velocity.x -= moveForce * scale * Math.abs(Input.xAxis);
        if (!jumping && sprite.getActiveAnimation()!=running) {
            sprite.setActiveAnimation(running);
        }
        if (velocity.x < -maxVelocity * scale) velocity.x = -maxVelocity * scale;
        setLocalBounds(standBounds);
    }

    public void right() {
        if (!alive) return;
        direction = 1;
        velocity.x += moveForce * scale * Math.abs(Input.xAxis);
        if (!jumping && sprite.getActiveAnimation()!=running) {
            sprite.setActiveAnimation(running);
        }
        if (velocity.x > maxVelocity * scale) velocity.x = maxVelocity * scale;
        setLocalBounds(standBounds);
    }

    public void jump() {
        if (!alive) return;
        velocity.y = moveForce * 15 * scale;
        if (velocity.y > maxVelocity * 15 * scale) velocity.y = maxVelocity * 15 * scale;

        if (sprite.getActiveAnimation()!= jump) {
            sprite.setActiveAnimation(jump);
            if ((this==((ShooterScene)scene).player) && (!jumping)) {
                jumping = true;
            }
        }
        setLocalBounds(standBounds);
    }

    public void fire() {
        if (!alive) return;
        if (System.nanoTime()-lastShotTime<400000000) return;
        lastShotTime = System.nanoTime();
        Bullet bullet = new Bullet(scene, scale / 2);
        bullet.x = x + (sprite.getWidth() * scale / 2.5f) * direction;
        if (crouching) {
            bullet.y = y - 7.5f * scale;
        } else {
            bullet.y = y + 7.5f * scale;
        }
        bullet.velocity.x = Bullet.bulletSpeed * direction * scale;
        bullet.velocity.y = 0;
        scene.addObject(bullet);
    }

    public void damage(int damage) {
        health -= damage;
        if (health<=0) {
            sprite.setActiveAnimation(die);
            setLocalBounds(dieBounds);
            alive = false;
            dieTime = System.nanoTime();
        } else {
            sprite.setActiveAnimation(wound);
        }
    }

    @Override
    public void update(float deltaTime) {
        if (!alive) {
           /** if (System.nanoTime()-dieTime>60000000000l) {
                scene.removeObject(this);
            }*/
            return;
        }
        if (AI) {
           artificalIntelligence(deltaTime);
        } else {
            if (Input.xAxis < -0.2f) left(); else
            if (Input.xAxis > 0.2f) right();
            if (Input.yAxis > 0.2f) jump(); else
            if (Input.yAxis < -0.2f) crouch();

            if ((Math.abs(Input.xAxis)  <= 0.2f) && (Math.abs(Input.yAxis) <= 0.2f)) {
                sprite.setActiveAnimation(idle);
                crouching = false;
                setLocalBounds(standBounds);
            }

            if (Input.BButton) fire();


        }
        debugRectangle.setColor(0.3f, 0.5f, 0.9f, 0.5f);
    }


    private void artificalIntelligence(float deltaTime) {
        long currentTime = System.nanoTime();
        if (currentTime - lastDecisionTime > 1000000000) {
            lastDecisionTime = currentTime;
            decision = (int) (Math.random() * 5);
        }
        switch (decision) {
            case 0:
                left(); crouching = false;
                break;
            case 1:
                right(); crouching = false;
                break;
            case 2:
                jump(); crouching = false;
                break;
            case 3:
                crouch();
                break;
            case 4:
                //fire();
                crouching = false;
                break;
            default:
                crouching = false;
        }
    }

    @Override
    public void onCollision(GameObject object) {
        if (!alive) return;
        if (PhysicsRender.getCollisionNormal(this, object)== PhysicsRender.NV_BOTTOM) {
            if (sprite.getActiveAnimation()== jump && alive==true) {
                sprite.setActiveAnimation(idle);
            }
            jumping = false;
            groundTime = System.nanoTime();
        }
        debugRectangle.setColor(0.9f, 0.3f, 0.3f, 0.5f);
    }

    @Override
    public void draw(Camera camera) {
        if (direction==1) {
           sprite.flipHorizontally(false);
        }
        else {
           sprite.flipHorizontally(true);
        }
        if (((ShooterScene)scene).debug) {
            AABB bounds = getWorldBounds();
            debugRectangle.draw(camera, bounds.min.x, bounds.min.y, bounds.width, bounds.height);
        }
        sprite.draw(camera,x,y,scale);
    }

}
