package com.axiom.operatio.scenes;

import android.util.Log;
import android.view.MotionEvent;

import com.axiom.atom.engine.core.GameObject;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Batcher;
import com.axiom.atom.engine.input.Input;
import com.axiom.atom.engine.physics.geometry.AABB;
import com.axiom.operatio.model.matflow.blocks.Block;
import com.axiom.operatio.model.matflow.blocks.Production;

public class ProductionScene extends GameScene {

    private AABB exitBtn;

    protected Production production;

    protected float cursorX, cursorY;
    protected int selectedColumn=0, selectedRow=0;
    protected float gridSize = 128;

    //--------------------------------------------------------------------------------------
    // Модель производства
    //--------------------------------------------------------------------------------------

    /**
     * Производственный цикл (такт)
     */
    public void cycle() {
        production.productionCycle();
    }



    //--------------------------------------------------------------------------------------
    // Отображение и управление
    //--------------------------------------------------------------------------------------

    @Override
    public String getSceneName() {
        return "Simulation";
    }

    @Override
    public void startScene() {
        if (production==null) production = new Production(this,100,100,gridSize);
        exitBtn = new AABB(1720,1004,1919,1079);
    }

    @Override
    public void updateScene(float deltaTime) {
        production.productionCycle();
        Camera camera = GraphicsRender.getCamera();
        cursorX = camera.x + Input.xAxis * 5;
        cursorY = camera.y + Input.yAxis * 5;
        camera.lookAt(cursorX, cursorY);
    }

    @Override
    public void preRender(Camera camera) {
        GraphicsRender.clear();
    }

    @Override
    public void postRender(Camera camera) {

        GraphicsRender.setZOrder(100);
        GraphicsRender.drawText(("QUADS:"+ Batcher.getEntriesCount() +
                " ITEMS:" + production.getTotalItems() +
                "\n" + production.getBlockAt(selectedColumn,selectedRow)).toCharArray(),
                camera.x1 + 50,
                camera.y1 + 100,2);
        GraphicsRender.setColor(1,0,0,1);


        GraphicsRender.drawRectangle(
                camera.x1 + exitBtn.min.x,
                camera.y1 + exitBtn.min.y,
                exitBtn.width, exitBtn.height);

        GraphicsRender.setZOrder(101);
        GraphicsRender.drawText("EXIT".toCharArray(),
                camera.x1 + exitBtn.min.x+30,
                camera.y1 + exitBtn.min.y + 30,2);

        GraphicsRender.setColor(1,1,0,0.3f);
        GraphicsRender.drawRectangle(selectedColumn*gridSize, selectedRow*gridSize, gridSize,gridSize);


        GraphicsRender.drawText(
                ("FPS:" + GraphicsRender.getFPS() +
                 " CALLS=" + Batcher.getDrawCallsCount() +
                        " MS:" + GraphicsRender.getRenderTime()).toCharArray(),
                camera.x1+50, camera.y1+1040, 2);

        GraphicsRender.setZOrder(0);


    }


    @Override
    public void onMotion(MotionEvent event, float worldX, float worldY) {
        if (event.getActionMasked()==MotionEvent.ACTION_UP) {
            GameObject obj = getSceneObjectAt(worldX, worldY);
            if (obj!=null) {
                if (obj instanceof Block) {
                    Block block = (Block) obj;
                    selectedColumn = block.column;
                    selectedRow = block.row;
                }
            }

            Camera camera = GraphicsRender.getCamera();
            if (exitBtn.collides(worldX - camera.x1, worldY - camera.y1)) {
                sceneManager.exitGame();
            }
        }

    }

    @Override
    public void dispose() {

    }
}
