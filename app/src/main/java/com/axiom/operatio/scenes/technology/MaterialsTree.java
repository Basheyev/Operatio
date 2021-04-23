package com.axiom.operatio.scenes.technology;

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
import com.axiom.operatio.model.production.machine.MachineType;
import com.axiom.operatio.model.production.machine.Operation;
import com.axiom.operatio.scenes.common.ItemWidget;

import static android.graphics.Color.WHITE;

public class MaterialsTree extends Panel {

    public static final int AVAILABLE = 0x80000000;
    public static final int UNAVAILABLE = 0x50220026;
    public static final int SELECTED = 0xFFd5c01f;

    private TechnologyScene technologyScene;
    private Production production;
    private ItemWidget[] itemWidget;
    private Material selectedMaterial;


    public MaterialsTree(Production production, TechnologyScene scene) {
        super();
        this.production = production;
        technologyScene = scene;
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


    public void updatePermissions() {
        GamePermissions permissions = production.getPermissions();
        for (int i=0; i<itemWidget.length; i++) {
            int backgroundColor = UNAVAILABLE;
            Material material = Material.getMaterial(i);
            Operation operation = MachineType.findOperation(material);
            boolean materialAvailable = permissions.isAvailable(material);
            boolean operationAvailable = permissions.isAvailable(operation);
            boolean rawMaterial = i < 8;
            float alpha;
            if (materialAvailable && (operationAvailable || rawMaterial)) {
                backgroundColor = AVAILABLE;
                alpha = 1;
            } else {
                alpha = 0.7f;
            }
            itemWidget[i].setColor(backgroundColor);
            itemWidget[i].setSpriteAlpha(alpha);
        }
    }


    protected void buildUI() {
        Panel panel = this;
        panel.setLocalBounds(24,50, 820, 880);
        panel.setColor(0xCC505050);

        Caption caption = new Caption("Materials recipes");
        caption.setTextScale(1.5f);
        caption.setTextColor(WHITE);
        caption.setLocalBounds(30, panel.getHeight() - 100, 300, 100);
        panel.addChild(caption);

        GamePermissions permissions = production.getPermissions();
        Inventory inventory = production.getInventory();

        itemWidget = new ItemWidget[Material.getMaterialsAmount()];
        float x = 30, y = 30;
        for (int i=0; i< Material.getMaterialsAmount(); i++) {
            Material material = Material.getMaterial(i);
            itemWidget[i] = new ItemWidget("" + inventory.getBalance(material));
            int backgroundColor = UNAVAILABLE;
            if (permissions.isAvailable(Material.getMaterial(i))) backgroundColor = AVAILABLE;

            itemWidget[i].setColor(backgroundColor);
            itemWidget[i].setBackground(material.getImage());
            itemWidget[i].setTextScale(1);
            itemWidget[i].setTextColor(WHITE);
            itemWidget[i].setLocalBounds(x, y, 80, 80);
            itemWidget[i].setClickListener(clickListener);
            itemWidget[i].setTag("" + material.getID());
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


    public void setSelectedMaterial(Material material) {
        if (material==null) return;
        unselectLastButton();
        itemWidget[material.getID()].setColor(SELECTED);
        selectedMaterial = material;
        getTechnologyScene().getRecipePanel().updateData();
    }


    public void unselectLastButton() {
        if (selectedMaterial==null) return;
        GamePermissions permissions = production.getPermissions();
        ItemWidget lastSelected = itemWidget[selectedMaterial.getID()];
        int backgroundColor = UNAVAILABLE;
        if (permissions.isAvailable(selectedMaterial)) backgroundColor = AVAILABLE;
        lastSelected.setColor(backgroundColor);
        selectedMaterial = null;
    }


    private ClickListener clickListener = new ClickListener() {

        protected int tickSound =-1;

        @Override
        public void onClick(Widget clickedItem) {
            if (clickedItem.getTag()==null) return;
            if (tickSound == -1) tickSound = SoundRenderer.loadSound(R.raw.tick_snd);
            int materialID = Integer.parseInt(clickedItem.getTag());
            Material material = Material.getMaterial(materialID);
            MaterialsTree materialsTree = (MaterialsTree) clickedItem.getParent();

            ItemWidget item = (ItemWidget) clickedItem;
            if (!item.isActive()) return;

            if (clickedItem.getColor()!=SELECTED) {
                unselectLastButton(clickedItem);
                clickedItem.setColor(SELECTED);
                materialsTree.selectedMaterial = material;
                materialsTree.getTechnologyScene().getRecipePanel().updateData();
                SoundRenderer.playSound(tickSound);
            } else {
                unselectLastButton(clickedItem);
                materialsTree.selectedMaterial = null;
                materialsTree.getTechnologyScene().getRecipePanel().updateData();
            }
        }

        public void unselectLastButton(Widget w) {
            MaterialsTree materialsTree = (MaterialsTree) w.getParent();
            materialsTree.unselectLastButton();
        }

    };


    public TechnologyScene getTechnologyScene() {
        return technologyScene;
    }
}
