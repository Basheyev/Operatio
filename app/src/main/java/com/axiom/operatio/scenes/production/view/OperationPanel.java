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
import com.axiom.operatio.model.block.Block;
import com.axiom.operatio.model.buffer.Buffer;
import com.axiom.operatio.model.conveyor.Conveyor;
import com.axiom.operatio.model.machine.Machine;
import com.axiom.operatio.model.machine.MachineType;
import com.axiom.operatio.model.machine.Operation;
import com.axiom.operatio.model.materials.Material;

import static android.graphics.Color.DKGRAY;
import static android.graphics.Color.GRAY;
import static android.graphics.Color.WHITE;

public class OperationPanel extends Panel {

    public final int panelColor = 0xCC505050;
    protected Caption caption;
    protected Button leftButton, operationButton, rightButton;
    protected Button changeoverButton;
    protected Block choosenBlock = null;
    protected int operationID = 0;

    private Button inpBtn[];
    private Button outBtn[];
    private int tickSound;
    private int snd1, snd2, snd3, snd4, snd5;
    private int sndConveyor, sndBuffer;

    protected ClickListener clickListener = new ClickListener() {
        @Override
        public void onClick(Widget w) {
            SoundRenderer.playSound(tickSound);
            Button button = (Button) w;
            if (choosenBlock != null) {
                if (choosenBlock instanceof Machine) {
                    Machine machine = (Machine) choosenBlock;
                    if (button.getTag().equals("<")) {
                        operationID--;
                        if (operationID < 0) operationID = 0;
                        showMachineInfo(machine, operationID);
                        if (operationID==machine.getOperationID()) {
                            changeoverButton.setColor(GRAY);
                        } else changeoverButton.setColor(0.5f, 1, 0.5f, 1);
                    } else if (button.getTag().equals(">")) {
                        int operationsCount = machine.getType().getOperations().length;
                        operationID++;
                        if (operationID >= operationsCount) operationID = operationsCount - 1;
                        showMachineInfo(machine, operationID);
                        if (operationID==machine.getOperationID()) {
                            changeoverButton.setColor(GRAY);
                        } else changeoverButton.setColor(0.5f, 1, 0.5f, 1);
                    } else if (button.getTag().equals("Changeover")) {
                        machine.setOperation(operationID);
                        changeoverButton.setColor(GRAY);
                    }
                }
            }
        }
    };


