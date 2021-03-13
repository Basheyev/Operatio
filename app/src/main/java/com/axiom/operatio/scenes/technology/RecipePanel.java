package com.axiom.operatio.scenes.technology;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.ui.widgets.Caption;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.machine.MachineType;
import com.axiom.operatio.model.production.machine.Operation;
import com.axiom.operatio.scenes.production.view.ItemWidget;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;
import static android.graphics.Color.YELLOW;

public class RecipePanel extends Panel {

    private MaterialsTree materialsTree;
    private Caption caption, inputsCaption, machineCaption, outputsCaption;
    private ItemWidget[] inpBtn, outBtn;
    private ItemWidget machineButton;
    private Caption[] inpCap, outCap;

    public RecipePanel(MaterialsTree panel) {
        super();
        materialsTree = panel;
        inpBtn = new ItemWidget[4];
        outBtn = new ItemWidget[4];
        inpCap = new Caption[4];
        outCap = new Caption[4];
        buildUI();
    }

    public void updateData() {
        Material selectedMaterial = materialsTree.getSelectedMaterial();

        // Если никакой материал не выбран
        if (selectedMaterial == null) {
            clearFields();
            return;
        }

        if (findMachineAndOperations(selectedMaterial)) {
            caption.setText(selectedMaterial.getName() + " recipe");
        } else {
            caption.setText("Recipe");
        }
    }


    private void clearFields() {
        caption.setText("Recipe");
        machineCaption.setText("Machine");
        for (int i=0; i<4; i++) {
            inpBtn[i].setBackground(null);
            inpBtn[i].setText("");
            inpCap[i].setText("");
            machineButton.setBackground(null);
            machineButton.setText("");
            outBtn[i].setBackground(null);
            outBtn[i].setText("");
            outCap[i].setText("");
        }
    }

    private void buildUI() {
        Panel panel = this;
        panel.setLocalBounds(874,60, 1026, 880);
        panel.setColor(0xCC505050);

        caption = new Caption("Recipe");
        caption.setTextScale(1.5f);
        caption.setTextColor(WHITE);
        caption.setLocalBounds(30, panel.getHeight() - 100, 300, 100);
        panel.addChild(caption);

        // Список входных материалов
        inputsCaption = new Caption("Input materials:");
        inputsCaption.setLocalBounds(144,650,250, 100);
        inputsCaption.setTextScale(1.2f);
        inputsCaption.setTextColor(WHITE);
        inputsCaption.setAlignment(Caption.ALIGN_RIGHT);
        addChild(inputsCaption);

        for (int i=0; i<4; i++) {
            inpBtn[i] = new ItemWidget("");
            inpBtn[i].setLocalBounds(330 , 580 - i*80, 64, 64);
            inpBtn[i].setColor(BLACK);
            inpBtn[i].setTextColor(WHITE);
            inpBtn[i].setTextScale(1);
            addChild(inpBtn[i]);
            inpCap[i] = new Caption("");
            inpCap[i].setLocalBounds(30 , 580 - i*80, 280, 64);
            inpCap[i].setTextColor(WHITE);
            inpCap[i].setTextScale(1.2f);
            inpCap[i].setAlignment(Caption.ALIGN_RIGHT);
            addChild(inpCap[i]);
        }

        // Кнопка отображающая машину
        machineCaption = new Caption("Machine");
        machineCaption.setLocalBounds(450,550,128, 100);
        machineCaption.setTextScale(1.2f);
        machineCaption.setTextColor(WHITE);
        machineCaption.setAlignment(Caption.ALIGN_CENTER);
        addChild(machineCaption);

        // Кнопка отображающая машину
        machineButton = new ItemWidget("");
        machineButton.setColor(BLACK);
        machineButton.opaque = false;
        machineButton.setTextColor(WHITE);
        machineButton.setTextScale(1);
        machineButton.setLocalBounds(450, 450, 128, 128);
        addChild(machineButton);

        // Список выходных материалов
        outputsCaption = new Caption("Output materials:");
        outputsCaption.setLocalBounds(630,650,300, 100);
        outputsCaption.setTextScale(1.2f);
        outputsCaption.setTextColor(WHITE);
        addChild(outputsCaption);


        for (int i=0; i<4; i++) {
            outBtn[i] = new ItemWidget("");
            outBtn[i].setLocalBounds(630, 580 - i*80, 64, 64);
            outBtn[i].setColor(BLACK);
            outBtn[i].setTextColor(WHITE);
            outBtn[i].setTextScale(1);
            addChild(outBtn[i]);
            outCap[i] = new Caption("");
            outCap[i].setLocalBounds(714 , 580 - i*80, 280, 64);
            outCap[i].setTextColor(WHITE);
            outCap[i].setTextScale(1.2f);
            outCap[i].setAlignment(Caption.ALIGN_LEFT);
            addChild(outCap[i]);
        }

    }


    protected boolean findMachineAndOperations(Material selectedMaterial) {
        int machineTypesCount = MachineType.getMachineTypesCount();
        for (int i=0; i<machineTypesCount; i++) {
            MachineType mt = MachineType.getMachineType(i);
            Operation[] op = mt.getOperations();
            for (int j=0; j<op.length; j++) {
                Material[] outputs = op[j].getOutputMaterials();
                for (int k=0; k<outputs.length; k++) {
                    if (outputs[k].equals(selectedMaterial)) {
                        showMachineAndOperation(mt, j);
                        return true;
                    }
                }
            }
        }
        clearFields();
        return false;
    }


    protected void showMachineAndOperation(MachineType machineType, int operationID) {

        Sprite machineImage = machineType.getImage();
        machineCaption.setText(machineType.getName());
        machineButton.setBackground(machineImage);
        machineButton.setText("Operation #" + operationID);

        Operation operation = machineType.getOperation(operationID);

        Material[] inputs =  operation.getInputMaterials();
        int[] inputAmount = operation.getInputAmount();
        Material[] outputs = operation.getOutputMaterials();
        int[] outputAmount = operation.getOutputAmount();

        for (int i=0; i<4; i++) {
            if (i<inputs.length) {
                inpBtn[i].setBackground(inputs[i].getImage());
                inpBtn[i].setText("" + inputAmount[i]);
                inpCap[i].setText(inputs[i].getName());
            } else {
                inpBtn[i].setBackground(null);
                inpBtn[i].setText("");
                inpCap[i].setText("");
            }
        }


        for (int i=0; i<4; i++) {
            if (i<outputs.length) {
                outBtn[i].setBackground(outputs[i].getImage());
                outBtn[i].setText("" + outputAmount[i]);
                outCap[i].setText(outputs[i].getName());
            } else {
                outBtn[i].setBackground(null);
                outBtn[i].setText("");
                outCap[i].setText("");
            }
        }

    }

    @Override
    public void draw(Camera camera) {
        drawLines();
        super.draw(camera);
    }


    private void drawLines() {

        AABB mch = machineButton.getWorldBounds();

        GraphicsRender.setZOrder(zOrder + 1);
        GraphicsRender.setLineThickness(6);
        GraphicsRender.setColor(YELLOW);

        for (int i=0; i<4; i++) if (inpBtn[i].getBackground()!=null) {
            AABB mat = inpBtn[i].getWorldBounds();
            GraphicsRender.drawLine(mat.center.x, mat.center.y, mch.center.x, mch.center.y);
        }

        for (int i=0; i<4; i++) if (outBtn[i].getBackground()!=null) {
            AABB mat = outBtn[i].getWorldBounds();
            GraphicsRender.drawLine(mat.center.x, mat.center.y, mch.center.x, mch.center.y);
        }

    }
}
