package com.axiom.operatio.scenes;

import android.util.Log;
import android.view.MotionEvent;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.BatchRender;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.operatio.production.ProductionBuilder;
import com.axiom.operatio.production.ProductionRenderer;
import com.axiom.operatio.production.Production;
import com.axiom.operatio.production.block.Block;
import com.axiom.operatio.production.buffer.Buffer;
import com.axiom.operatio.production.machines.Machine;
import com.axiom.operatio.production.machines.MachineType;
import com.axiom.operatio.production.transport.Conveyor;
import com.axiom.operatio.scenes.ui.BlocksPanel;
import com.axiom.operatio.scenes.ui.UIBuilder;

// TODO Реализовать добавление машин на производство

public class ProductionScene extends GameScene {

    private Production production;
    private ProductionRenderer productionRenderer;

    private BlocksPanel panel;
    private float cellWidth = 128;                  // Ширина клетки
    private float cellHeight = 128;                 // Высота клетки
    private boolean dragging = false;
    private float cursorX, cursorY;
    private long lastCycleTime;                     // Время последнего цикла (миллисекунды)
    private static long cycleMilliseconds = 300;    // Длительносить цикла (миллисекунды)

    private int snd1, snd2, snd3;

    @Override
    public String getSceneName() {
        return "Production";
    }

    @Override
    public void startScene() {
        //Input.enabled = false;
        production = ProductionBuilder.createDemoProduction();
        productionRenderer = new ProductionRenderer(production, cellWidth, cellHeight);
        panel = (BlocksPanel) UIBuilder.buildUI(getResources(), getSceneWidget());
        snd1 = SoundRenderer.loadSound(R.raw.machine_snd);
        snd2 = SoundRenderer.loadSound(R.raw.conveyor_snd);
        snd3 = SoundRenderer.loadSound(R.raw.buffer_snd);
    }

    @Override
    public void disposeScene() {
        production.clearBlocks();
    }

    @Override
    public void updateScene(float deltaTimeNs) {
        long now = System.currentTimeMillis();
        if (now - lastCycleTime > cycleMilliseconds) {
            production.cycle();
            lastCycleTime = now;
        }
    }

    @Override
    public void preRender(Camera camera) {
        productionRenderer.draw(camera,0,0,1920,1080);
    }

    @Override
    public void postRender(Camera camera) {
        float x = camera.getX();
        float y = camera.getY();
        GraphicsRender.setZOrder(2000);
     //   GraphicsRender.setColor(0,0,0,1);
     //   GraphicsRender.drawRectangle(cursorX-10,cursorY-10, 20, 20,null);
        String fps = "FPS:" + GraphicsRender.getFPS() +
                " QUADS:" + BatchRender.getEntriesCount() +
                " CALLS:" + BatchRender.getDrawCallsCount() +
                " TIME:" + GraphicsRender.getRenderTime() + "ms";
        GraphicsRender.drawText(fps.toCharArray(), x - 700,y + 500, 2);
    }

    @Override
    public void onMotion(MotionEvent event, float worldX, float worldY) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                dragging = true;
                cursorX = worldX;
                cursorY = worldY;
                break;
            case MotionEvent.ACTION_MOVE:
                if (dragging) {
                    Camera camera = Camera.getInstance();
                    float x = camera.getX() + (cursorX - worldX);
                    float y = camera.getY() + (cursorY - worldY);
                    // Проверка границ карты
                    if (x - Camera.WIDTH / 2 < 0) x = Camera.WIDTH / 2;
                    if (y - Camera.HEIGHT / 2 < 0) y = Camera.HEIGHT / 2;
                    if (x + Camera.WIDTH / 2 > production.getColumns() * cellWidth)
                        x = production.getColumns() * cellWidth - Camera.WIDTH / 2;
                    if (y + Camera.HEIGHT / 2 > production.getRows() * cellHeight)
                        y = production.getRows() * cellHeight - Camera.HEIGHT / 2;
                    camera.lookAt(x, y);
                }
                break;
            case MotionEvent.ACTION_UP:
                int column = productionRenderer.getProductionColumn(worldX);
                int row = productionRenderer.getProductionRow(worldY);
                dragging = false;
                if (column >= 0 && row >= 0) {
                    Block block = production.getBlockAt(column, row);
                    if (block!=null) {
                        if (block instanceof Machine) SoundRenderer.playSound(snd1);
                        if (block instanceof Conveyor) SoundRenderer.playSound(snd2);
                        if (block instanceof Buffer) SoundRenderer.playSound(snd3);
                        Log.i("PROD COL=" + column + ", ROW=" + row, block.toString());
                    } else if (panel.getToggledButton()!=null) {
                        MachineType mt = null;
                        if (panel.getToggledButton().equals("0")) mt = MachineType.getMachineType(0);
                        if (panel.getToggledButton().equals("1")) mt = MachineType.getMachineType(1);
                        if (panel.getToggledButton().equals("2")) mt = MachineType.getMachineType(2);
                        if (panel.getToggledButton().equals("3")) mt = MachineType.getMachineType(3);
                        if (mt!=null) {
                            Machine machine = new Machine(production,
                                    mt, mt.getOperations()[0],
                                    Machine.LEFT, Machine.RIGHT);
                            production.setBlock(machine, column, row);
                        }
                    }
                }

        }


    }


    public static long getCycleTimeMs() {
        return cycleMilliseconds;
    }
}
