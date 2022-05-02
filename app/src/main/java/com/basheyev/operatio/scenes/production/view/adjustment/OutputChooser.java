package com.basheyev.operatio.scenes.production.view.adjustment;

import android.view.MotionEvent;

import com.basheyev.atom.engine.core.GameLoop;
import com.basheyev.atom.engine.data.events.GameEvent;
import com.basheyev.atom.engine.data.events.GameEventSubscriber;
import com.basheyev.atom.engine.graphics.gles2d.Camera;
import com.basheyev.atom.engine.ui.listeners.ClickListener;
import com.basheyev.atom.engine.ui.widgets.Panel;
import com.basheyev.atom.engine.ui.widgets.Widget;
import com.basheyev.operatio.model.gameplay.GamePermissions;
import com.basheyev.operatio.model.gameplay.OperatioEvents;
import com.basheyev.operatio.model.materials.Material;
import com.basheyev.operatio.model.production.buffer.ImportBuffer;
import com.basheyev.operatio.model.production.inserter.Inserter;
import com.basheyev.operatio.model.production.machine.Machine;
import com.basheyev.operatio.model.production.machine.MachineType;
import com.basheyev.operatio.model.production.machine.Operation;
import com.basheyev.operatio.scenes.common.ItemWidget;
import com.basheyev.operatio.scenes.production.ProductionScene;

import static android.graphics.Color.WHITE;

/**
 * Выбирает выходной материал для AdjustmentPanel
 */
public class OutputChooser extends Panel implements GameEventSubscriber {

    public static final int ITEM_BACKGROUND = 0x80000000;
    public static final int ITEM_SELECTED = 0xFFd5c01f;

    public static final int MACHINE = 1;
    public static final int IMPORTER = 2;
    public static final int INSERTER = 3;

    private ProductionScene scene;
    private AdjustmentPanel adjustmentPanel;
    private ItemWidget[] itemWidget;
    private long lastChangeTime = 0;

    private int blockType = 0;
    private Machine machine = null;
    private ImportBuffer importer = null;
    private Inserter inserter = null;

    public OutputChooser(ProductionScene scene, AdjustmentPanel adjustmentPanel) {
        super();
        this.scene = scene;
        this.adjustmentPanel = adjustmentPanel;
        GameLoop.getInstance().addGameEventSubscriber(this);
        buildUI();
    }

    private void buildUI() {
        int panelWidth = 680;
        setLocalBounds(Camera.WIDTH - 390 - panelWidth,160,panelWidth, 700);
        setColor(0xCC505050);

        // max 64 output 8 x 8 для импортера
        itemWidget = new ItemWidget[Material.getMaterialsAmount()];
        float x = 30, y = 30;
        for (int i=0; i< Material.getMaterialsAmount(); i++) {
            Material material = Material.getMaterial(i);
            itemWidget[i] = new ItemWidget("");
            itemWidget[i].setColor(ITEM_BACKGROUND);
            itemWidget[i].setBackground(null);
            itemWidget[i].setTextScale(1);
            itemWidget[i].setTextColor(WHITE);
            itemWidget[i].setLocalBounds(x, y, 64, 64);
            itemWidget[i].setTag("" + material.getID());
            itemWidget[i].setClickListener(clickListener);
            itemWidget[i].setActive(true);
            addChild(itemWidget[i]);
            x += 80;
            if (x + 80 > getWidth()) {
                x = 30;
                y += 80;
            }
        }
    }


    private void clearAll() {
        for (ItemWidget widget : itemWidget) {
            widget.setBackground(null);
            widget.setColor(ITEM_BACKGROUND);
            widget.setTag("-1");
        }
    }


    private void unselectAll() {
        for (ItemWidget widget : itemWidget) {
            widget.setColor(ITEM_BACKGROUND);
        }
    }


