package com.axiom.operatio.scenes.inventory;

import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.ui.widgets.Caption;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.machine.MachineType;
import com.axiom.operatio.model.production.machine.Operation;
import com.axiom.operatio.scenes.production.view.ItemWidget;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

public class TechnologyPanel extends Panel {

    protected MaterialsPanel materialsPanel;
    protected Caption caption, inputsCaption, machineCaption, outputsCaption;
    protected ItemWidget[] inpBtn, outBtn;
    protected ItemWidget machineButton;

    public TechnologyPanel(MaterialsPanel panel) {
        super();
        materialsPanel = panel;
        inpBtn = new ItemWidget[4];
        outBtn = new ItemWidget[4];
        buildUI();
    }

    public void updateData() {
        Material selectedMaterial = materialsPanel.getSelectedMaterial();

        // Если никакой материал не выбран
        if (selectedMaterial == null) {
            clearFields();
            return;
        }

        findMachineAndOperations(selectedMaterial);

        caption.setText(selectedMaterial.getName() + " technology");
    }


    private void clearFields() {
        caption.setText("Technology");
        machineCaption.setText("Machine");
        for (int i=0; i<4; i++) {
            inpBtn[i].setBackground(null);
            inpBtn[i].setText("");
            machineButton.setBackground(null);
            machineButton.setText("");
            outBtn[i].setBackground(null);
            outBtn[i].setText("");
        }
    }

    private void buildUI() {
        Panel panel = this;
        panel.setLocalBounds(874,60, 1026, 300);
        panel.setColor(0xCC505050);

        caption = new Caption("Technology");
        caption.setTextScale(1.5f);
        caption.setTextColor(WHITE);
        caption.setLocalBounds(30, 200, 300, 100);
        panel.addChild(caption);

        // Список входных материалов
        inputsCaption = new Caption("Input materials:");
        inputsCaption.setLocalBounds(40,130,250, 100);
        inputsCaption.setTextScale(1.2f);
        inputsCaption.setTextColor(WHITE);
        addChild(inputsCaption);

        for (int i=0; i<4; i++) {
            inpBtn[i] = new ItemWidget("");
            inpBtn[i].setLocalBounds(40 + i*80, 50, 64, 64);
            inpBtn[i].setColor(BLACK);
            inpBtn[i].setTextColor(WHITE);
            inpBtn[i].setTextScale(1);
            addChild(inpBtn[i]);
        }

        // Кнопка отображающая машину
        machineCaption = new Caption("Machine");
        machineCaption.setLocalBounds(400,130,250, 100);
        machineCaption.setTextScale(1.2f);
        machineCaption.setTextColor(WHITE);
        addChild(machineCaption);

        machineButton = new ItemWidget("");
        machineButton.setColor(BLACK);
        machineButton.opaque = false;
        machineButton.setTextColor(WHITE);
        machineButton.setTextScale(1);
        machineButton.setLocalBounds(400, 20, 128, 128);
        addChild(machineButton);

        // Список выходных материалов
        outputsCaption = new Caption("Output materials:");
        outputsCaption.setLocalBounds(600,130,300, 100);
        outputsCaption.setTextScale(1.2f);
        outputsCaption.setTextColor(WHITE);
        addChild(outputsCaption);


        for (int i=0; i<4; i++) {
            outBtn[i] = new ItemWidget("");
            outBtn[i].setLocalBounds(600 + i*80, 50, 64, 64);
            outBtn[i].setColor(BLACK);
            outBtn[i].setTextColor(WHITE);
            outBtn[i].setTextScale(1);
            addChild(outBtn[i]);
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
            } else {
                inpBtn[i].setBackground(null);
                inpBtn[i].setText("");
            }
        }


        for (int i=0; i<4; i++) {
            if (i<outputs.length) {
                outBtn[i].setBackground(outputs[i].getImage());
                outBtn[i].setText("" + outputAmount[i]);
            } else {
                outBtn[i].setBackground(null);
                outBtn[i].setText("");
            }
        }

    }


}
