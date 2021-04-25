package com.axiom.operatio.scenes.production.view.adjustment;

import android.graphics.Color;
import android.view.MotionEvent;

import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Text;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Caption;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.operatio.model.common.FormatUtils;
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

    private Caption upperCaption, middleCaption, lowerCaption;
    private Button leftButton, centerButton, rightButton;
    private ItemWidget centerItemWidget;
    private Button changeoverButton;
    private Block chosenBlock = null;
    private StringBuffer timeBuffer = new StringBuffer(256);

    private OutputChooser outputChooser;

    private int chosenOperationID = 0;
    private int chosenMaterialID = 0;
    private int conveyorSpeed = 0;

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
        hideOutputChooser();
        scene.getSceneWidget().addChild(outputChooser);

        clickListener = new AdjustmentHandler(scene, this, outputChooser);
        buildButtons();
    }


    private void buildButtons() {
        // Название блока
        upperCaption = buildCaption(BLOCK_INFO, 40,600);

        // Кнопки управления блоком
        leftButton = buildActiveButton(LEFT, 40, 500, 75, 100);
        centerButton = buildActiveButton(CHOOSER, 140, 500, 100, 100);
        centerButton.setTextScale(1.5f);
        centerItemWidget = buildActiveItemWidget(140, 500, 100, 100);
        rightButton = buildActiveButton(RIGHT, 265, 500, 75, 100);

        // Список входных материалов
        middleCaption = buildCaption(INPUTS, 40,420);
        for (int i=0; i<4; i++) inpBtn[i] = buildItemWidget(40 + i*80, 350);

        // Список выходных материалов
        lowerCaption = buildCaption(OUTPUTS, 40,260);
        for (int i=0; i<4; i++) outBtn[i] = buildItemWidget(40 + i*80, 200);

        changeoverButton = buildActiveButton(CHANGEOVER, 40, 50, 300, 100);
        changeoverButton.setTextScale(1.5f);
    }


    private Caption buildCaption(String text, float x, float y) {
        Caption caption = new Caption(text);
        caption.setLocalBounds(x,y,300, 50);
        caption.setTextScale(1.3f);
        caption.setTextColor(WHITE);
        caption.setVerticalAlignment(Text.ALIGN_TOP);
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


    private ItemWidget buildActiveItemWidget(float x, float y, float width, float height) {
        ItemWidget iw = new ItemWidget("");
        iw.setTag(CHOOSER);
        iw.setLocalBounds(x, y, width, height);
        iw.setColor(Color.DKGRAY);
        iw.setTextColor(WHITE);
        iw.setTextScale(1.2f);
        iw.setClickListener(clickListener);
        addChild(iw);
        return iw;
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


    protected void selectConveyorSpeed(Conveyor conveyor, int speed) {
        int currentSpeed = conveyor.getSpeed();
        if (speed==1 || speed==2 || speed==3) {
            conveyorSpeed = speed;
        }
        if (currentSpeed == conveyorSpeed) setChangeoverState(false); else setChangeoverState(true);
    }


    protected void selectMachineOperation(Machine machine, int opID) {
        int operationsCount = machine.getType().getOperations().length;
        int currentOperation = machine.getOperationID();
        if (opID < 0) opID = 0;
        if (opID >= operationsCount) opID = operationsCount - 1;
        chosenOperationID = opID;
        if (chosenOperationID == currentOperation) setChangeoverState(false); else setChangeoverState(true);
    }



    protected void selectImporterMaterial(ImportBuffer importBuffer, int matID) {
        int materialsAmount = Material.getMaterialsAmount();
        int currentMaterial = importBuffer.getImportMaterial().getID();
        if (matID < 0) matID = 0;
        if (matID >= materialsAmount) matID = materialsAmount - 1;
        chosenMaterialID = matID;
        if (chosenMaterialID == currentMaterial) setChangeoverState(false); else setChangeoverState(true);
    }


    protected void selectInserterMaterial(Inserter inserter, int matID) {
        int materialsAmount = Material.getMaterialsAmount();
        Material material = inserter.getTargetMaterial();

        int currentMaterial = (material != null) ? material.getID() : -1;

        if (matID < -1) matID = -1;
        if (matID >= materialsAmount) matID = materialsAmount - 1;
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
            hideOutputChooser();
        }

        if (block instanceof Machine) {
            Machine machine = (Machine) block;
            if (blockChanged) chosenOperationID = machine.getOperationID();
            showMachineInfo(machine, chosenOperationID);
            if (outputChooser.isVisible()) outputChooser.showMachineOutputs(machine);
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
        if (block instanceof Conveyor) {
            Conveyor conveyor = (Conveyor) block;
            if (blockChanged) conveyorSpeed = conveyor.getSpeed();
            showConveyorInfo(conveyor, conveyorSpeed);
        }
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
        upperCaption.setText("Block information");
        hideButtons();
        hideInputsOutputs();
        hideOutputChooser();
        visible = false;
    }


    private void hideButtons() {
        centerButton.setVisible(false);
        centerItemWidget.setVisible(false);
        leftButton.setVisible(false);
        rightButton.setVisible(false);
        middleCaption.setVisible(false);
        lowerCaption.setVisible(false);
        changeoverButton.setVisible(false);
    }


    /**
     * Отображает информацию о машине
     * @param machine машина
     */
    protected void showMachineInfo(Machine machine, int opID) {

        hideButtons();

        MachineType machineType = machine.getType();
        Operation currentOperation = machineType.getOperation(opID);
        Material[] inputMaterials = currentOperation.getInputs();
        int[] inputAmount = currentOperation.getInputAmount();
        Material[] outputMaterials = currentOperation.getOutputs();
        int[] outputAmount = currentOperation.getOutputAmount();

        upperCaption.setText(machineType.getName());
        centerItemWidget.setVisible(true);
        centerItemWidget.setLocalBounds( 140, 500, 100, 100);
        centerItemWidget.setBackground(outputMaterials[0].getImage());
        centerItemWidget.setText(outputAmount[0] + "");
        leftButton.setVisible(true);
        rightButton.setVisible(true);

        middleCaption.setVisible(true);
        middleCaption.setText(outputMaterials[0].getName());
        for (int i=0; i<4; i++) {
            if (i < inputMaterials.length) {
                inpBtn[i].setBackground(inputMaterials[i].getImage());
                inpBtn[i].setText(inputAmount[i] + "");
            } else {
                inpBtn[i].setBackground(null);
                inpBtn[i].setText("");
            }
            inpBtn[i].setVisible(true);
        }


        lowerCaption.setVisible(true);
        float processingTime = ((machine.getOperation().getCycles() * production.getCycleMilliseconds()) / 1000.0f);
        FormatUtils.formatFloat(processingTime, timeBuffer);
        lowerCaption.setText("Processing time: " + timeBuffer + "s" + "\n\n" + machine.getStateDescription());

        GamePermissions permissions = production.getPermissions();
        changeoverButton.setVisible(permissions.isAvailable(currentOperation));
    }


    /**
     * Отображает информацию буфере
     * @param buffer буфер
     */
    private void showBufferInfo(Buffer buffer) {
        upperCaption.setText("Buffer contains");

        hideButtons();

        centerButton.setVisible(true);
        centerButton.setText("" + (buffer.getItemsAmount()) + "/" + (buffer.getCapacity()-1));
        centerButton.setBackground(null);
        centerButton.setLocation(40, 500);
        centerButton.setSize(300,100);

        middleCaption.setText("Stored materials");
        for (int i=0; i<4; i++) {
            Material material = buffer.getKeepingUnitMaterial(i);
            if (material!=null) {
                inpBtn[i].setBackground(material.getImage());
                inpBtn[i].setText("" + buffer.getKeepingUnitTotal(i));
            } else {
                inpBtn[i].setBackground(null);
                inpBtn[i].setText("");
            }
            inpBtn[i].setVisible(true);
            outBtn[i].setVisible(false);
        }

        changeoverButton.setVisible(true);
    }


    /**
     * Отображает информацию о конвейере
     * @param conveyor конвейер
     */
    protected void showConveyorInfo(Conveyor conveyor, int speed) {
        hideButtons();
        upperCaption.setText("Conveyor");
        centerButton.setVisible(true);
        centerButton.setText(speed + "x");
        centerButton.setBackground(null);
        centerButton.setLocalBounds( 140, 500, 100, 100);
        leftButton.setVisible(true);
        rightButton.setVisible(true);
        changeoverButton.setVisible(true);
        hideInputsOutputs();
        middleCaption.setVisible(true);
        float processingTime = (conveyor.getDeliveryCycles() * production.getCycleMilliseconds()) / 1000.0f;
        FormatUtils.formatFloat(processingTime, timeBuffer);
        middleCaption.setText("Delivery time: " + timeBuffer + "s\n\n" + conveyor.getStateDescription());
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

        upperCaption.setText("Importer");
        centerButton.setVisible(true);
        centerButton.setText("");
        centerButton.setBackground(material != null ? material.getImage() : null);
        centerButton.setLocalBounds( 140, 500, 100, 100);
        leftButton.setVisible(true);
        rightButton.setVisible(true);
        changeoverButton.setVisible(true);

        long balance = production.getInventory().getBalance(material);
        String name = material != null ? material.getName() : "";
        String balanceStr = name + "\n\n" + "Balance: " + balance + " items";
        middleCaption.setText(balanceStr);
        middleCaption.setVisible(true);
    }


    /**
     * Отображает информаци об экспортере на склад
     * @param exportBuffer
     */
    private void showExporterInfo(ExportBuffer exportBuffer) {
        upperCaption.setText("Exporter");
        hideButtons();
        hideInputsOutputs();

        middleCaption.setVisible(true);
        middleCaption.setText("Exported materials");
        for (int i=0; i<4; i++) {
            Material material = exportBuffer.getKeepingUnitMaterial(i);
            if (material!=null) {
                inpBtn[i].setBackground(material.getImage());
                inpBtn[i].setText("" + exportBuffer.getKeepingUnitTotal(i));
            } else {
                inpBtn[i].setBackground(null);
                inpBtn[i].setText("");
            }
            inpBtn[i].setVisible(true);
            outBtn[i].setVisible(false);
        }
    }


    protected void showInserterInfo(Inserter inserter, int materialID) {
        Material material = Material.getMaterial(materialID);

        hideButtons();
        hideInputsOutputs();

        upperCaption.setText("Inserter");
        centerButton.setVisible(true);
        centerButton.setText("");
        centerButton.setLocalBounds( 140, 500, 100, 100);
        if (material==null) centerButton.setBackground(null);
        else centerButton.setBackground(material.getImage());

        leftButton.setVisible(true);
        rightButton.setVisible(true);
        changeoverButton.setVisible(true);

        middleCaption.setVisible(true);
        float throughputTime = (inserter.getDeliveryCycles() * production.getCycleMilliseconds()) / 1000.0f;
        FormatUtils.formatFloat(throughputTime, timeBuffer);
        String materialName = (materialID==-1) ? "Any material" : material.getName();
        middleCaption.setText(materialName + "\n\nDelivery time: " + timeBuffer + "s\n\n" + inserter.getStateDescription());
    }


    private void hideInputsOutputs() {
        for (int i=0; i<4; i++) {
            inpBtn[i].setVisible(false);
            outBtn[i].setVisible(false);
        }
    }


    public void hideOutputChooser() {
        outputChooser.setVisible(false);
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

    public int getConveyorSpeed() {
        return conveyorSpeed;
    }
}
