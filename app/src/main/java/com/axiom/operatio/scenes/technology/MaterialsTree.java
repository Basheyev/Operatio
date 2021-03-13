package com.axiom.operatio.scenes.technology;

import com.axiom.atom.R;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Caption;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.gameplay.Level;
import com.axiom.operatio.model.gameplay.LevelFactory;
import com.axiom.operatio.model.inventory.Inventory;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.scenes.production.view.ItemWidget;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.RED;
import static android.graphics.Color.WHITE;

public class MaterialsTree extends Panel {

    public static final int AVAILABLE = BLACK;
    public static final int UNAVAILABLE = 0xA0220026;
    public static final int SELECTED = RED;

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


    public void updatePermissions(int level) {
        LevelFactory lm = LevelFactory.getInstance();
        Level currentLevel = lm.getLevel(level);
        for (int i=0; i<itemWidget.length; i++) {
            int backgroundColor = UNAVAILABLE;
            if (currentLevel.isMaterialAvailable(i)) backgroundColor = AVAILABLE;
            itemWidget[i].setColor(backgroundColor);
        }
    }


    protected void buildUI() {
        Panel panel = this;
        panel.setLocalBounds(24,60, 820, 880);
        panel.setColor(0xCC505050);

        Caption caption = new Caption("Materials");
        caption.setTextScale(1.5f);
        caption.setTextColor(WHITE);
        caption.setLocalBounds(30, panel.getHeight() - 100, 300, 100);
        panel.addChild(caption);

        LevelFactory levelFactory = LevelFactory.getInstance();
        Level currentLevel = levelFactory.getLevel(production.getLevel());
        Inventory inventory = production.getInventory();

        itemWidget = new ItemWidget[Material.getMaterialsAmount()];
        float x = 30, y = 30;
        for (int i=0; i< Material.getMaterialsAmount(); i++) {
            Material material = Material.getMaterial(i);
            itemWidget[i] = new ItemWidget("" + inventory.getBalance(material));
            int backgroundColor = UNAVAILABLE;
            if (currentLevel.isMaterialAvailable(i)) backgroundColor = AVAILABLE;
            itemWidget[i].setColor(backgroundColor);
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
            MaterialsTree materialsTree = (MaterialsTree) w.getParent();

            ItemWidget item = (ItemWidget) w;
            if (!item.isActive()) return;

            if (w.getColor()!=SELECTED) {
                unselectAllButtons(w);
                w.setColor(SELECTED);
                materialsTree.selectedMaterial = material;
                materialsTree.getTechnologyScene().getRecipePanel().updateData();
                SoundRenderer.playSound(tickSound);
            } else {
                unselectAllButtons(w);
                materialsTree.selectedMaterial = null;
                materialsTree.getTechnologyScene().getRecipePanel().updateData();
            }
        }

        public void unselectAllButtons(Widget w) {
            LevelFactory levelFactory = LevelFactory.getInstance();
            Level currentLevel = levelFactory.getLevel(production.getLevel());
            for (int i=0; i< Material.getMaterialsAmount(); i++) {
                int backgroundColor = UNAVAILABLE;
                if (currentLevel.isMaterialAvailable(i)) backgroundColor = AVAILABLE;
                itemWidget[i].setColor(backgroundColor);
            }
            MaterialsTree materialsTree = (MaterialsTree) w.getParent();
            materialsTree.selectedMaterial = null;
        }

    };


    public TechnologyScene getTechnologyScene() {
        return technologyScene;
    }
}
