package com.axiom.operatio.scenes.production.view.adjustment;

import android.graphics.Color;
import android.view.MotionEvent;

import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Caption;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.operatio.model.gameplay.GamePermissions;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.model.production.buffer.Buffer;
import com.axiom.operatio.model.production.buffer.ImportBuffer;
import com.axiom.operatio.model.production.buffer.ExportBuffer;
import com.axiom.operatio.model.production.conveyor.Conveyor;
import com.axiom.operatio.model.production.inserter.Inserter;
import com.axiom.operatio.model.production.machine.Machine;
import com.axiom.operatio.model.production.machine.MachineType;
import com.axiom.operatio.model.production.machine.Operation;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.scenes.common.ItemWidget;
import com.axiom.operatio.scenes.production.ProductionScene;
import com.axiom.operatio.scenes.production.view.HelperPanel;

import static android.graphics.Color.DKGRAY;
import static android.graphics.Color.GRAY;
import static android.graphics.Color.WHITE;

/**
 * Панель настройки блока
 */
public class AdjustmentPanel extends Panel {

    private static final int panelColor = 0xCC505050;
    protected static final String BLOCK_INFO = "Block information";
    protected static final String CHOOSER = "Chooser";
    protected static final String LEFT = "<";
    protected static final String RIGHT = ">";
    protected static final String INPUTS = "Input materials:";
    protected static final String OUTPUTS = "Output materials:";
    protected static final String CHANGEOVER = "Changeover";

    private Production production;
    private ProductionScene productionScene;

    private Caption caption, inputsCaption, outputsCaption;
    private Button leftButton, centerButton, rightButton;
    private Button changeoverButton;
    private Block chosenBlock = null;

    private OutputChooser outputChooser;

    private int chosenOperationID = 0;
    private int chosenMaterialID = 0;

    private long lastProductionCycle;

    private ItemWidget[] inpBtn;
    private ItemWidget[] outBtn;



    public AdjustmentPanel(Production production, ProductionScene scene) {
        super();
        this.production = production;
        this.productionScene = scene;
        setLocalBounds(Camera.WIDTH - 375,160,375, 700);
        setColor(panelColor);
        inpBtn = new ItemWidget[4];
        outBtn = new ItemWidget[4];
        outputChooser = new OutputChooser(scene, this);
        outputChooser.visible = false;
        scene.getSceneWidget().addChild(outputChooser);

        clickListener = new AdjustmentHandler(scene, this, outputChooser);
        buildButtons();
    }


    private void buildButtons() {
        // Название блока
        caption = buildCaption(BLOCK_INFO, 40,599);

        // Кнопки управления блоком
        leftButton = buildActiveButton(LEFT, 40, 500, 75, 100);
        centerButton = buildActiveButton(CHOOSER, 140, 500, 100, 100);
        centerButton.setTextScale(1.5f);
        rightButton = buildActiveButton(RIGHT, 265, 500, 75, 100);

        // Список входных материалов
        inputsCaption = buildCaption(INPUTS, 40,400);
        for (int i=0; i<4; i++) inpBtn[i] = buildItemWidget(40 + i*80, 350);

        // Список выходных материалов
        outputsCaption = buildCaption(OUTPUTS, 40,250);
        for (int i=0; i<4; i++) outBtn[i] = buildItemWidget(40 + i*80, 200);

        changeoverButton = buildActiveButton(CHANGEOVER, 40, 50, 300, 100);
        changeoverButton.setTextScale(1.5f);
    }


    private Caption buildCaption(String text, float x, float y) {
        Caption caption = new Caption(text);
        caption.setLocalBounds(x,y,300, 100);
        caption.setTextScale(1.5f);
        caption.setTextColor(WHITE);
        addChild(caption);
        return caption;
    }


    private ItemWidget buildItemWidget(float x, float y) {
        ItemWidget iw = new ItemWidget("");
        iw.setLocalBounds(x, y, 64, 64);
        iw.setColor(DKGRAY);
        iw.setTextColor(WHITE);
        iw.setTextScale(1);
        addChild(iw);
        return iw;
    }


    private Button buildActiveButton(String caption, float x, float y, float width, float height) {
        Button button = new Button(caption);
        button.setTag(caption);
        button.setLocalBounds(x, y, width, height);
        button.setColor(Color.GRAY);
        button.setTextColor(WHITE);
        button.setClickListener(clickListener);
        addChild(button);
        return button;
    }


    @Override
    public void draw(Camera camera) {

        // Если прошел один цикл производства обновить информацию
        long currentCycle = production.getCurrentCycle();
        if (currentCycle > lastProductionCycle) {
            if (chosenBlock != null) showBlockInfo(chosenBlock);
            lastProductionCycle = currentCycle;
        }
        // Если выбран блок - отрисовать
        if (chosenBlock != null) super.draw(camera);
    }




