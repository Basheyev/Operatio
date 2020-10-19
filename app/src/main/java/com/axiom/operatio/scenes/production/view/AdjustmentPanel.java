package com.axiom.operatio.scenes.production.view;

import android.graphics.Color;

import com.axiom.atom.R;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Caption;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.model.production.buffer.Buffer;
import com.axiom.operatio.model.production.buffer.ImportBuffer;
import com.axiom.operatio.model.production.conveyor.Conveyor;
import com.axiom.operatio.model.production.machine.Machine;
import com.axiom.operatio.model.production.machine.MachineType;
import com.axiom.operatio.model.production.machine.Operation;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.inventory.Inventory;

import static android.graphics.Color.DKGRAY;
import static android.graphics.Color.GRAY;
import static android.graphics.Color.WHITE;

// TODO Добавить отображение информации буфера
// TODO Добавить отображение информации конвейера
// TODO Добавить отображение информации импортера
// TODO Добавить отображение информации экспортера
// TODO Добавить отображение информации перкрестка конвейера
public class AdjustmentPanel extends Panel {

    public final int panelColor = 0xCC505050;
    protected Caption caption, inputsCaption, outputsCaption;
    protected Button leftButton, centerButton, rightButton;
    protected Button changeoverButton;
    protected Block choosenBlock = null;

    protected int choosenOperationID = 0;
    protected int materialID = 0;

    private long lastProductionCycle;

    private ItemWidget inpBtn[];
    private ItemWidget outBtn[];
    private int tickSound;
    private boolean collapsed = false;
 /*   private int snd1, snd2, snd3, snd4, snd5;
    private int sndConveyor, sndBuffer;*/


    public static ClickListener panelListener = new ClickListener() {
        @Override
        public void onClick(Widget w) {
            AdjustmentPanel panel = (AdjustmentPanel) w;
            if (!panel.collapsed) {
                panel.setLocation(Camera.WIDTH - 40,200);
                panel.collapsed = true;
            } else {
                panel.setLocation(Camera.WIDTH - 375,200);
                panel.collapsed = false;
            }
        }
    };

    protected ClickListener clickListener = new ClickListener() {
        @Override
        public void onClick(Widget w) {
            SoundRenderer.playSound(tickSound);
            Button button = (Button) w;
            if (choosenBlock != null) {
                if (choosenBlock instanceof Machine) {
                    Machine machine = (Machine) choosenBlock;
                    if (button.getTag().equals("<")) {
                        choosenOperationID--;
                        if (choosenOperationID < 0) choosenOperationID = 0;
                        showMachineInfo(machine, choosenOperationID);
                        if (choosenOperationID ==machine.getOperationID()) {
                            changeoverButton.setColor(GRAY);
                        } else changeoverButton.setColor(0.5f, 1, 0.5f, 1);
                    } else if (button.getTag().equals(">")) {
                        int operationsCount = machine.getType().getOperations().length;
                        choosenOperationID++;
                        if (choosenOperationID >= operationsCount) choosenOperationID = operationsCount - 1;
                        showMachineInfo(machine, choosenOperationID);
                        if (choosenOperationID ==machine.getOperationID()) {
                            changeoverButton.setColor(GRAY);
                        } else changeoverButton.setColor(0.5f, 1, 0.5f, 1);
                    } else if (button.getTag().equals("Changeover")) {
                        machine.setOperation(choosenOperationID);
                        changeoverButton.setColor(GRAY);
                    }
                } else if (choosenBlock instanceof ImportBuffer) {
                    ImportBuffer importBuffer = (ImportBuffer) choosenBlock;
                    if (button.getTag().equals("<")) {
                        materialID--;
                        if (materialID < 0) materialID = 0;
                        showImporterInfo(importBuffer, materialID);
                        if (materialID == importBuffer.getImportMaterial().getMaterialID()) {
                            changeoverButton.setColor(GRAY);
                        } else changeoverButton.setColor(0.5f, 1, 0.5f, 1);
                    } else if (button.getTag().equals(">")) {
                        int materialsAmount = Material.getMaterialsAmount();
                        materialID++;
                        if (materialID >= materialsAmount) materialID = materialsAmount - 1;
                        showImporterInfo(importBuffer, materialID);
                        if (materialID == importBuffer.getImportMaterial().getMaterialID()) {
                            changeoverButton.setColor(GRAY);
                        } else changeoverButton.setColor(0.5f, 1, 0.5f, 1);
                    } else if (button.getTag().equals("Changeover")) {
                        importBuffer.setImportMaterial(Material.getMaterial(materialID));
                        changeoverButton.setColor(GRAY);
                    }

                }
            }
        }
    };


