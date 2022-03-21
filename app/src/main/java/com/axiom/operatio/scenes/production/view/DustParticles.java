package com.axiom.operatio.scenes.production.view;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Rectangle;
import com.axiom.atom.engine.graphics.renderers.Sprite;

public class DustParticles {

    private static class MoneyParticle {
        public float x, y;
        public float vx, vy;
        public float rotation = 0;
        public float scale = 1.0f;
        public float alpha = 1.0f;
        public long birthTime = 0;
        public boolean visible = false;
    }

    private Sprite sprite;
    private Rectangle rectangle;
    private final MoneyParticle[] moneyParticles;
    private final int amount;
    private final float speed;
    private final long lifeTime;
    private long lastTime;
    public int zOrder = 0;


    public DustParticles(Sprite image, int amount, long lifeTimeMs, float speed) {
        if (image!=null) sprite = image; else rectangle = new Rectangle();
        moneyParticles = new MoneyParticle[amount];
        for (int i = 0; i< moneyParticles.length; i++) moneyParticles[i] = new MoneyParticle();
        this.amount = amount;
        this.speed = speed;
        this.lifeTime = lifeTimeMs;
    }


    public void draw(Camera camera, float x, float y, float scaleFactor, AABB scissor) {
        MoneyParticle moneyParticle;
        float particleSize;

        if (!processParticles(scaleFactor / 16)) return;

        if (sprite==null) {
            for (int i = 0; i < moneyParticles.length; i++) {
                moneyParticle = moneyParticles[i];
                if (moneyParticle.visible) {
                    particleSize = scaleFactor * moneyParticle.scale;
                    rectangle.setZOrder(zOrder);
                    rectangle.setRotation(moneyParticle.rotation);
                    rectangle.setAlpha(moneyParticle.alpha);
                    rectangle.draw(camera, x + moneyParticle.x - particleSize * 0.5f,
                            y + moneyParticle.y - particleSize * 0.5f,
                            particleSize, particleSize, scissor);
                }
            }
        } else {
            for (int i = 0; i < moneyParticles.length; i++) {
                moneyParticle = moneyParticles[i];
                if (moneyParticle.visible) {
                    particleSize = scaleFactor * moneyParticle.scale;
                    sprite.setZOrder(zOrder);
                    sprite.setRotation(moneyParticle.rotation);
                    sprite.setAlpha(moneyParticle.alpha);
                    sprite.draw(camera, x + moneyParticle.x - particleSize * 0.5f,
                            y + moneyParticle.y - particleSize * 0.5f,
                            particleSize, particleSize, scissor);
                }
            }
        }

    }


    public void draw(Camera camera, float x, float y, float particleSize) {
        draw(camera,x,y, particleSize, null);
    }



    private boolean processParticles(float speedScale) {
        MoneyParticle moneyParticle;
        long passedTime;
        boolean visiblesLeft = false;

        long currentTime = System.currentTimeMillis();
        float delta = (currentTime - lastTime) / 1000.0f;         // Прошедшие доли секунды

        for (int i=0; i<amount; i++) {
            moneyParticle = moneyParticles[i];
            if (moneyParticle.visible) {
                passedTime = currentTime - moneyParticle.birthTime;
                if (passedTime > lifeTime) moneyParticle.visible = false;
                moneyParticle.alpha = 1.0f - ((float) passedTime) / ((float) lifeTime);
                moneyParticle.rotation += 0.1f;
                moneyParticle.x += (moneyParticle.vx * delta * speedScale);
                moneyParticle.y += (moneyParticle.vy * delta * speedScale);
                visiblesLeft = true;
            }
        }
        lastTime = currentTime;
        return visiblesLeft;
    }


    public void generateParticles() {
        MoneyParticle moneyParticle;
        float angle, spread;
        for (int i=0; i<amount; i++) {
            moneyParticle = moneyParticles[i];
            angle = (float) (Math.random() * 2 * Math.PI);
            spread = (float) Math.random() * 0.6f + 0.4f;
            moneyParticle.vx = (float) Math.cos(angle) * speed * spread;
            moneyParticle.vy = (float) Math.sin(angle) * speed * spread;
            moneyParticle.x = (float) (moneyParticle.vx * Math.random());
            moneyParticle.y = (float) (moneyParticle.vy * Math.random());
            moneyParticle.rotation = (float) (angle - Math.PI/2);
            moneyParticle.scale = (float) Math.random() * 2 + 0.5f;
            moneyParticle.alpha = 1;
            moneyParticle.birthTime = System.currentTimeMillis();
            moneyParticle.visible = true;
        }
        lastTime = System.currentTimeMillis();
    }



}
