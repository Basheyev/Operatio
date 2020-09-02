package com.axiom.atom.engine.tests.demotest;

import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.BatchRender;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.atom.engine.input.Input;

public class DemoScene extends GameScene {

    Sprite background;
    Widget panel = new Panel();
    DemoCharacter person;

    Sprite joystick;

    public String getSceneName() {
        return "Demo";
    }

    /**
     * Вызывается из потока GLThreads чтобы шейдеры загрузились
     */
    public void startScene() {

        Widget widget = getSceneWidget();

        ClickListener listener = new ClickListener() {
            @Override
            public void onClick(Widget w) {
                Log.i("INPUT", "BUTTON PRESSED " + w.toString());
            }
        };

        panel.setLocalBounds(0,0,1920,180);
       // panel.setColor(1,0,0,0.7f);
        panel.setClickListener(listener);
        widget.addChild(panel);

        Widget button = new Button("EXIT BUTTON");
        button.setLocalBounds(1680,40,200,100);
        button.setColor(Color.GREEN);
        button.setClickListener(listener);
        panel.addChild(button);

        Widget button2 = new Button(new Sprite(getResources(), R.drawable.material));
        button2.setLocalBounds(50,40,200,100);
        button2.setColor(Color.YELLOW);
        button2.setClickListener(listener);
        panel.addChild(button2);

        long startTime = System.currentTimeMillis();
        background = new Sprite(getResources(), R.drawable.bitmap);
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
        GraphicsRender.clear();
        background.draw(camera, 0,0,1920,1080);
    }

    @Override
    public void postRender(Camera camera) {
        // В текстуре все кнопочки
        // A
        joystick.setActiveFrame(0);
        joystick.draw(camera, camera.SCREEN_WIDTH * 0.9f,
                camera.SCREEN_HEIGHT * 0.25f,2 + (Input.AButton ? 0.5f : 0));
        // B
        joystick.setActiveFrame(1);
        joystick.draw(camera, camera.SCREEN_WIDTH * 0.7f,
                camera.SCREEN_HEIGHT * 0.25f,2 + (Input.BButton ? 0.5f : 0));

        // Joystick border
        joystick.setActiveFrame(3);
        joystick.draw(camera, camera.SCREEN_WIDTH * 0.2f, camera.SCREEN_HEIGHT * 0.3f,4);

        // Joystick
        joystick.setActiveFrame(2);
        joystick.draw(camera,
                camera.SCREEN_WIDTH * 0.2f + Input.xAxis * (joystick.getWidth()),
                camera.SCREEN_HEIGHT * 0.3f + Input.yAxis * (joystick.getHeight()),
                1);

        GraphicsRender.drawText(
                ("FPS:" + GraphicsRender.getFPS() +
                        " CALLS=" + BatchRender.getDrawCallsCount() +
                        " QUADS=" + BatchRender.getEntriesCount() +
                        " MS:" + GraphicsRender.getRenderTime()).toCharArray(),
                camera.getMinX()+50, camera.getMinY()+1040, 2);
    }

    @Override
    public void onMotion(MotionEvent event, float worldX, float worldY) {

    }


    public void disposeScene() {

    }

}
