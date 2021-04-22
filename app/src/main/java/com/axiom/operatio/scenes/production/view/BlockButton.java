package com.axiom.operatio.scenes.production.view;

import android.content.res.Resources;
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
import com.axiom.operatio.model.common.FormatUtils;
import com.axiom.operatio.model.gameplay.GamePermissions;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.model.production.buffer.Buffer;
import com.axiom.operatio.model.production.buffer.ExportBuffer;
import com.axiom.operatio.model.production.buffer.ImportBuffer;
import com.axiom.operatio.model.production.conveyor.Conveyor;
import com.axiom.operatio.model.production.inserter.Inserter;
import com.axiom.operatio.model.production.machine.Machine;
import com.axiom.operatio.model.production.machine.MachineType;
import com.axiom.operatio.model.production.machine.Operation;
import com.axiom.operatio.scenes.production.ProductionScene;
import com.axiom.operatio.scenes.production.ProductionSceneUI;
import com.axiom.operatio.scenes.production.view.adjustment.AdjustmentPanel;
import com.axiom.operatio.scenes.production.controller.BlockAddMoveHandler;

/**
 * Кнопка выбора блока для добавления на производство
 */
public class BlockButton extends Button {

    public static final int MACHINES_INDEX_START = 5;

    private ProductionScene scene;
    private BlocksPanel panel;
    private int tickSound;
    private int denySound;
    private boolean active = true;

    private static Sprite allMachines = null;

    public BlockButton(ProductionScene scene, BlocksPanel panel, int id) {
        super();

        this.scene = scene;
        setTextColor(Color.WHITE);
        setTextScale(1f);

        if (allMachines==null) {
            Resources resources = SceneManager.getResources();
            allMachines = new Sprite(resources, R.drawable.blocks, 8, 16);
        }

        if (id==0) initializeImageButton(65, ImportBuffer.PRICE);
        else if (id==1) initializeImageButton(66, ExportBuffer.PRICE);
        else if (id==2) initializeAnimationButton(40, 47, 15, Conveyor.PRICE);  // Если это конвейер
        else if (id==3) initializeImageButton(98, Inserter.PRICE);                           // Если то манипулятор
        else if (id==4) initializeAnimationButton(72, 79, 8, Buffer.PRICE);     // Если это буфер
        else if (id>=MACHINES_INDEX_START) initializeMachineButton(id); // Если это машины 0-4



        setColor(Color.DKGRAY);
        this.panel = panel;
        this.tag = "" + id;
        panel.addChild(this);
        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);
        denySound = SoundRenderer.loadSound(R.raw.deny_snd);
    }


    private void initializeMachineButton(int id) {
        int startFrame = (id - MACHINES_INDEX_START) * 8;
        background = allMachines.getAsSprite(startFrame, startFrame + 7);
        int animation = background.addAnimation(0, 7, 8, true);
        background.setActiveAnimation(animation);
        double price = MachineType.getMachineType(id - MACHINES_INDEX_START).getPrice();
        StringBuffer priceText = new StringBuffer();
        setText(FormatUtils.formatMoneyAppend(price, priceText));
    }


    private void initializeImageButton(int activeFrame, int price) {
        background = allMachines.getAsSprite(activeFrame);
        background.setActiveFrame(activeFrame);
        if (price > 0) setText("$" + price); else setText("");
    }


    private void initializeAnimationButton(int startFrame, int stopFrame, int fps, int price) {
        background = allMachines.getAsSprite(startFrame, stopFrame);
        int animation = background.addAnimation(0, stopFrame - startFrame, fps,true);
        background.setActiveAnimation(animation);
        setText("$" + price);
    }


    @Override
    public boolean onMotionEvent(MotionEvent event, float worldX, float worldY) {
        if (!active) return true;
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
            double cash = scene.getProduction().getLedger().getCashBalance();
            if (cash - block.getPrice() >= 0) {
                moveHandler.startAction(block, worldX, worldY);
                SoundRenderer.playSound(tickSound);
            } else {
                SoundRenderer.playSound(denySound);
            }
        }
    }


    private void actionUp(BlockAddMoveHandler moveHandler) {
        panel.untoggleButtons();
        scene.getProduction().unselectBlock();
        AdjustmentPanel opsPanel = ProductionSceneUI.getAdjustmentPanel();
        opsPanel.hideBlockInfo();
        moveHandler.invalidateAction();
    }


    protected Block createBlock(Production production, String toggled) {
        Block block = null;
        int choice = Integer.parseInt(toggled);
        switch (choice) {
            case 0: block = new ImportBuffer(production, Material.getMaterial(0)); break;
            case 1: block = new ExportBuffer(production); break;
            case 2: block = new Conveyor(production, Block.LEFT, Block.RIGHT); break;
            case 3: block = new Inserter(production, Block.LEFT, Block.RIGHT); break;
            case 4: block = new Buffer(production, 100); break;
            case 5: block = createMachine(production, 0); break;
            case 6: block = createMachine(production, 1); break;
            case 7: block = createMachine(production, 2); break;
            case 8: block = createMachine(production, 3); break;
            case 9: block = createMachine(production, 4); break;


        }
        return block;
    }


    private Machine createMachine(Production production, int machineType) {
        GamePermissions permissions = production.getPermissions();
        MachineType mt = MachineType.getMachineType(machineType);
        Operation[] machineOps = mt.getOperations();

        // Ищем первую доступную операцию машины
        Operation defaultOperation = null;
        for (int i=0; i<machineOps.length; i++) {
            if (permissions.isAvailable(machineOps[i])) {
                defaultOperation = machineOps[i];
                break;
            }
        }

        // Если ни одна операция не доступна - машина недоступна
        if (defaultOperation==null) return null;

        //
        return new Machine(production, mt, defaultOperation, Machine.LEFT, Machine.RIGHT);
    }


    @Override
    public void draw(Camera camera) {
        if (parent==null || !visible) return;
        AABB bounds = getWorldBounds();
        AABB parentScissor = parent.getScissors();

        if (opaque) {
            GraphicsRender.setZOrder(zOrder);
            GraphicsRender.setColor(color[0], color[1], color[2], color[3]);
            GraphicsRender.drawRectangle(bounds, parentScissor);
        }

        if (!active) return;

        if (background !=null) {
            background.setZOrder(zOrder + 1);
            background.draw(camera, bounds, parentScissor);
        }

        if (text!=null) {
            GraphicsRender.setZOrder(zOrder + 2);
            float textWidth = GraphicsRender.getTextWidth(text, textScale);
            GraphicsRender.setColor(0.0f,0.0f,0.0f, 0.7f);
            GraphicsRender.drawRectangle(bounds.minX, bounds.minY, bounds.width, 30);
            GraphicsRender.setZOrder(zOrder + 3);
            GraphicsRender.setColor(textColor[0], textColor[1], textColor[2], textColor[3]);
            GraphicsRender.drawText(text, bounds.maxX - textWidth - 5, bounds.minY + 5, textScale, parentScissor);
        }

    }


    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