    protected void selectMachineOperation(Machine machine, int opID) {
        int operationsCount = machine.getType().getOperations().length;
        int currentOperation = machine.getOperationID();
        if (opID < 0) opID = 0;
        if (opID > operationsCount) opID = operationsCount - 1;
        chosenOperationID = opID;
        if (chosenOperationID == currentOperation) setChangeoverState(false); else setChangeoverState(true);
    }



    protected void selectImporterMaterial(ImportBuffer importBuffer, int matID) {
        int materialsAmount = Material.getMaterialsAmount();
        int currentMaterial = importBuffer.getImportMaterial().getID();
        if (matID < 0) matID = 0;
        if (matID > materialsAmount) matID = materialsAmount - 1;
        chosenMaterialID = matID;
        if (chosenMaterialID == currentMaterial) setChangeoverState(false); else setChangeoverState(true);
    }


    protected void selectInserterMaterial(Inserter inserter, int matID) {
        int materialsAmount = Material.getMaterialsAmount();
        Material material = inserter.getTargetMaterial();

        int currentMaterial = (material != null) ? material.getID() : -1;

        if (matID < -1) matID = -1;
        if (matID > materialsAmount) matID = materialsAmount - 1;
        chosenMaterialID = matID;

        if (chosenMaterialID == currentMaterial) setChangeoverState(false); else setChangeoverState(true);
    }


    protected void setChangeoverState(boolean active) {
        if (active) {
            changeoverButton.setColor(0f, 0.6f, 0f, 1);
        } else {
            changeoverButton.setColor(GRAY);
        }
    }


    @Override
    public boolean onMotionEvent(MotionEvent event, float worldX, float worldY) {
        if (event.getActionMasked()==MotionEvent.ACTION_UP) {
            Block selectedBlock = production.getSelectedBlock();
            productionScene.getInputHandler().invalidateAllActions();
            if (selectedBlock!=null) production.selectBlock(selectedBlock.column, selectedBlock.row);
        }
        return super.onMotionEvent(event, worldX, worldY);
    }

    /**
     * Отображение информации о блоке на панели
     * @param block информацио о котором надо отобразить
     */
    public void showBlockInfo(Block block) {
        boolean blockChanged = false;

        if (block == null) { visible = false; return; } else visible = true;

        if (block != chosenBlock) {
            blockChanged = true;
            chosenBlock = block;
            changeoverButton.setColor(GRAY);
            outputChooser.visible = false;
        }

        if (block instanceof Machine) {
            Machine machine = (Machine) block;
            if (blockChanged) chosenOperationID = machine.getOperationID();
            showMachineInfo(machine, chosenOperationID);
            if (outputChooser.visible) outputChooser.showMachineOutputs(machine);
        }
        if (block instanceof ImportBuffer) {
            ImportBuffer importBuffer = (ImportBuffer) block;
            if (blockChanged) chosenMaterialID = importBuffer.getImportMaterial().getID();
            showImporterInfo(importBuffer, chosenMaterialID);
        }
        if (block instanceof Inserter) {
            Inserter inserter = (Inserter) block;
            Material mat = inserter.getTargetMaterial();
            if (blockChanged) chosenMaterialID = mat != null ? mat.getID() : -1;
            showInserterInfo(inserter, chosenMaterialID);
        }
        if (block instanceof Conveyor) showConveyorInfo((Conveyor) block);
        if (block instanceof Buffer) showBufferInfo((Buffer) block);
        if (block instanceof ExportBuffer) showExporterInfo((ExportBuffer) block);

        HelperPanel helperPanel = productionScene.getHelperPanel();
        helperPanel.setText(block.getDescription());
    }


    /**
     * Скрыть панель отображения информации о блоке
     */
    public void hideBlockInfo() {
        chosenBlock = null;
        caption.setText("Block information");
        hideButtons();
        hideInputsOutputs();
        outputChooser.visible = false;
        visible = false;
    }


    private void hideButtons() {
        centerButton.visible = false;
        leftButton.visible = false;
        rightButton.visible = false;
        inputsCaption.visible = false;
        outputsCaption.visible = false;
        changeoverButton.visible = false;
    }


