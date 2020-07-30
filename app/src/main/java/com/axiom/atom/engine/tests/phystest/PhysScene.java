package com.axiom.atom.engine.tests.phystest;

import android.opengl.GLES20;
import android.view.MotionEvent;

import com.axiom.atom.engine.core.GameObject;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Batcher;
import com.axiom.atom.engine.physics.PhysicsRender;

import javax.microedition.khronos.opengles.GL10;


public class PhysScene extends GameScene {

    GameObject player;
    BoxObject selectedObject;
    float selectedX, selectedY;

    @Override
    public String getSceneName() {
        return "DEBUG";
    }

    @Override
    public void startScene() {

        player =  new BoxObject(this);

        // Добавляем платформу
        BoxObject block;
        float bw = 1000;
        float bh = 50;
        block = new BoxObject(this);
        block.x = 960;
        block.y = 100;
        block.bodyType = PhysicsRender.BODY_STATIC;
        block.setLocalBounds(-bw/2, -bh/2, bw/2, bh/2);
        addObject(block);


        for (int i=0; i<3; i++) {
            //for (int j = 0; j < 2; j++) {
                block = new BoxObject(this);
                block.x = 660 + i * 130;
                block.y = 400 + i * 150;
                block.bodyType = PhysicsRender.BODY_DYNAMIC;
                bw = (i % 2 == 1) ? 200 : 75; // (float) (50 + Math.random() * 50);
                bh = (i % 2 == 1) ? 75 : 200; // (float) (30 + Math.random() * 50);
                block.setLocalBounds(-bw/2, -bh/2, bw/2, bh/2);
                addObject(block);
           // }
        }

     /*   player.mass = 50;
        player.bodyType = WorldPhysics.BODY_DYNAMIC;
        player.setLocalBounds(-80, -50, +80, 50);
        addObject(player);*/

      }

    @Override
    public void updateScene(float deltaTime) {

        //camera.x = (int) player.x;
        //camera.y = (int) player.y;

        PhysicsRender.doPhysics(this, deltaTime);

    }


    @Override
    public void preRender(Camera camera) {
        GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public void postRender(Camera camera) {
        GraphicsRender.drawText(
                ("FPS:" + GraphicsRender.getFPS() +
                        " CALLS=" + Batcher.getDrawCallsCount() +
                        " QUADS=" + Batcher.getEntriesCount() +
                        " MS:" + GraphicsRender.getRenderTime()).toCharArray(),
                camera.getMinX()+50, camera.getMinY()+1040, 2);
    }

    @Override
    public void onMotion(MotionEvent event, float worldX, float worldY) {
        BoxObject block;
        float bw;
        float bh;
        int action = event.getActionMasked();
        if (action==MotionEvent.ACTION_DOWN) {
            block = (BoxObject) this.getSceneObjectAt(worldX, worldY);
            if (block!=null) {
                //removeObject(block);
                selectedObject = block;
                selectedX = worldX;
                selectedY = worldY;
            } else {
                block = new BoxObject(this);
                block.x = worldX;
                block.y = worldY;
                block.bodyType = PhysicsRender.BODY_DYNAMIC;
                bw = (float) (100 + Math.random() * 150);
                bh = (float) (70 + Math.random() * 150);
                block.setLocalBounds(-bw / 2, -bh / 2, bw / 2, bh / 2);
                addObject(block);
            }
        } else if (action==MotionEvent.ACTION_MOVE) {

        } else if (action==MotionEvent.ACTION_UP) {
            if (selectedObject!=null) {
                selectedObject.velocity.x += (worldX - selectedX) * 0.1f;
                selectedObject.velocity.y += (worldY - selectedY) * 0.1f;
                selectedObject = null;
            }
        }
    }

    @Override
    public void disposeScene() {

    }
}
