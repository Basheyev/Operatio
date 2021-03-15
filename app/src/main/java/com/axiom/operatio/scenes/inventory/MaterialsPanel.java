package com.axiom.operatio.scenes.inventory;

import com.axiom.atom.R;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Caption;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.gameplay.GamePermissions;
import com.axiom.operatio.model.inventory.Inventory;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.scenes.common.ItemWidget;

import java.util.ArrayList;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.RED;
import static android.graphics.Color.WHITE;

/**
 * Панель отображения списка материалов с остатками на складе
 */
public class MaterialsPanel extends Panel {

    private InventoryScene inventoryScene;
    private Production production;
    private ItemWidget[] itemWidget;
    private Material selectedMaterial;


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


    public void updatePermissions(int level) {
        GamePermissions permissions = production.getPermissions();
        for (int i=0; i<itemWidget.length; i++) {
            ItemWidget item = itemWidget[i];
            if (permissions.isAvailable(Material.getMaterial(i))) {
                item.setActive(true);
            } else {
                item.setActive(false);
            }
        }
    }


    protected void buildUI() {
        Panel panel = this;
        panel.setLocalBounds(24,60, 820, 880);
        panel.setColor(0xCC505050);

        Caption caption = new Caption("Inventory");
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


    private ClickListener clickListener = new ClickListener() {

        protected int tickSound =-1;

        @Override
        public void onClick(Widget w) {
            if (w.getTag()==null) return;
            if (tickSound == -1) tickSound = SoundRenderer.loadSound(R.raw.tick_snd);
            int materialID = Integer.parseInt(w.getTag());
            Material material = Material.getMaterial(materialID);
            MaterialsPanel materialsPanel = (MaterialsPanel) w.getParent();

            ItemWidget item = (ItemWidget) w;
            if (!item.isActive()) return;

            if (w.getColor()!=RED) {
                unselectAllButtons(w);
                w.setColor(RED);
                materialsPanel.selectedMaterial = material;
                SoundRenderer.playSound(tickSound);
            } else {
                unselectAllButtons(w);
                materialsPanel.selectedMaterial = null;
            }
            materialsPanel.inventoryScene.getMarketPanel().updateValues();
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
