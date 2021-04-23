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
import com.axiom.operatio.model.ledger.Ledger;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.machine.MachineType;
import com.axiom.operatio.model.production.machine.Operation;
import com.axiom.operatio.scenes.common.ItemWidget;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;


public class RecipePanel extends Panel {

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
    private StringBuffer machineDescription = new StringBuffer(256);


    public RecipePanel(MaterialsTree panel, Production production) {
        super();
        this.materialsTree = panel;
        this.production = production;
        this.selectedOperation = null;
        buildUI();
    }


    private void buildUI() {
        setLocalBounds(874,50, 1026, 880);
        setColor(0xCC505050);
        caption = buildCaption(RECIPE, 30, getHeight() - 100, 500, 100, 1.5f, 0, 0);

        // Формируем компоненты списка входных материалов
        inputsCaption = buildCaption("",144,650,250, 100, 1.2f, Text.ALIGN_RIGHT, 0);
        inpBtn = new ItemWidget[4];
        inpCap = new Caption[4];
        for (int i=0; i<4; i++) {
            inpBtn[i] = buildItemButton(330 , 580 - i*80, 64, 64);
            inpCap[i] = buildItemCaption(30 , 580 - i*80, 280, 64, Text.ALIGN_RIGHT);
            inpCap[i].setClickListener(clickListener);
        }

        // Формируем компоненты списка выходных материалов
        outputsCaption = buildCaption("",630,650,300, 100, 1.2f, 0, 0);
        outBtn = new ItemWidget[4];
        outCap = new Caption[4];
        for (int i=0; i<4; i++) {
            outBtn[i] = buildItemButton(630, 580 - i*80, 64, 64);
            outCap[i] = buildItemCaption(714 , 580 - i*80, 280, 64, Text.ALIGN_LEFT);
        }

        // Надпись рассказывающая о машине
        machineCaption = buildCaption("", 450,600,128, 100, 1.2f, Text.ALIGN_CENTER, Text.ALIGN_BOTTOM);
        machineButton = buildMachineButton(450, 450, 128, 128);

        // Кнопка исследования
        researchButton = buildResearchButton(getWidth() - 325,25,300,100);

        clearFields();
    }


    private Caption buildCaption(String txt, float x, float y, float w, float h, float scale, int HA, int VA) {
        Caption cap = new Caption(txt);
        cap.setLocalBounds(x,y,w,h);
        cap.setTextScale(scale);
        cap.setTextColor(WHITE);
        if (HA!=0) cap.setHorizontalAlignment(HA);
        if (VA!=0) cap.setVerticalAlignment(VA);
        addChild(cap);
        return cap;
    }

    private ItemWidget buildItemButton(float x, float y, float w, float h) {
        ItemWidget btn = new ItemWidget("");
        btn.setLocalBounds(x, y, w,h);
        btn.setColor(BLACK);
        btn.setTextColor(WHITE);
        btn.setTextScale(1);
        addChild(btn);
        return btn;
    }


    private Caption buildItemCaption(float x, float y, float w, float h, int HA) {
        Caption cap = new Caption("");
        cap.setLocalBounds(x , y, w, h);
        cap.setTextColor(WHITE);
        cap.setTextScale(1f);
        if (HA!=0) cap.setHorizontalAlignment(HA);
        addChild(cap);
        return cap;
    }


    private ItemWidget buildMachineButton( float x, float y, float w, float h) {
        ItemWidget mb = new ItemWidget("");
        mb.setColor(BLACK);
        mb.setOpaque(false);
        mb.setTextColor(WHITE);
        mb.setTextScale(1);
        mb.setLocalBounds(x, y, w, h);
        addChild(mb);
        return mb;
    }


    private Button buildResearchButton(float x, float y, float w, float h) {
        Button rb = new Button("Research");
        rb.setLocalBounds(x,y,w,h);
        rb.setTextScale(1.4f);
        rb.setTextColor(WHITE);
        rb.setClickListener(researchClickListener);
        rb.setVisible(false);
        addChild(rb);
        return rb;
    }