    public AdjustmentPanel() {
        super();
        setLocalBounds(Camera.WIDTH - 375,200,375, 700);
        setColor(panelColor);
        inpBtn = new ItemWidget[4];
        outBtn = new ItemWidget[4];
        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);
        buildButtons();
       // setClickListener(panelListener);
    }


    protected void buildButtons() {
        Material m = Material.getMaterial(choosenOperationID);

        caption = new Caption("Block information");
        caption.setLocalBounds(40,599,300, 100);
        caption.setScale(1.5f);
        caption.setTextColor(WHITE);
        addChild(caption);

        leftButton = new Button("<");
        leftButton.setLocalBounds( 40, 500, 75, 100);
        leftButton.setTag("<");
        leftButton.setColor(GRAY);
        leftButton.setTextColor(WHITE);
        leftButton.setClickListener(clickListener);
        addChild(leftButton);

        centerButton = new Button("");
        centerButton.setTextScale(1.5f);
        centerButton.setLocalBounds( 140, 500, 100, 100);
        centerButton.setColor(GRAY);
        centerButton.setTextColor(WHITE);
        addChild(centerButton);

        rightButton = new Button(">");
        rightButton.setTag(">");
        rightButton.setLocalBounds( 265, 500, 75, 100);
        rightButton.setColor(GRAY);
        rightButton.setTextColor(WHITE);
        rightButton.setClickListener(clickListener);
        addChild(rightButton);

        // Список входных материалов
        inputsCaption = new Caption("Input materials:");
        inputsCaption.setLocalBounds(40,400,300, 100);
        inputsCaption.setScale(1.5f);
        inputsCaption.setTextColor(WHITE);
        addChild(inputsCaption);

        for (int i=0; i<4; i++) {
            inpBtn[i] = new ItemWidget("");
            inpBtn[i].setLocalBounds(40 + i*80, 350, 64, 64);
            inpBtn[i].setColor(DKGRAY);
            inpBtn[i].setTextColor(WHITE);
            inpBtn[i].setTextScale(1);
            addChild(inpBtn[i]);
        }

        // Список выходных материалов
        outputsCaption = new Caption("Output materials:");
        outputsCaption.setLocalBounds(40,250,300, 100);
        outputsCaption.setScale(1.5f);
        outputsCaption.setTextColor(WHITE);
        addChild(outputsCaption);


        for (int i=0; i<4; i++) {
            outBtn[i] = new ItemWidget("");
            outBtn[i].setLocalBounds(40 + i*80, 200, 64, 64);
            outBtn[i].setColor(DKGRAY);
            outBtn[i].setTextColor(WHITE);
            outBtn[i].setTextScale(1);
            addChild(outBtn[i]);
        }

        changeoverButton = new Button("Changeover");
        changeoverButton.setTextScale(1.5f);
        changeoverButton.setTag("Changeover");
        changeoverButton.setLocalBounds( 40, 50, 300, 100);
        changeoverButton.setColor(Color.GRAY);
        changeoverButton.setTextColor(WHITE);
        changeoverButton.setClickListener(clickListener);
        addChild(changeoverButton);
    }


    private int productionCycle;

    @Override
    public void draw(Camera camera) {
        super.draw(camera);
        long currentCycle = Production.getCurrentCycle();
        if (currentCycle > lastProductionCycle) {
            if (choosenBlock!=null) {
                showBlockInfo(choosenBlock, false);
            }
            lastProductionCycle = currentCycle;
        }
    }

    /**
     * Отображение информации о блоке на панели
     * @param block
     */
    public void showBlockInfo(Block block, boolean playSound) {
        boolean blockChanged = false;

        if (block!=choosenBlock) {
            blockChanged = true;
            choosenBlock = block;
            changeoverButton.setColor(GRAY);
        }

        if (block instanceof Machine) {
            Machine machine = (Machine) block;
            if (blockChanged) choosenOperationID = machine.getOperationID();
            showMachineInfo(machine, choosenOperationID);
        }
        if (block instanceof Conveyor) {
            showConveyorInfo((Conveyor) block);
        }
        if (block instanceof Buffer) {
            showBufferInfo((Buffer) block);
        }
        if (block instanceof ImportBuffer) {
            ImportBuffer importBuffer = (ImportBuffer) block;
            if (blockChanged) materialID = importBuffer.getImportMaterial().getMaterialID();
            showImporterInfo(importBuffer, materialID);
        }
       // visible = true;
    }


    public void hideBlockInfo() {
        choosenBlock = null;
      //  visible = false;
    }

    /**
     * Отображает информацию о машине
     * @param machine машина
     */
    private void showMachineInfo(Machine machine, int opID) {
        MachineType machineType = machine.getType();
        Operation[] allOperations = machineType.getOperations();

        Operation currentOperation = machineType.getOperation(opID);
        Material[] inputMaterials = currentOperation.getInputMaterials();
        Material[] outputMaterials = currentOperation.getOutputMaterials();

        leftButton.visible = true;
        centerButton.setLocalBounds( 140, 500, 100, 100);
        centerButton.setBackground(null);
        rightButton.visible = true;

        caption.setText(machineType.getName() + " operation");

        centerButton.setText("" + (opID+1) + "/" + allOperations.length);

        inputsCaption.setText("Inputs:");
        for (int i=0; i<4; i++) {
            if (i < inputMaterials.length) {
                inpBtn[i].setBackground(inputMaterials[i].getImage());
            } else {
                inpBtn[i].setBackground(null);
            }
            inpBtn[i].setText("");
            inpBtn[i].visible = true;
        }

        outputsCaption.setText("Outputs:");
        for (int i=0; i<4; i++) {
            if (i < outputMaterials.length) {
                outBtn[i].setBackground(outputMaterials[i].getImage());
            } else {
                outBtn[i].setBackground(null);
            }
            outBtn[i].setText("");
            outBtn[i].visible = true;
        }

        changeoverButton.visible = true;
    }

    /**
     * Отображает информацию буфере
     * @param buffer буфер
     */
    private void showBufferInfo(Buffer buffer) {
        caption.setText("Buffer contains");
        centerButton.setText("" + (buffer.getItemsAmount()) + "/" + (buffer.getCapacity()-1));
        centerButton.setBackground(null);
        centerButton.setLocation(40, 500);
        centerButton.setSize(300,100);
        leftButton.visible = false;
        rightButton.visible = false;

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

        outputsCaption.setText("");
        changeoverButton.visible = false;
    }

    /**
     * Отображает информацию о конвейере
     * @param conveyor конвейер
     */
    private void showConveyorInfo(Conveyor conveyor) {
        caption.setText("Conveyor contains");
        centerButton.setText("" + (conveyor.getItemsAmount()));
        centerButton.setBackground(null);
        centerButton.setLocation(40, 500);
        centerButton.setSize(300,100);
        leftButton.visible = false;
        rightButton.visible = false;

        inputsCaption.setText("Processing:");
        outputsCaption.setText("");

        // Отобразить материалы внутри конвейера
        for (int i=0; i<4; i++) {
            inpBtn[i].visible = true;



            outBtn[i].visible = false;
        }

        changeoverButton.visible = false;
    }


    /**
     * Отображает информацию об импортере со склада
     * @param importBuffer буфер импорта
     * @param materialID номер импортируемого материала
     */
    private void showImporterInfo(ImportBuffer importBuffer, int materialID) {
        Material material = Material.getMaterial(materialID);

        caption.setText("Importer");
        centerButton.setText("");
        centerButton.setBackground(material.getImage());


        leftButton.visible = true;
        centerButton.setLocalBounds( 140, 500, 100, 100);
        rightButton.visible = true;

        inputsCaption.setText(material.getName() + "\n\n"
                + "Balance: " + Inventory.getInstance().getBalance(material) + " items");
        outputsCaption.setText("");

        for (int i=0; i<4; i++) {
            inpBtn[i].visible = false;
            outBtn[i].visible = false;
        }

        changeoverButton.visible = true;
    }

}
