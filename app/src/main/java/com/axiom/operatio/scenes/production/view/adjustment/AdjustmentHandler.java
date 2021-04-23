package com.axiom.operatio.scenes.production.view.adjustment;

import com.axiom.atom.R;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.model.production.buffer.Buffer;
import com.axiom.operatio.model.production.buffer.ImportBuffer;
import com.axiom.operatio.model.production.conveyor.Conveyor;
import com.axiom.operatio.model.production.inserter.Inserter;
import com.axiom.operatio.model.production.machine.Machine;
import com.axiom.operatio.scenes.production.ProductionScene;

import static com.axiom.operatio.scenes.production.view.adjustment.AdjustmentPanel.CHANGEOVER;
import static com.axiom.operatio.scenes.production.view.adjustment.AdjustmentPanel.CHOOSER;
import static com.axiom.operatio.scenes.production.view.adjustment.AdjustmentPanel.LEFT;
import static com.axiom.operatio.scenes.production.view.adjustment.AdjustmentPanel.RIGHT;

public class AdjustmentHandler implements ClickListener {

    private ProductionScene productionScene;
    private AdjustmentPanel adjustmentPanel;
    private OutputChooser outputChooser;
    private int tickSound;

    public AdjustmentHandler(ProductionScene scene, AdjustmentPanel pnl, OutputChooser chooser) {
        super();
        productionScene = scene;
        adjustmentPanel = pnl;
        outputChooser = chooser;
        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);
    }

    @Override
    public void onClick(Widget w) {
        SoundRenderer.playSound(tickSound);
        Button button = (Button) w;
        Block chosenBlock = adjustmentPanel.getChosenBlock();

        productionScene.getModePanel().untoggleButtons();

        if (chosenBlock != null) {
            if (chosenBlock instanceof Machine) {
                machineAdjustmentClick(button, (Machine) chosenBlock);
            } else if (chosenBlock instanceof ImportBuffer) {
                importBufferAdjustmentClick(button, (ImportBuffer) chosenBlock);
            } else if (chosenBlock instanceof Inserter) {
                inserterAdjustmentClick(button, (Inserter) chosenBlock);
            } else if (chosenBlock instanceof Conveyor) {
                conveyorAdjustmentClick(button, (Conveyor) chosenBlock);
            } else if (chosenBlock instanceof Buffer) {
                bufferAdjustmentClick(button, (Buffer) chosenBlock);
            }
        }
    }


    private void machineAdjustmentClick(Button button, Machine machine) {
        int currentOperationID = adjustmentPanel.getChosenOperationID();
        String tag = button.getTag();
        switch (tag) {
            case CHOOSER:
                outputChooser.showMachineOutputs(machine);
                outputChooser.setVisible(!outputChooser.isVisible());
                break;
            case LEFT:
                adjustmentPanel.selectMachineOperation(machine, currentOperationID - 1);
                adjustmentPanel.showMachineInfo(machine, adjustmentPanel.getChosenOperationID());
                if (outputChooser.isVisible()) outputChooser.showMachineOutputs(machine);
                outputChooser.setVisible(false);
                break;
            case RIGHT:
                adjustmentPanel.selectMachineOperation(machine, currentOperationID + 1);
                adjustmentPanel.showMachineInfo(machine, adjustmentPanel.getChosenOperationID());
                if (outputChooser.isVisible()) outputChooser.showMachineOutputs(machine);
                outputChooser.setVisible(false);
                break;
            case CHANGEOVER:
                machine.setOperation(currentOperationID);
                adjustmentPanel.setChangeoverState(false);
                outputChooser.setVisible(false);
                break;
        }
    }



    private void importBufferAdjustmentClick(Button button, ImportBuffer importBuffer) {
        int currentMaterialID = adjustmentPanel.getChosenMaterialID();
        String tag = button.getTag();
        switch (tag) {
            case CHOOSER:
                outputChooser.showImporterMaterials(importBuffer);
                outputChooser.setVisible(!outputChooser.isVisible());
                break;
            case LEFT:
                adjustmentPanel.selectImporterMaterial(importBuffer, currentMaterialID - 1);
                adjustmentPanel.showImporterInfo(importBuffer, adjustmentPanel.getChosenMaterialID());
                outputChooser.setVisible(false);
                break;
            case RIGHT:
                adjustmentPanel.selectImporterMaterial(importBuffer, currentMaterialID + 1);
                adjustmentPanel.showImporterInfo(importBuffer, adjustmentPanel.getChosenMaterialID());
                outputChooser.setVisible(false);
                break;
            case CHANGEOVER:
                importBuffer.setImportMaterial(Material.getMaterial(currentMaterialID));
                adjustmentPanel.setChangeoverState(false);
                outputChooser.setVisible(false);
                break;
        }
    }




    private void inserterAdjustmentClick(Button button, Inserter inserter) {
        int currentMaterialID = adjustmentPanel.getChosenMaterialID();
        String tag = button.getTag();
        switch (tag) {
            case CHOOSER:
                outputChooser.showInserterMaterials(inserter);
                outputChooser.setVisible (!outputChooser.isVisible());
                break;
            case LEFT:
                adjustmentPanel.selectInserterMaterial(inserter, currentMaterialID - 1);
                adjustmentPanel.showInserterInfo(inserter, adjustmentPanel.getChosenMaterialID());
                outputChooser.setVisible(false);
                break;
            case RIGHT:
                adjustmentPanel.selectInserterMaterial(inserter, currentMaterialID + 1);
                adjustmentPanel.showInserterInfo(inserter, adjustmentPanel.getChosenMaterialID());
                outputChooser.setVisible(false);
                break;
            case CHANGEOVER:
                inserter.setTargetMaterial(Material.getMaterial(currentMaterialID));
                adjustmentPanel.setChangeoverState(false);
                outputChooser.setVisible(false);
                break;
        }
    }



    private void conveyorAdjustmentClick(Button button, Conveyor conveyor) {
        String tag = button.getTag();
        switch (tag) {
            case CHANGEOVER:
                conveyor.clear();
                adjustmentPanel.setChangeoverState(false);
                outputChooser.setVisible(false);
                break;
            default:

        }
    }


    private void bufferAdjustmentClick(Button button, Buffer buffer) {
        String tag = button.getTag();
        switch (tag) {
            case CHANGEOVER:
                buffer.clear();
                adjustmentPanel.setChangeoverState(false);
                outputChooser.setVisible(false);
                break;
            default:

        }
    }
}
