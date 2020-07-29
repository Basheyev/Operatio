package com.axiom.atom.engine.tests.shoottest;


import android.opengl.GLES20;
import android.view.MotionEvent;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Batcher;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.input.Input;
import com.axiom.atom.engine.physics.PhysicsRender;
import com.axiom.atom.engine.tests.shoottest.objects.Player;
import com.axiom.atom.engine.tests.shoottest.objects.TileMap;

import javax.microedition.khronos.opengles.GL10;

import static com.axiom.atom.engine.graphics.GraphicsRender.camera;


public class ShooterScene extends GameScene {
    public Player player;
    public Sprite joystick;
    public TileMap tileMap;
    public boolean debug = true;

    @Override
    public String getSceneName() {
        return "Shooter";
    }

    @Override
    public void startScene() {
        joystick = new Sprite(getResources(), R.drawable.joystick, 2,2);
        tileMap = new TileMap(this, 5);
        for (int i=1; i<2; i++) {
            player = new Player(this, 8f);
            player.x = i * 4 * player.getLocalBounds().width;
            player.y = 700;
            player.AI = true;
            addObject(player);
        }
        player.AI = false;
        player.direction = 1;
    }

    @Override
    public void updateScene(float deltaTime) {

        // плавное движение камеры в сторону нажатия
        float dx = (player.x - camera.x) * 0.1f;
        float dy = (player.y - camera.y) * 0.1f;
        camera.x += dx;
        camera.y += dy;

        camera.lookAt(camera.x, camera.y);

        PhysicsRender.doPhysics(this, deltaTime);
    }

    @Override
    public void preRender(Camera camera) {
        GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public void postRender(Camera camera) {
        // В текстуре все кнопочки
        // A
        joystick.zOrder = 10;
        joystick.setActiveFrame(0);
        joystick.draw(camera,
                camera.x1 + camera.SCREEN_WIDTH * 0.9f,
                camera.y1 + camera.SCREEN_HEIGHT * 0.25f,
                2 + (Input.AButton ? 0.5f : 0));
        // B
        joystick.setActiveFrame(1);
        joystick.draw(camera,
                camera.x1 + camera.SCREEN_WIDTH * 0.7f,
                camera.y1 + camera.SCREEN_HEIGHT * 0.25f,
                2 + (Input.BButton ? 0.5f : 0));

        // Joystick border
        joystick.setActiveFrame(3);
        joystick.draw(camera,
                camera.x1 + camera.SCREEN_WIDTH * 0.2f,
                camera.y1 + camera.SCREEN_HEIGHT * 0.3f,4);

        // Joystick
        joystick.setActiveFrame(2);
        joystick.draw(camera,
                camera.x1 + camera.SCREEN_WIDTH * 0.2f + Input.xAxis * (joystick.getWidth()),
                camera.y1 + camera.SCREEN_HEIGHT * 0.3f + Input.yAxis * (joystick.getHeight()),
                1);

        GraphicsRender.setZOrder(100);
        GraphicsRender.drawText(
                ("FPS:" + GraphicsRender.getFPS() +
                        " CALLS=" + Batcher.getDrawCallsCount()).toCharArray(),
                camera.x1+50, camera.y1+1040, 2);
    }

    @Override
    public void onMotion(MotionEvent event, float worldX, float worldY) {
        if (Input.AButton) getSceneManager().exitGame();
    }

    @Override
    public void dispose() {

    }
}
