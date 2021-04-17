package com.axiom.operatio.scenes.production.view;

import android.view.MotionEvent;

import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.gameplay.GamePermissions;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.buffer.ImportBuffer;
import com.axiom.operatio.model.production.machine.Machine;
import com.axiom.operatio.model.production.machine.MachineType;
import com.axiom.operatio.model.production.machine.Operation;
import com.axiom.operatio.scenes.common.ItemWidget;
import com.axiom.operatio.scenes.production.ProductionScene;

import static android.graphics.Color.WHITE;

/**
 * Выбирает выходной материал для AdjustmentPanel
 */
public class OutputChooser extends Panel {

    public static final int ITEM_BACKGROUND = 0x80000000;
    public static final int ITEM_SELECTED = 0xFFd5c01f;

    public static final int MACHINE = 1;
    public static final int IMPORTER = 2;

    private ProductionScene scene;
    private AdjustmentPanel adjustmentPanel;
    private ItemWidget[] itemWidget;
    private ItemWidget selectedItem;

    private int blockType = 0;
    private Machine machine = null;
    private ImportBuffer importer = null;

    public OutputChooser(ProductionScene scene, AdjustmentPanel adjustmentPanel) {
        super();
        this.scene = scene;
        this.adjustmentPanel = adjustmentPanel;
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
        selectedItem = null;
    }


    private void clearAll() {
        for (ItemWidget widget : itemWidget) {
            widget.setBackground(null);
            widget.setColor(ITEM_BACKGROUND);
        }
    }


    private void unselectAll() {
        for (ItemWidget widget : itemWidget) {
            widget.setColor(ITEM_BACKGROUND);
        }
    }


    public void showMachineOutputs(Machine machine) {

        if (blockType==MACHINE && this.machine==machine) return;

        clearAll();

        GamePermissions permissions = scene.getProduction().getPermissions();
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
                index++;
            }
        }

        blockType = IMPORTER;
        importer = importBuffer;
    }


    @Override
    public boolean onMotionEvent(MotionEvent event, float worldX, float worldY) {
        super.onMotionEvent(event, worldX, worldY);
        return true;
    }

    private ClickListener clickListener = new ClickListener() {
        @Override
        public void onClick(Widget w) {
            ItemWidget itemWidget = (ItemWidget) w;
            if (blockType==MACHINE && machine != null) {
                int opIndex = Integer.parseInt(itemWidget.getTag());
                adjustmentPanel.showMachineInfo(machine, opIndex);
                adjustmentPanel.selectMachineOperation(machine, opIndex);
                unselectAll();
                itemWidget.setColor(ITEM_SELECTED);
                selectedItem = itemWidget;
            } else if (blockType==IMPORTER) {
                int matID = Integer.parseInt(itemWidget.getTag());
                adjustmentPanel.showImporterInfo(importer, matID);
                adjustmentPanel.selectImporterMaterial(importer, matID);
                unselectAll();
                itemWidget.setColor(ITEM_SELECTED);
                selectedItem = itemWidget;
            }
        }
    };

}
