package com.axiom.atom.tests.demotest;

import android.util.Log;
import android.view.MotionEvent;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.BatchRender;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.input.Input;

public class DemoScene extends GameScene {

    Sprite background;
    DemoCharacter person;

    Sprite joystick;

    public String getSceneName() {
        return "Demo";
    }

    /**
     * Вызывается из потока GLThreads чтобы шейдеры загрузились
     */
    public void startScene() {

        long startTime = System.currentTimeMillis();
        background = new Sprite(getResources(), R.drawable.background);
        background.zOrder = 0;
        joystick = new Sprite(getResources(), R.drawable.joystick, 2,2);
        joystick.zOrder = 10;

        for (int j=1; j<2; j++)
        for (int i=1; i<2; i++) {
            person = new DemoCharacter(this, 10);
            person.x = i * 200;
            person.y = j * 400;
            addObject(person);
        }

        Log.i ("DEMO SCENE LOADER", (System.currentTimeMillis() - startTime) + "ms");

    }


    public void updateScene(float deltaTime) {
        person.x += Input.xAxis * 3;  // * delta time (render time)
        person.y += Input.yAxis * 3;  // * delta time (render time)

        if (Input.xAxis < -0.2f) {
            person.direction = -1;
        } else if (Input.xAxis > 0.2f) {
            person.direction = 1;
        }


        if (person.x < 0) person.x = 0;
        if (person.x > 1920) person.x = 1920;
        if (person.y < 0) person.y = 0;
        if (person.y > 1080) person.y = 1080;

        if (Input.AButton) person.crouch(); else
        if (Input.BButton) person.death(); else person.walk();

    }

    @Override
    public void preRender(Camera camera) {
        //GraphicsRender.clear();
        background.draw(camera, 0,0,1920,1080);
    }

    @Override
    public void postRender(Camera camera) {
        // В текстуре все кнопочки
        // A
        joystick.setActiveFrame(0);
        joystick.draw(camera, Camera.WIDTH * 0.9f,
                camera.HEIGHT * 0.25f,2 + (Input.AButton ? 0.5f : 0));
        // B
        joystick.setActiveFrame(1);
        joystick.draw(camera, Camera.WIDTH * 0.7f,
                camera.HEIGHT * 0.25f,2 + (Input.BButton ? 0.5f : 0));

        // Joystick border
        joystick.setActiveFrame(3);
        joystick.draw(camera, Camera.WIDTH * 0.2f, Camera.HEIGHT * 0.3f,4);

        // Joystick
        joystick.setActiveFrame(2);
        joystick.draw(camera,
                Camera.WIDTH * 0.2f + Input.xAxis * (joystick.getWidth()),
                Camera.HEIGHT * 0.3f + Input.yAxis * (joystick.getHeight()),
                1);

        GraphicsRender.drawText(
                ("FPS:" + GraphicsRender.getFPS() +
                        " CALLS=" + BatchRender.getDrawCallsCount() +
                        " QUADS=" + BatchRender.getEntriesCount() +
                        " MS:" + GraphicsRender.getRenderTime()),
                camera.getMinX()+50, camera.getMinY()+1040, 2);
    }

    @Override
    public void onMotion(MotionEvent event, float worldX, float worldY) {

    }

    public void disposeScene() {

    }

}
