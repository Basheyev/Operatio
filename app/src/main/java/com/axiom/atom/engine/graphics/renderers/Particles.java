package com.axiom.atom.engine.graphics.renderers;

import android.graphics.Rect;
import android.provider.Telephony;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.data.Channel;
import com.axiom.atom.engine.graphics.gles2d.Camera;

// TODO Добить базовые частицы
public class Particles {

    public static class Particle {
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
    private Particle[] particles;
    private int amount;
    private float speed;
    private long lifeTime;
    public int zOrder = 0;


    public Particles(Sprite image, int amount, long lifeTimeMs, float speed) {
        if (image!=null) sprite = image; else rectangle = new Rectangle();
        particles = new Particle[amount];
        for (int i=0; i<particles.length; i++) particles[i] = new Particle();
        this.amount = amount;
        this.speed = speed;
        this.lifeTime = lifeTimeMs;
        generateParticles();
    }


    public void draw(Camera camera, float x, float y, AABB scissor) {
        Particle particle;
        float particleSize;

        if (!processParticles()) return;

        if (sprite==null) {
            for (int i = 0; i < particles.length; i++) {
                particle = particles[i];
                if (particle.visible) {
                    particleSize = 32 * particle.scale;
                    rectangle.zOrder = zOrder;
                    rectangle.setRotation(particle.rotation);
                    rectangle.setAlpha(particle.alpha);
                    rectangle.draw(camera, x + particle.x - particleSize/2,
                            y + particle.y - particleSize/2,
                            particleSize, particleSize, scissor);
                }
            }
        } else {
            for (int i = 0; i < particles.length; i++) {
                particle = particles[i];
                if (particle.visible) {
                    particleSize = 32 * particle.scale;
                    sprite.zOrder = zOrder;
                    sprite.setRotation(particle.rotation);
                    sprite.setAlpha(particle.alpha);
                    sprite.draw(camera, x + particle.x - particleSize/2,
                            y + particle.y - particleSize/2,
                            particleSize, particleSize, scissor);
                }
            }
        }

    }


    public void draw(Camera camera, float x, float y) {
        draw(camera,x,y, null);
    }


    private long lastTime;


    private boolean processParticles() {
        Particle particle;
        long passedTime;
        boolean visiblesLeft = false;
        long currentTime = System.currentTimeMillis();
        float delta = (currentTime - lastTime) / 1000.0f;         // Прошедшие доли секунд

        for (int i=0; i<amount; i++) {
            particle = particles[i];
            if (particle.visible) {
                passedTime = currentTime - particle.birthTime;
                if (passedTime > lifeTime) particle.visible = false;
                particle.alpha = 1.0f - ((float) passedTime) / ((float) lifeTime);
                particle.rotation += 0.1f;
                particle.x += (particle.vx * delta);
                particle.y += (particle.vy * delta);
                visiblesLeft = true;
            }
        }
        lastTime = currentTime;
        return visiblesLeft;
    }


    public void generateParticles() {
        Particle particle;
        float angle, spread;
        for (int i=0; i<amount; i++) {
            particle = particles[i];
            angle = (float) (Math.random() * 2 * Math.PI);
            spread = (float) Math.random() * 0.6f + 0.4f;
            particle.vx = (float) Math.cos(angle) * speed * spread;
            particle.vy = (float) Math.sin(angle) * speed * spread;
            particle.x = (float) (particle.vx * Math.random());
            particle.y = (float) (particle.vy * Math.random());
            particle.rotation = (float) (angle - Math.PI/2);
            particle.scale = (float) Math.random() * 2 + 0.5f;
            particle.alpha = 1;
            particle.birthTime = System.currentTimeMillis();
            particle.visible = true;
        }
        lastTime = System.currentTimeMillis();
    }



}