    protected boolean findMachineAndOperations(Material selectedMaterial) {
        GamePermissions permissions = production.getPermissions();
        selectedOperation = MachineType.findOperation(selectedMaterial);
        boolean rawMaterial = selectedMaterial.getID() < 8;
        if (selectedOperation != null && !rawMaterial) {
            String recipePrice = "Research $" + Math.round(selectedOperation.getRecipeCost());
            showMachineAndOperation(selectedOperation);
            boolean materialNotAvailable = !permissions.isAvailable(selectedMaterial);
            boolean operationNotAvailable = !permissions.isAvailable(selectedOperation);
            researchButton.setVisible(materialNotAvailable || operationNotAvailable);
            if (researchButton.isVisible()) researchButton.setText(recipePrice);
            return true;
        } else researchButton.setVisible(false);
        clearFields();
        return false;
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
        machineButton.setBackground(null);
        machineButton.setText("");
        for (int i=0; i<4; i++) {
            updateItemWidget(inpBtn[i], null, "", null);
            updateItemCaption(inpCap[i], "", null);
            updateItemWidget(outBtn[i], null, "", null);
            updateItemCaption(outCap[i], "", null);
        }
        selectedOperation = null;
    }


    private void updateItemWidget(ItemWidget btn, Sprite image, String txt, String tag) {
        btn.setBackground(image);
        btn.setText(txt);
        btn.setTag(tag);
    }

    private  void updateItemCaption(Caption caption, String txt, String tag) {
        caption.setText(txt);
        caption.setTag(tag);
    }



    protected void showMachineAndOperation(Operation operation) {

        int operationID = operation.getID();
        MachineType machineType = operation.getMachineType();
        Sprite machineImage = machineType.getImage();

        machineDescription.setLength(0);
        machineDescription.append(machineType.getName());
        machineDescription.append("\noperation #");
        machineDescription.append(operationID + 1);
        machineDescription.append("\n\n");
        FormatUtils.formatMoneyAppend(operation.getOperationCost(), machineDescription);

        machineCaption.setText(machineDescription);
        machineButton.setBackground(machineImage);

        Material[] inputs =  operation.getInputs();
        int[] inputAmount = operation.getInputAmount();
        Material[] outputs = operation.getOutputs();
        int[] outputAmount = operation.getOutputAmount();

        for (int i=0; i<4; i++) {
            if (i<inputs.length) {
                String materialTag = "" + inputs[i].getID();
                updateItemWidget(inpBtn[i], inputs[i].getImage(), "x" + inputAmount[i], materialTag);
                updateItemCaption(inpCap[i], inputs[i].getName() + " - $" + Math.round(inputs[i].getPrice()), materialTag);
            } else {
                updateItemWidget(inpBtn[i], null, "", null);
                updateItemCaption(inpCap[i], "", null);
            }
        }


        for (int i=0; i<4; i++) {
            if (i<outputs.length) {
                updateItemWidget(outBtn[i], outputs[i].getImage(), "x" + outputAmount[i], null);
                updateItemCaption(outCap[i], outputs[i].getName() + " - $" + Math.round(outputs[i].getPrice()), null);
            } else {
                updateItemWidget(outBtn[i], null, "", null);
                updateItemCaption(outCap[i], "", null);
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
        GraphicsRender.setColor(0.08f,0,0.16f,0.5f);
        GraphicsRender.drawRectangle(bnds.minX, bnds.minY + 250, bnds.width, 530);
    }


    private void drawFlowLines() {

        AABB mch = machineButton.getWorldBounds();

        GraphicsRender.setZOrder(zOrder + 2);
        GraphicsRender.setLineThickness(6);
        GraphicsRender.setColor(0.8f,0.8f,0,0.6f);

        for (int i=0; i<4; i++) if (inpBtn[i].getBackground()!=null) {
            AABB mat = inpBtn[i].getWorldBounds();
            GraphicsRender.drawLine(mat.centerX, mat.centerY, mch.centerX, mch.centerY);
        }

        for (int i=0; i<4; i++) if (outBtn[i].getBackground()!=null) {
            AABB mat = outBtn[i].getWorldBounds();
            GraphicsRender.drawLine(mat.centerX, mat.centerY, mch.centerX, mch.centerY);
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
                if (!permissions.isAvailable(material) || !permissions.isAvailable(selectedOperation)) {
                    double recipePrice = selectedOperation.getRecipeCost();
                    if (production.getLedger().creditCashBalance(Ledger.EXPENSE_RECIPE_BOUGHT, recipePrice)) {
                        SoundRenderer.playSound(buySound);
                        permissions.addMaterialPermission(material);
                        permissions.addOperationPermission(selectedOperation);
                        permissions.addMachinePermission(selectedOperation.getMachineType());
                        w.setVisible(false);
                    }
                }
            }
        }

    };



}
