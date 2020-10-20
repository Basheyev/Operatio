package com.axiom.operatio.scenes.production.view;

import android.graphics.Color;
import android.view.MotionEvent;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.widgets.Button;
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

    protected ProductionScene scene;
    protected BlocksPanel panel;
    protected int tickSound;

    public BlockButton(ProductionScene scene, BlocksPanel panel, int id) {
        super();

        this.scene = scene;

        int animation;

        if (id==0) { // Если это конвейер
            background = new Sprite(SceneManager.getResources(), R.drawable.blocks, 8, 11);
            animation = background.addAnimation(40, 47, 15,true);
            background.setActiveAnimation(animation);
        } else if (id==1) { // Если это буфер
            background = new Sprite(SceneManager.getResources(), R.drawable.blocks, 8, 11);
            animation = background.addAnimation(72, 79, 8,true);
            background.setActiveAnimation(animation);
        } else if (id>=2 && id<7) { // Если это машины 0-4
            background = new Sprite(SceneManager.getResources(), R.drawable.blocks, 8, 11);
            int startFrame = (id - 2) * 8;
            animation = background.addAnimation(startFrame, startFrame + 7, 8, true);
            background.setActiveAnimation(animation);
        } else if (id==7) {
            background = new Sprite(SceneManager.getResources(), R.drawable.blocks, 8, 11);
            background.setActiveFrame(64);
        } else if (id==8) {
            background = new Sprite(SceneManager.getResources(), R.drawable.blocks, 8, 11);
            background.setActiveFrame(65);
        } else if (id==9) {
            background = new Sprite(SceneManager.getResources(), R.drawable.blocks, 8, 11);
            background.setActiveFrame(66);
        }

        setColor(0.5f, 0.7f, 0.5f, 0.9f);
        setColor(Color.GRAY);
        this.panel = panel;
        this.tag = "" + id;
        panel.addChild(this);
        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);
    }

    @Override
    public boolean onMotionEvent(MotionEvent event, float worldX, float worldY) {
        BlockAddMoveHandler moveHandler = scene.getInputHandler().getBlockAddMoveHandler();

        int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                SoundRenderer.playSound(tickSound);
                ProductionSceneUI.getModePanel().untoggleButtons();
                panel.toggledButton = getTag();
                Block block = createBlock(scene.getProduction(), getTag());
                if (block!=null) {
                    scene.getInputHandler().invalidateAllActions();
                    moveHandler.startAction(block, worldX, worldY);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                panel.toggledButton = "";
                scene.getProduction().unselectBlock();
                moveHandler.invalidateAction();
                break;
        }

        return true;

    }



    protected Block createBlock(Production production, String toggled) {
        Block block = null;
        MachineType mt;
        int choice = Integer.parseInt(toggled);
        switch (choice) {
            case 0: // КОн
                block = new Conveyor(production, Block.LEFT, Block.RIGHT, 5);
                break;
            case 1:
                block = new Buffer(production, 100);
                break;
            case 2:
                mt = MachineType.getMachineType(0);
                block = new Machine(production, mt, mt.getOperations()[0], Machine.LEFT, Machine.RIGHT);
                break;
            case 3:
                mt = MachineType.getMachineType(1);
                block = new Machine(production, mt, mt.getOperations()[0], Machine.LEFT, Machine.RIGHT);
                break;
            case 4:
                mt = MachineType.getMachineType(2);
                block = new Machine(production, mt, mt.getOperations()[0], Machine.LEFT, Machine.RIGHT);
                break;
            case 5:
                mt = MachineType.getMachineType(3);
                block = new Machine(production, mt, mt.getOperations()[0], Machine.LEFT, Machine.RIGHT);
                break;
            case 6:
                mt = MachineType.getMachineType(4);
                block = new Machine(production, mt, mt.getOperations()[0], Machine.LEFT, Machine.RIGHT);
                break;
            case 7:
                break;
            case 8:
                block = new ImportBuffer(production, Material.getMaterial(0));
                break;
            case 9:
                block = new ExportBuffer(production);
                break;
        }

        return block;

    }



}
