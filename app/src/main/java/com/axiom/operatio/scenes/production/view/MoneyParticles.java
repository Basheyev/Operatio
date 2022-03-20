package com.axiom.operatio.scenes.production.view;

import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.operatio.model.common.FormatUtils;
import com.axiom.operatio.model.production.ProductionRenderer;

public class MoneyParticles {

    private static class MoneyParticle {
        public StringBuffer valueText;
        public double value;
        public float x, y;
        public float vx, vy;
        public float scale = 1.0f;
        public float alpha = 1.0f;
        public long birthTime = 0;
        public int zOrder;
        public boolean visible = false;
    }

    private static final int MAX_PARTICLES = 32;
    private static final long TIME_TO_LIVE = 1500;
    private final MoneyParticle[] particles;
    private final float speed;
    private long lastTime;


    public MoneyParticles(int maxParticles, float speed) {
        this.speed = speed;
        particles = new MoneyParticle[maxParticles];
        for (int i=0; i<particles.length; i++) {
            particles[i] = new MoneyParticle();
            particles[i].valueText = new StringBuffer(32);
        }
    }


    public void addParticle(double value, float x, float y, int zOrder) {
        MoneyParticle particle;
        for (int i=0; i<particles.length; i++) {
            particle = particles[i];
            if (!particle.visible) {
                particle.x = x;
                particle.y = y;
                particle.vx = 0;
                particle.vy = speed;
                particle.value = value;
                particle.valueText.setLength(0);
                FormatUtils.formatMoneyAppend(Math.round(value), particle.valueText);
                particle.birthTime = System.currentTimeMillis();
                particle.zOrder = zOrder;
                particle.visible = true;
                break;
            }
        }
    }


    private boolean process(float speedScale) {
        MoneyParticle particle;
        long passedTime;
        boolean visiblesLeft = false;

        long currentTime = System.currentTimeMillis();
        float delta = (currentTime - lastTime) / 1000.0f;         // Прошедшие доли секунды

        for (int i=0; i<particles.length; i++) {
            particle = particles[i];
            if (particle.visible) {
                passedTime = currentTime - particle.birthTime;
                if (passedTime > TIME_TO_LIVE) particle.visible = false;
                particle.alpha = 1.0f - ((float) passedTime) / ((float) TIME_TO_LIVE);
                particle.x += (particle.vx * delta * speedScale);
                particle.y += (particle.vy * delta * speedScale);
                visiblesLeft = true;
            }
        }
        lastTime = currentTime;
        return visiblesLeft;
    }


    public void draw(float scaleFactor) {
        MoneyParticle particle;
        float particleSize;
        float r,g,b;

        // Переместить частицы денег
        process(scaleFactor / 16);

        for (int i = 0; i < particles.length; i++) {
            particle = particles[i];
            r = g = b = 0.05f;
            if (particle.visible) {
                if (particle.value < 0) r = 0.3f; else g = 0.3f;
                particleSize = scaleFactor * particle.scale;
                GraphicsRender.setZOrder(particle.zOrder);
                GraphicsRender.setColor(r, g, b, particle.alpha);
                GraphicsRender.drawText(particle.valueText, particle.x,
                        particle.y + particleSize * 0.5f,
                        2, null);

            }
        }
    }

}