    public OperationPanel() {
        super();
        setLocalBounds(Camera.WIDTH - 400,200,400, 700);
        setColor(panelColor);
        inpBtn = new Button[4];
        outBtn = new Button[4];
        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);
        snd1 = SoundRenderer.loadSound(R.raw.machine_press);
        snd2 = SoundRenderer.loadSound(R.raw.machine_roller);
        snd3 = SoundRenderer.loadSound(R.raw.machine_cutter);
        snd4 = SoundRenderer.loadSound(R.raw.machine_extruder);
        snd5 = SoundRenderer.loadSound(R.raw.machine_assembly);
        sndConveyor = SoundRenderer.loadSound(R.raw.conveyor_snd);
        sndBuffer = SoundRenderer.loadSound(R.raw.buffer_snd);
        buildButtons();
    }


    protected void buildButtons() {
        Material m = Material.getMaterial(operationID);

        caption = new Caption("Block information");
        caption.setLocalBounds(30,599,300, 100);
        caption.setScale(1.5f);
        caption.setTextColor(WHITE);
        addChild(caption);

        leftButton = new Button("<");
        leftButton.setLocalBounds( 30, 500, 75, 100);
        leftButton.setTag("<");
        leftButton.setColor(GRAY);
        leftButton.setTextColor(WHITE);
        leftButton.setClickListener(clickListener);
        addChild(leftButton);

        operationButton = new Button("");
        operationButton.setTextScale(1.5f);
        operationButton.setLocalBounds( 130, 500, 100, 100);
        operationButton.setColor(GRAY);
        operationButton.setTextColor(WHITE);
        addChild(operationButton);

        rightButton = new Button(">");
        rightButton.setTag(">");
        rightButton.setLocalBounds( 255, 500, 75, 100);
        rightButton.setColor(GRAY);
        rightButton.setTextColor(WHITE);
        rightButton.setClickListener(clickListener);
        addChild(rightButton);

        // Список входных материалов
        Caption inputsCaption = new Caption("Input materials:");
        inputsCaption.setLocalBounds(30,400,300, 100);
        inputsCaption.setScale(1.5f);
        inputsCaption.setTextColor(WHITE);
        addChild(inputsCaption);

        for (int i=0; i<4; i++) {
            inpBtn[i] = new Button("");
            inpBtn[i].setLocalBounds(30 + i*80, 350, 64, 64);
            inpBtn[i].setColor(DKGRAY);
            inpBtn[i].setTextColor(WHITE);
            addChild(inpBtn[i]);
        }

        // Список выходных материалов
        Caption outputsCaption = new Caption("Output materials:");
        outputsCaption.setLocalBounds(30,250,300, 100);
        outputsCaption.setScale(1.5f);
        outputsCaption.setTextColor(WHITE);
        addChild(outputsCaption);


        for (int i=0; i<4; i++) {
            outBtn[i] = new Button("");
            outBtn[i].setLocalBounds(30 + i*80, 200, 64, 64);
            outBtn[i].setColor(DKGRAY);
            outBtn[i].setTextColor(WHITE);
            addChild(outBtn[i]);
        }

        changeoverButton = new Button("Changeover");
        changeoverButton.setTextScale(1.5f);
        changeoverButton.setTag("Changeover");
        changeoverButton.setLocalBounds( 30, 50, 300, 100);
        changeoverButton.setColor(Color.GRAY);
        changeoverButton.setTextColor(WHITE);
        changeoverButton.setClickListener(clickListener);
        addChild(changeoverButton);
    }


    /**
     * Отображение информации о блоке на панели
     * @param block
     */
    public void showBlockInfo(Block block, boolean playSound) {
        choosenBlock = block;
        changeoverButton.setColor(GRAY);
        if (block instanceof Machine) {
            Machine machine = (Machine) block;
            /*if (playSound) {
                switch (machine.getType().getID()) {
                    case 0:
                        SoundRenderer.playSound(snd1);
                        break;
                    case 1:
                        SoundRenderer.playSound(snd2);
                        break;
                    case 2:
                        SoundRenderer.playSound(snd3);
                        break;
                    case 3:
                        SoundRenderer.playSound(snd4);
                        break;
                    case 4:
                        SoundRenderer.playSound(snd5);
                        break;
                }
            }*/
            operationID = machine.getOperationID();
            showMachineInfo(machine, operationID);
        }
        if (block instanceof Conveyor) {
            showConveyorInfo((Conveyor) block);
            SoundRenderer.playSound(sndConveyor);
        }
        if (block instanceof Buffer) {
            showBufferInfo((Buffer) block);
            SoundRenderer.playSound(sndBuffer);
        }
        visible = true;
    }


    public void hideBlockInfo() {
        choosenBlock = null;
        visible = false;
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
        operationButton.setLocation(130, 500);
        operationButton.setSize(100,100);
        rightButton.visible = true;

        caption.setText(machineType.getName() + " operation");

        operationButton.setText("" + (opID+1) + "/" + allOperations.length);

        for (int i=0; i<4; i++) {
            if (i < inputMaterials.length) {
                inpBtn[i].setBackground(inputMaterials[i].getImage());
            } else {
                inpBtn[i].setBackground(null);
            }
        }

        for (int i=0; i<4; i++) {
            if (i < outputMaterials.length) {
                outBtn[i].setBackground(outputMaterials[i].getImage());
            } else {
                outBtn[i].setBackground(null);
            }
        }

    }


    private void showBufferInfo(Buffer buffer) {
        caption.setText("Buffer contains");
        operationButton.setText("" + (buffer.getItemsAmount()) + "/" + (buffer.getCapacity()-1));
        operationButton.setLocation(30, 500);
        operationButton.setSize(300,100);
        leftButton.visible = false;
        rightButton.visible = false;
    }

    private void showConveyorInfo(Conveyor conveyor) {

    }

}
