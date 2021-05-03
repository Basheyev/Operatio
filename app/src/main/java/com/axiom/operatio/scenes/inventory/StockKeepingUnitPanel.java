package com.axiom.operatio.scenes.inventory;

import com.axiom.atom.engine.graphics.renderers.Text;
import com.axiom.atom.engine.ui.widgets.Caption;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.operatio.model.inventory.Inventory;
import com.axiom.operatio.model.materials.Material;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.GRAY;
import static android.graphics.Color.WHITE;

public class StockKeepingUnitPanel extends Panel {

    public static final int PANEL_COLOR = 0xCC505050;
    public static final int EXCLAMATION_COLOR = 0xFF9d3e4d;
    public static final int OK_COLOR = 0xFF80B380;

    private InventoryScene scene;
    private Inventory inventory;
    private MaterialsPanel materialsPanel;

    private Caption headerCaption;

    private Caption balanceCaption;
    private Caption contractCaption;
    private Caption outOfStockCaption;

    private StringBuffer headerText;
    private StringBuffer balanceText;
    private StringBuffer contractText;
    private StringBuffer outOfStockText;


    public StockKeepingUnitPanel(InventoryScene scene) {
        super();
        this.scene = scene;
        this.inventory = scene.getInventory();
        this.materialsPanel = scene.getMaterialsPanel();
        headerText = new StringBuffer(64);
        balanceText = new StringBuffer();
        contractText = new StringBuffer();
        outOfStockText = new StringBuffer();
        buildUI();
    }

    private void buildUI() {
        setColor(PANEL_COLOR);
        setLocalBounds(874, 50, 1022, 300);

        headerText.append("Stock Keeping Unit");
        headerCaption = buildCaption(headerText, 25, 225, 800, 50, 0);
        headerCaption.setHorizontalAlignment(Text.ALIGN_LEFT);
        headerCaption.setVerticalAlignment(Text.ALIGN_TOP);

        balanceCaption = buildCaption(balanceText, 25, 25, 300, 200, GRAY);
        contractCaption = buildCaption(contractText, 350, 25, 300, 200, EXCLAMATION_COLOR);
        outOfStockCaption = buildCaption(outOfStockText, 675, 25, 300, 200, EXCLAMATION_COLOR);
    }



    private Caption buildCaption(StringBuffer buffer, float x, float y, float w, float h, int color) {
        Caption caption = new Caption(buffer);
        caption.setLocalBounds(x,y,w,h);
        caption.setTextColor(WHITE);
        caption.setTextScale(1.3f);
        caption.setVerticalAlignment(Text.ALIGN_CENTER);
        caption.setHorizontalAlignment(Text.ALIGN_CENTER);
        caption.setColor(color);
        caption.setOpaque(color != 0);
        addChild(caption);
        return caption;
    }



    public void updateData() {

        Material material = materialsPanel.getSelectedMaterial();

        headerText.setLength(0);
        headerText.append("Stock Keeping Unit daily report: ");
        balanceText.setLength(0);
        balanceText.append("Daily balance: ");
        contractText.setLength(0);
        contractText.append("Nonperformance:\n\n");
        outOfStockText.setLength(0);
        outOfStockText.append("Out of stock:\n\n");
        contractCaption.setColor(GRAY);
        outOfStockCaption.setColor(GRAY);

        if (material==null) return;

        int materialID = material.getID();
        headerText.append(material.getName());

        int dailyPush = inventory.getDailyPush(materialID);
        int dailyPoll = inventory.getDailyPoll(materialID);
        int dailyBalance = inventory.getDailyBalance(materialID);
        if (dailyBalance > 0) balanceText.append('+');
        balanceText.append(dailyBalance);
        balanceText.append("\n\n");
        balanceText.append("IN:");
        balanceText.append(dailyPush);
        balanceText.append(" / OUT:");
        balanceText.append(dailyPoll);


        int nonPerformance = inventory.getDailyNonPerformance(materialID);
        contractText.append(nonPerformance);
        contractCaption.setColor( nonPerformance==0 ? OK_COLOR : EXCLAMATION_COLOR);

        int outOfStock = inventory.getDailyOutOfStock(materialID);
        outOfStockText.append(outOfStock);
        outOfStockCaption.setColor( outOfStock==0 ? OK_COLOR : EXCLAMATION_COLOR);

    }

}
