package com.axiom.operatio.scenes.inventory;

import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Caption;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.inventory.Inventory;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.scenes.production.view.ItemWidget;

import java.util.ArrayList;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.RED;
import static android.graphics.Color.WHITE;

/**
 * Панель отображения списка материалов с остатками на складе
 */
public class MaterialsPanel extends Panel {

    protected InventoryScene inventoryScene;
    protected Production production;
    protected ItemWidget[] itemWidget;
    protected Material selectedMaterial;

    public MaterialsPanel(Production production, InventoryScene scene) {
        super();
        this.production = production;
        inventoryScene = scene;
        buildUI();
    }

    public void updateData() {
        Inventory inventory = production.getInventory();
        for (int i = 0; i< Material.getMaterialsAmount(); i++) {
            Material material = Material.getMaterial(i);
            int balance = inventory.getBalance(material);
            if (balance > 0) {
                itemWidget[i].setText("" + balance);
            } else {
                itemWidget[i].setText("");
            }
        }
    }

    protected void buildUI() {
        Panel panel = this;
        panel.setLocalBounds(50,100, 820, Camera.HEIGHT - 200);
        panel.setColor(0xCC505050);

        Caption caption = new Caption("Materials");
        caption.setTextScale(1.5f);
        caption.setTextColor(WHITE);
        caption.setLocalBounds(30, panel.getHeight() - 100, 300, 100);
        panel.addChild(caption);

        Inventory inventory = production.getInventory();
        itemWidget = new ItemWidget[Material.getMaterialsAmount()];
        float x = 30, y = 30;
        for (int i=0; i< Material.getMaterialsAmount(); i++) {
            Material material = Material.getMaterial(i);
            int balance = inventory.getBalance(material);
            itemWidget[i] = new ItemWidget("" + balance);
            itemWidget[i].setColor(BLACK);
            itemWidget[i].setBackground(material.getImage());
            itemWidget[i].setTextScale(1);
            itemWidget[i].setTextColor(WHITE);
            itemWidget[i].setLocalBounds(x, y, 80, 80);
            itemWidget[i].setClickListener(clickListener);
            itemWidget[i].setTag("" + material.getMaterialID());
            panel.addChild(itemWidget[i]);
            x += 96;
            if (x + 96 > panel.getWidth()) {
                x = 30;
                y += 96;
            }
        }
        selectedMaterial = null;
    }

    public Material getSelectedMaterial() {
        return selectedMaterial;
    }

    protected static ClickListener clickListener = new ClickListener() {

        @Override
        public void onClick(Widget w) {
            if (w.getTag()==null) return;
            int materialID = Integer.parseInt(w.getTag());
            Material material = Material.getMaterial(materialID);
            MaterialsPanel materialsPanel = (MaterialsPanel) w.getParent();
            if (w.getColor()!=RED) {
                unselectAllButtons(w);
                w.setColor(RED);
                materialsPanel.selectedMaterial = material;
                materialsPanel.inventoryScene.technologyPanel.updateData();
            } else {
                unselectAllButtons(w);
                materialsPanel.selectedMaterial = null;
                materialsPanel.inventoryScene.technologyPanel.updateData();
            }
        }

        public void unselectAllButtons(Widget w) {
            ArrayList<Widget> children = w.getParent().getChildren();
            for (int i=0; i<children.size(); i++) {
                children.get(i).setColor(BLACK);
            }
            MaterialsPanel materialsPanel = (MaterialsPanel) w.getParent();
            materialsPanel.selectedMaterial = null;
        }

    };

}
