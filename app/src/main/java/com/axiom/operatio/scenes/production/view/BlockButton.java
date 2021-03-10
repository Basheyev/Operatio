package com.axiom.operatio.scenes.production.view;

import android.graphics.Color;
import android.view.MotionEvent;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.operatio.model.gameplay.Utils;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.model.production.buffer.Buffer;
import com.axiom.operatio.model.production.buffer.ExportBuffer;
import com.axiom.operatio.model.production.buffer.ImportBuffer;
import com.axiom.operatio.model.production.conveyor.Conveyor;
import com.axiom.operatio.model.production.machine.Machine;
import com.axiom.operatio.model.production.machine.MachineType;
import com.axiom.operatio.scenes.production.ProductionScene;
import com.axiom.operatio.scenes.production.controller.BlockAddMoveHandler;

public class BlockButton extends Button {

    private ProductionScene scene;
    private BlocksPanel panel;
    private int tickSound;
    private int denySound;


    public BlockButton(ProductionScene scene, BlocksPanel panel, int id) {
        super();

        this.scene = scene;
        setTextColor(Color.WHITE);
        setTextScale(1f);

        if (id==0) initializeAnimationButton(40, 47, 15, Conveyor.PRICE);    // Если это конвейер
        else if (id==1) initializeAnimationButton(72, 79, 8, Buffer.PRICE); // Если это буфер
        else if (id>=2 && id<7) initializeMachineButton(id); // Если это машины 0-4
        else if (id==7) initializeImageButton(64, 0);
        else if (id==8) initializeImageButton(65, ImportBuffer.PRICE);
        else if (id==9) initializeImageButton(66, ExportBuffer.PRICE);

        setColor(0.5f, 0.7f, 0.5f, 0.9f);
        setColor(Color.GRAY);
        this.panel = panel;
        this.tag = "" + id;
        panel.addChild(this);
        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);
        denySound = SoundRenderer.loadSound(R.raw.deny_snd);
    }


    private void initializeMachineButton(int id) {
        background = new Sprite(SceneManager.getResources(), R.drawable.blocks, 8, 11);
        int startFrame = (id - 2) * 8;
        int animation = background.addAnimation(startFrame, startFrame + 7, 8, true);
        background.setActiveAnimation(animation);
        setText(Utils.moneyAsString(Math.round(MachineType.getMachineType(id-2).getPrice())));
    }


    private void initializeImageButton(int activeFrame, int price) {
        background = new Sprite(SceneManager.getResources(), R.drawable.blocks, 8, 11);
        background.setActiveFrame(activeFrame);
        if (price > 0) setText("$" + price); else setText("");
    }


    private void initializeAnimationButton(int startFrame, int stopFrame, int fps, int price) {
        background = new Sprite(SceneManager.getResources(), R.drawable.blocks, 8, 11);
        int animation = background.addAnimation(startFrame, stopFrame, fps,true);
        background.setActiveAnimation(animation);
        setText("$" + price);
    }


    @Override
    public boolean onMotionEvent(MotionEvent event, float worldX, float worldY) {
        BlockAddMoveHandler moveHandler = scene.getInputHandler().getBlockAddMoveHandler();
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                actionDown(worldX,worldY,moveHandler);
                break;
            case MotionEvent.ACTION_UP:
                actionUp(moveHandler);
                break;
        }
        return true;
    }


    private void actionDown(float worldX, float worldY, BlockAddMoveHandler moveHandler) {
        scene.getInputHandler().invalidateAllActions();
        ProductionSceneUI.getModePanel().untoggleButtons();
        Block block = createBlock(scene.getProduction(), getTag());
        if (block!=null) {
            panel.setToggledButton(getTag());
            double cash = scene.getProduction().getCashBalance();
            if (cash - block.getPrice() >= 0) {
                moveHandler.startAction(block, worldX, worldY);
                SoundRenderer.playSound(tickSound);
            } else {
                SoundRenderer.playSound(denySound);
            }
        }
    }


    private void actionUp(BlockAddMoveHandler moveHandler) {
        panel.setToggledButton("");
        scene.getProduction().unselectBlock();
        moveHandler.invalidateAction();
    }


    protected Block createBlock(Production production, String toggled) {
        Block block = null;
        int choice = Integer.parseInt(toggled);
        switch (choice) {
            case 0: block = new Conveyor(production, Block.LEFT, Block.RIGHT); break;
            case 1: block = new Buffer(production, 100); break;
            case 2: block = createMachine(production, 0); break;
            case 3: block = createMachine(production, 1); break;
            case 4: block = createMachine(production, 2); break;
            case 5: block = createMachine(production, 3); break;
            case 6: block = createMachine(production, 4); break;
            case 7: break;
            case 8: block = new ImportBuffer(production, Material.getMaterial(0)); break;
            case 9: block = new ExportBuffer(production); break;
        }
        return block;
    }


    private Machine createMachine(Production production, int machineType) {
        MachineType mt = MachineType.getMachineType(machineType);
        return new Machine(production, mt, mt.getOperations()[0], Machine.LEFT, Machine.RIGHT);
    }


    @Override
    public void draw(Camera camera) {
        if (parent==null || !visible) return;
        AABB bounds = getWorldBounds();
        // AABB scissors = getScissors();
        AABB parentScissor = parent.getScissors();

        if (opaque) {
            GraphicsRender.setZOrder(zOrder);
            GraphicsRender.setColor(color[0], color[1], color[2], color[3]);
            GraphicsRender.drawRectangle(bounds, parentScissor);
        }

        if (background !=null) {
            background.zOrder = zOrder + 1;
            background.draw(camera, bounds, parentScissor);
        }

        if (text!=null) {
            GraphicsRender.setZOrder(zOrder + 2);
            float textWidth = GraphicsRender.getTextWidth(text, textScale);
            float textHeight = GraphicsRender.getTextHeight(text,textScale);
            GraphicsRender.setColor(0.0f,0.0f,0.0f, 0.7f);
            GraphicsRender.drawRectangle(bounds.min.x, bounds.min.y, bounds.width, 30);
            GraphicsRender.setZOrder(zOrder + 3);
            GraphicsRender.setColor(textColor[0], textColor[1], textColor[2], textColor[3]);
            GraphicsRender.drawText(text, bounds.max.x - textWidth - 5, bounds.min.y + 5, textScale, parentScissor);
            // Пока не обрезаем текст для повышения производительности
            // GraphicsRender.drawText(text, bounds.center.x - textWidth/2, bounds.center.y - (textHeight/2), textScale, scissors);
        }
        //super.draw(camera);
    }



}
