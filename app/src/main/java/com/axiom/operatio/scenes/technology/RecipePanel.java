package com.axiom.operatio.scenes.technology;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.graphics.renderers.Text;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Caption;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.common.FormatUtils;
import com.axiom.operatio.model.gameplay.GamePermissions;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.machine.MachineType;
import com.axiom.operatio.model.production.machine.Operation;
import com.axiom.operatio.scenes.common.ItemWidget;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;


public class RecipePanel extends Panel {

    public static final int RECIPE_PRICE = 500;

    public static final String INPUTS = "Inputs";
    public static final String OUTPUTS = "Outputs";
    public static final String RECIPE = "Recipe";

    private MaterialsTree materialsTree;
    private Production production;
    private Caption caption, inputsCaption, machineCaption, outputsCaption;
    private ItemWidget[] inpBtn, outBtn;
    private ItemWidget machineButton;
    private Caption[] inpCap, outCap;
    private Button researchButton;

    private Operation selectedOperation;

    public RecipePanel(MaterialsTree panel, Production production) {
        super();
        this.materialsTree = panel;
        this.production = production;
        this.selectedOperation = null;
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
            inputsCaption.setText(INPUTS);
            outputsCaption.setText(OUTPUTS);
        } else {
            clearFields();
        }
    }


    private void clearFields() {
        caption.setText(RECIPE);
        inputsCaption.setText("");
        outputsCaption.setText("");
        machineCaption.setText("No recipe");
        for (int i=0; i<4; i++) {
            inpBtn[i].setBackground(null);
            inpBtn[i].setText("");
            inpBtn[i].setTag(null);
            inpCap[i].setText("");
            inpCap[i].setTag(null);
            machineButton.setBackground(null);
            machineButton.setText("");
            outBtn[i].setBackground(null);
            outBtn[i].setText("");
            outCap[i].setText("");
        }
        selectedOperation = null;
    }


    private void buildUI() {
        Panel panel = this;
        panel.setLocalBounds(874,60, 1026, 880);
        panel.setColor(0xCC505050);

        inpBtn = new ItemWidget[4];
        outBtn = new ItemWidget[4];
        inpCap = new Caption[4];
        outCap = new Caption[4];

        caption = new Caption(RECIPE);
        caption.setTextScale(1.5f);
        caption.setTextColor(WHITE);
        caption.setLocalBounds(30, panel.getHeight() - 100, 500, 100);
        panel.addChild(caption);

        // Список входных материалов
        inputsCaption = new Caption("");
        inputsCaption.setLocalBounds(144,650,250, 100);
        inputsCaption.setTextScale(1.2f);
        inputsCaption.setTextColor(WHITE);
        inputsCaption.setHorizontalAlignment(Text.ALIGN_RIGHT);
        addChild(inputsCaption);

        for (int i=0; i<4; i++) {
            inpBtn[i] = new ItemWidget("");
            inpBtn[i].setLocalBounds(330 , 580 - i*80, 64, 64);
            inpBtn[i].setColor(BLACK);
            inpBtn[i].setTextColor(WHITE);
            inpBtn[i].setTextScale(1);
            inpBtn[i].setClickListener(clickListener);
            addChild(inpBtn[i]);
            inpCap[i] = new Caption("");
            inpCap[i].setLocalBounds(30 , 580 - i*80, 280, 64);
            inpCap[i].setTextColor(WHITE);
            inpCap[i].setTextScale(1f);
            inpCap[i].setHorizontalAlignment(Text.ALIGN_RIGHT);
            inpCap[i].setClickListener(clickListener);
            addChild(inpCap[i]);
        }

        // Кнопка отображающая машину
        machineCaption = new Caption("");
        machineCaption.setLocalBounds(450,600,128, 100);
        machineCaption.setTextScale(1.2f);
        machineCaption.setTextColor(WHITE);
        machineCaption.setHorizontalAlignment(Text.ALIGN_CENTER);
        machineCaption.setVerticalAlignment(Text.ALIGN_BOTTOM);

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
        outputsCaption = new Caption("");
        outputsCaption.setLocalBounds(630,650,300, 100);
        outputsCaption.setTextScale(1f);
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
            outCap[i].setTextScale(1f);
            outCap[i].setHorizontalAlignment(Text.ALIGN_LEFT);
            addChild(outCap[i]);
        }

        researchButton = new Button("Research");
        researchButton.setLocalBounds(panel.getWidth() - 325,25,300,100);
        researchButton.setTextScale(1.5f);
        researchButton.setTextColor(WHITE);
        researchButton.setClickListener(researchClickListener);
        researchButton.visible = false;
        addChild(researchButton);

        clearFields();

    }


    protected boolean findMachineAndOperations(Material selectedMaterial) {
        GamePermissions permissions = production.getPermissions();
        int machineTypesCount = MachineType.getMachineTypesCount();
        for (int i=0; i<machineTypesCount; i++) {
            MachineType machineType = MachineType.getMachineType(i);
            Operation[] operation = machineType.getOperations();
            for (int j=0; j<operation.length; j++) {
                Material[] outputs = operation[j].getOutputs();
                for (int k=0; k<outputs.length; k++) {
                    if (outputs[k].equals(selectedMaterial)) {
                        if (permissions.isAvailable(selectedMaterial)) {
                            researchButton.visible = false;
                        } else {
                            researchButton.visible = true;
                        }
                        selectedOperation = machineType.getOperation(j);
                        showMachineAndOperation(machineType, j);
                        return true;
                    }
                }
            }
        }
        clearFields();
        return false;
    }


    private StringBuffer machineDescription = new StringBuffer(256);

    protected void showMachineAndOperation(MachineType machineType, int operationID) {

        Operation operation = machineType.getOperation(operationID);
        Sprite machineImage = machineType.getImage();

        machineDescription.delete(0, machineDescription.length());
        machineDescription.append(machineType.getName());
        machineDescription.append("\noperation #");
        machineDescription.append(operationID + 1);
        machineDescription.append("\n\n");
        machineDescription.append(FormatUtils.formatMoney(operation.getCost()));
        machineCaption.setText(machineDescription);
        machineButton.setBackground(machineImage);

        Material[] inputs =  operation.getInputs();
        int[] inputAmount = operation.getInputAmount();
        Material[] outputs = operation.getOutputs();
        int[] outputAmount = operation.getOutputAmount();

        for (int i=0; i<4; i++) {
            if (i<inputs.length) {
                String materialTag = "" + inputs[i].getMaterialID();
                inpBtn[i].setBackground(inputs[i].getImage());
                inpBtn[i].setText("x" + inputAmount[i]);
                inpBtn[i].setTag(materialTag);
                inpCap[i].setText(inputs[i].getName() + " - $" + Math.round(inputs[i].getPrice()));
                inpCap[i].setTag(materialTag);
            } else {
                inpBtn[i].setBackground(null);
                inpBtn[i].setText("");
                inpBtn[i].setTag(null);
                inpCap[i].setText("");
                inpCap[i].setTag(null);
            }
        }


        for (int i=0; i<4; i++) {
            if (i<outputs.length) {
                outBtn[i].setBackground(outputs[i].getImage());
                outBtn[i].setText("x" + outputAmount[i]);
                outCap[i].setText(outputs[i].getName() + " - $" + Math.round(outputs[i].getPrice()));
            } else {
                outBtn[i].setBackground(null);
                outBtn[i].setText("");
                outCap[i].setText("");
            }
        }

    }

    @Override
    public void draw(Camera camera) {
        drawRecipeBackground();
        drawFlowLines();
        super.draw(camera);
    }


    private void drawRecipeBackground() {
        AABB bnds = getWorldBounds();
        GraphicsRender.setZOrder(zOrder + 1);
        GraphicsRender.setColor(0.08f,0,0.16f,0.8f);
        GraphicsRender.drawRectangle(bnds.min.x, bnds.min.y + 250, bnds.width, 530);
    }


    private void drawFlowLines() {

        AABB mch = machineButton.getWorldBounds();

        GraphicsRender.setZOrder(zOrder + 2);
        GraphicsRender.setLineThickness(6);
        GraphicsRender.setColor(0.8f,0.8f,0,0.6f);

        for (int i=0; i<4; i++) if (inpBtn[i].getBackground()!=null) {
            AABB mat = inpBtn[i].getWorldBounds();
            GraphicsRender.drawLine(mat.center.x, mat.center.y, mch.center.x, mch.center.y);
        }

        for (int i=0; i<4; i++) if (outBtn[i].getBackground()!=null) {
            AABB mat = outBtn[i].getWorldBounds();
            GraphicsRender.drawLine(mat.center.x, mat.center.y, mch.center.x, mch.center.y);
        }

    }


    private ClickListener clickListener = new ClickListener() {

        protected int tickSound =-1;

        @Override
        public void onClick(Widget w) {
            if (tickSound == -1) tickSound = SoundRenderer.loadSound(R.raw.tick_snd);
            String materialTag = w.getTag();
            if (materialTag != null) {
                try {
                    int materialID = Integer.parseInt(w.getTag());
                    materialsTree.setSelectedMaterial(Material.getMaterial(materialID));
                    SoundRenderer.playSound(tickSound);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    };



    private ClickListener researchClickListener = new ClickListener() {

        protected int buySound =-1;

        @Override
        public void onClick(Widget w) {
            if (buySound == -1) buySound = SoundRenderer.loadSound(R.raw.cash_snd);
            GamePermissions permissions = production.getPermissions();
            Material material = materialsTree.getSelectedMaterial();
            if (material!=null && selectedOperation!=null) {
                if (!permissions.isAvailable(material)) {
                    if (production.decreaseCashBalance(0, RECIPE_PRICE)) {
                        SoundRenderer.playSound(buySound);
                        permissions.addMaterialPermission(material);
                        permissions.addOperationPermission(selectedOperation);
                        permissions.addMachinePermission(selectedOperation.getMachineType());
                    }
                }
            }
        }

    };



}