    public void showMachineOutputs(Machine machine) {

        GamePermissions permissions = scene.getProduction().getPermissions();

        if (blockType==MACHINE && this.machine==machine) {
            long permissionChangeTime = permissions.getLastChangeTime();
            if (permissionChangeTime > lastChangeTime) {
                lastChangeTime = permissionChangeTime;
            } else return;
        }

        clearAll();
        MachineType type = machine.getType();
        Operation[] operations = type.getOperations();

        int index = 0;
        for (int i=0; i < operations.length; i++) {
            if (permissions.isAvailable(operations[i])) {
                itemWidget[index].setBackground(operations[i].getOutputs()[0].getImage());
                itemWidget[index].setTag("" + i); // код операции машины

                if (machine.getOperation()==operations[i]) {
                    itemWidget[index].setColor(ITEM_SELECTED);
                }

                index++;
            }
        }

        blockType = MACHINE;
        this.machine = machine;

    }


    public void showImporterMaterials(ImportBuffer importBuffer) {

        clearAll();

        GamePermissions permissions = scene.getProduction().getPermissions();

        int index = 0;
        for (int i=0; i < Material.getMaterialsAmount(); i++) {
            Material material = Material.getMaterial(i);
            if (permissions.isAvailable(material)) {
                itemWidget[index].setBackground(material.getImage());
                itemWidget[index].setTag("" + i);

                if (importBuffer.getImportMaterial()==material) {
                    itemWidget[index].setColor(ITEM_SELECTED);
                }

                index++;
            }
        }

        blockType = IMPORTER;
        importer = importBuffer;
    }


    public void showInserterMaterials(Inserter inserter) {

        clearAll();

        GamePermissions permissions = scene.getProduction().getPermissions();
        int index = 0;

        for (int i=0; i < Material.getMaterialsAmount(); i++) {
            Material material = Material.getMaterial(i);
            if (permissions.isAvailable(material) && material != null) {
                itemWidget[index].setBackground(material.getImage());
                itemWidget[index].setTag("" + i);
                if (inserter.getTargetMaterial()==material) {
                    itemWidget[index].setColor(ITEM_SELECTED);
                }
                index++;
            }
        }

        blockType = INSERTER;
        this.inserter = inserter;
    }


    @Override
    public boolean onMotionEvent(MotionEvent event, float worldX, float worldY) {
        super.onMotionEvent(event, worldX, worldY);
        return true;
    }

    private ClickListener clickListener = new ClickListener() {
        @Override
        public void onClick(Widget w) {
            ItemWidget selectedIW = (ItemWidget) w;
            if (blockType==MACHINE && machine != null) {
                int opIndex = Integer.parseInt(selectedIW.getTag());
                adjustmentPanel.showMachineInfo(machine, opIndex);
                adjustmentPanel.selectMachineOperation(machine, opIndex);
                unselectAll();
                selectedIW.setColor(ITEM_SELECTED);
            } else if (blockType==IMPORTER) {
                int matID = Integer.parseInt(selectedIW.getTag());
                if (itemWidget[matID].getColor()==ITEM_SELECTED) matID = -1;
                adjustmentPanel.showImporterInfo(importer, matID);
                adjustmentPanel.selectImporterMaterial(importer, matID);
                unselectAll();
                if (matID==-1) selectedIW.setColor(ITEM_BACKGROUND);
                else selectedIW.setColor(ITEM_SELECTED);
            } else if (blockType==INSERTER) {
                int matID = Integer.parseInt(selectedIW.getTag());
                if (itemWidget[matID].getColor()==ITEM_SELECTED) matID = -1;
                adjustmentPanel.showInserterInfo(inserter, matID);
                adjustmentPanel.selectInserterMaterial(inserter, matID);
                unselectAll();
                if (matID==-1) selectedIW.setColor(ITEM_BACKGROUND);
                else selectedIW.setColor(ITEM_SELECTED);
            }

        }
    };


    @Override
    public boolean onGameEvent(GameEvent event) {
        if (event.getTopic() == OperatioEvents.MATERIAL_RESEARCHED ||
            event.getTopic() == OperatioEvents.OPERATION_RESEARCHED) {
            if (blockType==INSERTER && inserter != null) showInserterMaterials(inserter);
            if (blockType==IMPORTER && importer != null) showImporterMaterials(importer);
            if (blockType==MACHINE && machine != null) showMachineOutputs(machine);
        }
        return false;
    }
}
