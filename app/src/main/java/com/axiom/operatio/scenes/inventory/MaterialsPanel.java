package com.axiom.operatio.scenes.inventory;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Caption;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.inventory.Inventory;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.scenes.production.view.ItemWidget;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

/**
 * Панель отображения списка материалов с остатками на складе
 */
public class MaterialsPanel extends Panel {

    protected static ItemWidget[] itemWidget;

    public MaterialsPanel() {
        super();
        buildUI();
    }

    public void updateData() {
        Inventory inventory = Inventory.getInstance();
        for (int i = 0; i< Material.getMaterialsAmount(); i++) {
            Material material = Material.getMaterial(i);
            int balance = inventory.getBalance(material);
            itemWidget[i].setText("" + balance);
        }
    }

    protected void buildUI() {
        Panel panel = this;
        panel.setLocalBounds(50,100, 820, Camera.HEIGHT - 200);
        panel.setColor(0xCC505050);

        Caption caption = new Caption("Materials");
        caption.setScale(1.5f);
        caption.setTextColor(WHITE);
        caption.setLocalBounds(30, panel.getHeight() - 100, 300, 100);
        panel.addChild(caption);

        Inventory inventory = Inventory.getInstance();
        itemWidget = new ItemWidget[Material.getMaterialsAmount()];
        float x = 30, y = 700;
        for (int i=0; i< Material.getMaterialsAmount(); i++) {
            Material material = Material.getMaterial(i);
            int balance = inventory.getBalance(material);
            itemWidget[i] = new ItemWidget("" + balance);
            itemWidget[i].setColor(BLACK);
            itemWidget[i].setBackground(material.getImage());
            itemWidget[i].setTextScale(1);
            itemWidget[i].setTextColor(WHITE);
            itemWidget[i].setLocalBounds(x, y, 80, 80);
            panel.addChild(itemWidget[i]);
            x += 96;
            if (x + 96 > panel.getWidth()) {
                x = 30;
                y -= 96;
            }
        }
    }

}