    /**
     * Отображает информацию о машине
     * @param machine машина
     */
    protected void showMachineInfo(Machine machine, int opID) {
        MachineType machineType = machine.getType();
        Operation[] allOperations = machineType.getOperations();

        Operation currentOperation = machineType.getOperation(opID);
        Material[] inputMaterials = currentOperation.getInputs();
        int[] inputAmount = currentOperation.getInputAmount();
        Material[] outputMaterials = currentOperation.getOutputs();
        int[] outputAmount = currentOperation.getOutputAmount();

        caption.setText(machineType.getName() + " operation");
        centerButton.visible = true;
        centerButton.setText("" + opID + "/" + (allOperations.length-1));
        centerButton.setLocalBounds( 140, 500, 100, 100);
        centerButton.setBackground(null);
        leftButton.visible = true;
        rightButton.visible = true;

        inputsCaption.visible = true;
        inputsCaption.setText(INPUTS);
        for (int i=0; i<4; i++) {
            if (i < inputMaterials.length) {
                inpBtn[i].setBackground(inputMaterials[i].getImage());
                inpBtn[i].setText(inputAmount[i] + "");
            } else {
                inpBtn[i].setBackground(null);
                inpBtn[i].setText("");
            }
            inpBtn[i].visible = true;
        }

        outputsCaption.visible = true;
        outputsCaption.setText(OUTPUTS);
        for (int i=0; i<4; i++) {
            if (i < outputMaterials.length) {
                outBtn[i].setBackground(outputMaterials[i].getImage());
                outBtn[i].setText(outputAmount[i] + "");
            } else {
                outBtn[i].setBackground(null);
                outBtn[i].setText("");
            }
            outBtn[i].visible = true;
        }

        GamePermissions permissions = production.getPermissions();
        changeoverButton.visible = permissions.isAvailable(currentOperation);
    }


    /**
     * Отображает информацию буфере
     * @param buffer буфер
     */
    private void showBufferInfo(Buffer buffer) {
        caption.setText("Buffer contains");

        hideButtons();

        centerButton.visible = true;
        centerButton.setText("" + (buffer.getItemsAmount()) + "/" + (buffer.getCapacity()-1));
        centerButton.setBackground(null);
        centerButton.setLocation(40, 500);
        centerButton.setSize(300,100);

        inputsCaption.setText("Stored materials");
        for (int i=0; i<4; i++) {
            Material material = buffer.getKeepingUnitMaterial(i);
            if (material!=null) {
                inpBtn[i].setBackground(material.getImage());
                inpBtn[i].setText("" + buffer.getKeepingUnitTotal(i));
            } else {
                inpBtn[i].setBackground(null);
                inpBtn[i].setText("");
            }
            inpBtn[i].visible = true;
            outBtn[i].visible = false;
        }

    }


    /**
     * Отображает информацию о конвейере todo доработать информацию о конвейере
     * @param conveyor конвейер
     */
    private void showConveyorInfo(Conveyor conveyor) {
        hideButtons();
        caption.setText("Conveyor");
        centerButton.visible = true;
        centerButton.setText("" + (conveyor.getItemsAmount()));
        centerButton.setBackground(null);
        centerButton.setLocation(40, 500);
        centerButton.setSize(300,100);
        hideInputsOutputs();
    }


    /**
     * Отображает информацию об импортере со склада
     * @param importBuffer буфер импорта
     * @param materialID номер импортируемого материала
     */
    protected void showImporterInfo(ImportBuffer importBuffer, int materialID) {
        Material material = Material.getMaterial(materialID);

        hideButtons();
        hideInputsOutputs();

        caption.setText("Importer");
        centerButton.visible = true;
        centerButton.setText("");
        centerButton.setBackground(material != null ? material.getImage() : null);
        centerButton.setLocalBounds( 140, 500, 100, 100);
        leftButton.visible = true;
        rightButton.visible = true;
        changeoverButton.visible = true;

        long balance = production.getInventory().getBalance(material);
        String name = material != null ? material.getName() : "";
        String balanceStr = name + "\n" + "Balance: " + balance + " items";
        inputsCaption.setText(balanceStr);
        inputsCaption.visible = true;
    }


    /**
     * Отображает информаци об экспортере на склад
     * @param exportBuffer
     */
    private void showExporterInfo(ExportBuffer exportBuffer) {
        caption.setText("Exporter");
        hideButtons();
        hideInputsOutputs();
    }


    protected void showInserterInfo(Inserter inserter, int materialID) {
        Material material = Material.getMaterial(materialID);

        hideButtons();
        hideInputsOutputs();

        caption.setText("Inserter");
        centerButton.visible = true;
        centerButton.setText("");
        centerButton.setLocalBounds( 140, 500, 100, 100);
        if (material==null) centerButton.setBackground(null);
        else centerButton.setBackground(material.getImage());

        leftButton.visible = true;
        rightButton.visible = true;
        changeoverButton.visible = true;
    }


    private void hideInputsOutputs() {
        for (int i=0; i<4; i++) {
            inpBtn[i].visible = false;
            outBtn[i].visible = false;
        }
    }


    public Block getChosenBlock() {
        return chosenBlock;
    }

    public int getChosenOperationID() {
        return chosenOperationID;
    }

    public int getChosenMaterialID() {
        return chosenMaterialID;
    }
}
