package com.axiom.operatio.scenes.inventory;

import com.axiom.atom.engine.graphics.renderers.Text;
import com.axiom.atom.engine.ui.widgets.Caption;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.operatio.model.inventory.Inventory;
import com.axiom.operatio.model.inventory.Market;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.Production;

import static android.graphics.Color.WHITE;

public class StockKeepingUnitPanel extends Panel {

    public static final int PANEL_COLOR = 0xCC505050;

    private InventoryScene scene;
    private Inventory inventory;
    private MaterialsPanel materialsPanel;

    private Caption caption;
    private StringBuffer captionText = new StringBuffer(128);

    public StockKeepingUnitPanel(InventoryScene scene) {
        super();
        this.scene = scene;
        this.inventory = scene.getInventory();
        this.materialsPanel = scene.getMaterialsPanel();
        buildUI();
    }

    private void buildUI() {
        setColor(PANEL_COLOR);
        setLocalBounds(874, 50, 1022, 300);

        caption = new Caption("balance");
        caption.setOpaque(true);
        caption.setLocalBounds(25, 25, 800, 250);
        caption.setTextColor(WHITE);
        caption.setTextScale(1.3f);
        caption.setVerticalAlignment(Text.ALIGN_TOP);
        addChild(caption);
    }

    public void updateData() {
        captionText.setLength(0);
        Material material = materialsPanel.getSelectedMaterial();
        if (material==null) return;
        int materialID = material.getID();
        captionText.append(material.getName());
        captionText.append("\n\n");
        captionText.append("- Daily hyper stock quantity: ");
        captionText.append(inventory.getDailyBalance(materialID));
        captionText.append("\n");
        captionText.append("- Daily out of stock quantity: ");
        captionText.append(inventory.getDailyOutOfStock(materialID));
        caption.setText(captionText);
    }

}
