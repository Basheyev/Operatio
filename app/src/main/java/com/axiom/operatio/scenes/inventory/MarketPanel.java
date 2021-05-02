package com.axiom.operatio.scenes.inventory;

import android.graphics.Color;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Caption;
import com.axiom.atom.engine.ui.widgets.CheckBox;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.common.FormatUtils;
import com.axiom.operatio.model.inventory.Inventory;
import com.axiom.operatio.model.inventory.Market;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.Production;

/**
 * Панель отображения рынка
 */
public class MarketPanel extends Panel {

    public static final String MARKET = "Market";
    public static final String PURCHASE_CONTRACT = "Sign purchase contract (daily acceptance)";
    public static final String SALES_CONTRACT = "Sign sales contract (daily shipment)";
    public static final String BUY = "BUY";
    public static final String SELL = "SELL";

    public static final int NOT_SELECTED = -1;

    public static final int GRAPH_BACKGROUND = 0x80000000;
    public static final int PANEL_COLOR = 0xCC505050;
    public static final int BUY_COLOR = 0xFF9d3e4d;
    public static final int SELL_COLOR = 0xFF80B380;

    private InventoryScene scene;
    private Production production;
    private Inventory inventory;

    private CheckBox autoBuyCB, autoSellCB;
    private Caption caption, materialCaption;
    private Market market;
    private final double[] values;
    private double maxValue;
    private int counter = 0;
    private float graphBottomY;
    private String commodityName = "";
    private int currentCommodity = 0;
    private int previousCommodity = -1;
    private int quantity = 20;
    private MaterialsPanel materialsPanel;
    private Button sellButton, dealSum, buyButton;
    private Button leftButton, quantityButton, rightButton;

    private StringBuffer sumText, materialText;

    private final int tickSound, denySound;


    public MarketPanel(InventoryScene scene) {
        super();
        this.scene = scene;
        this.market = scene.getMarket();
        this.production = scene.getProduction();
        this.inventory = scene.getInventory();
        this.materialsPanel = scene.getMaterialsPanel();

        values = new double[Market.HISTORY_LENGTH];
        counter = 0;
        commodityName = Material.getMaterial(currentCommodity).getName();

        sumText = new StringBuffer(32);
        materialText = new StringBuffer(128);

        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);
        denySound = SoundRenderer.loadSound(R.raw.deny_snd);;

        buildUI();

    }


    private void buildUI() {
        setLocalBounds(874, 370, 1022, 560);
        setColor(PANEL_COLOR);


        caption = new Caption(MARKET);
        caption.setTextScale(1.5f);
        caption.setTextColor(Color.WHITE);
        caption.setLocalBounds(30, 460, 300, 100);
        addChild(caption);

        materialCaption = new Caption("Material price");
        materialCaption.setTextScale(1.3f);
        materialCaption.setTextColor(Color.WHITE);
        materialCaption.setLocalBounds(50, 160, 300, 100);
        addChild(materialCaption);

        buyButton = buildButton(BUY, 25, 45, 150, 80, BUY_COLOR, 1.5f,true);
        sellButton = buildButton(SELL, 800, 45, 180, 80, SELL_COLOR, 1.5f, true);

        leftButton = buildButton("<", 200, 45, 75, 80, Color.GRAY, 1.5f,true);
        quantityButton = buildButton("" + quantity, 275, 45, 150, 80, Color.BLACK, 1.5f, false);
        rightButton = buildButton(">",425, 45, 75, 80,  Color.GRAY, 1.5f,true);

        sumText = FormatUtils.formatMoney(production.getLedger().getCashBalance(), sumText);
        dealSum = buildButton(sumText, 525, 45, 250, 80, Color.BLACK, 1.5f,false);

        autoBuyCB = buildCheckBox(PURCHASE_CONTRACT, 160, 485, 250, 100);
        autoSellCB = buildCheckBox(SALES_CONTRACT, 160, 485, 250, 100);

        graphBottomY = 160;
    }


    private Button buildButton(CharSequence txt, float x, float y, float w, float h, int back, float textScale, boolean listener) {
        Button button = new Button(txt);
        button.setTag(txt.toString());
        button.setLocalBounds(x, y, w, h);
        button.setColor(back);
        button.setTextScale(textScale);
        button.setTextColor(Color.WHITE);
        if (listener) button.setClickListener(clickListener);
        addChild(button);
        return button;
    }


    private CheckBox buildCheckBox(CharSequence txt, float x, float y, float w, float h) {
        CheckBox cb = new CheckBox(txt, false);
        cb.setTag(txt.toString());
        cb.setLocalBounds(x,y,w,h);
        cb.setTextColor(Color.WHITE);
        cb.setTextScale(1.5f);
        cb.setClickListener(clickListener);
        addChild(cb);
        return cb;
    }


    public void updateValues() {
        synchronized (values) {
            Material material = null;
            if (materialsPanel!=null) material = materialsPanel.getSelectedMaterial();
            if (material!=null) {
                currentCommodity = material.getID();
            } else {
                currentCommodity = NOT_SELECTED;
                autoSellCB.setVisible(false);
                autoBuyCB.setVisible(false);
                quantityButton.setText("");
                dealSum.setText("");
                materialText.setLength(0);
                return;
            }
            if (currentCommodity != previousCommodity) {
                Material currentMaterial = Material.getMaterial(currentCommodity);
                commodityName = currentMaterial!=null ? currentMaterial.getName() : "";
                autoBuyCB.setChecked(inventory.hasPurchaseContract(currentCommodity));
                autoSellCB.setChecked(inventory.hasSalesContract(currentCommodity));
                quantity = inventory.getContractQuantity(currentCommodity);
                quantityButton.setText("" + quantity);
                previousCommodity = currentCommodity;
            }

            if (currentCommodity < 8) {
                autoBuyCB.setVisible(true);
                autoSellCB.setVisible(false);
            } else {
                autoBuyCB.setVisible(false);
                autoSellCB.setVisible(true);
            }

            maxValue = market.getHistoryMaxValue(currentCommodity);
            counter = market.getHistoryLength(currentCommodity);
            market.getHistoryValues(currentCommodity, values);
            dealSum.setText(FormatUtils.formatMoney(quantity * market.getValue(currentCommodity), sumText));

            materialText.setLength(0);
            materialText.append(commodityName).append(" - ");
            FormatUtils.formatMoneyAppend(market.getValue(currentCommodity), materialText);
            materialCaption.setText(materialText);



        }
    }


    @Override
    public void draw(Camera camera) {
        super.draw(camera);

        AABB wBounds = getWorldBounds();

        GraphicsRender.setZOrder(zOrder + 1);
        float graphWidth = 960;
        float graphHeight = 300;
        float x = wBounds.minX + 25;
        float y = wBounds.minY + graphBottomY;
        float oldX = x;
        float oldY;
        GraphicsRender.setColor(GRAPH_BACKGROUND);
        GraphicsRender.drawRectangle(x, y, graphWidth,  graphHeight);

        int matID = currentCommodity;
        if (matID != NOT_SELECTED)
        synchronized (values) {
            GraphicsRender.setZOrder(zOrder + 2);
            float faceValueY = (float) (y + market.getFaceValue(matID) / maxValue * graphHeight * 0.8f);
            GraphicsRender.setColor(Color.DKGRAY);
            GraphicsRender.drawLine(x, faceValueY, x + graphWidth, faceValueY);
            GraphicsRender.setZOrder(zOrder + 3);
            y += (int) (values[0] / maxValue * graphHeight * 0.8f);
            oldY = y;
            for (int i = 0; i < counter; i++) {
                x = wBounds.minX + i * 10 + 25;
                y = wBounds.minY + graphBottomY + (int) (values[i] / maxValue * graphHeight * 0.8f);
                if (oldY > y) GraphicsRender.setColor(Color.RED);
                else GraphicsRender.setColor(Color.GREEN);
                GraphicsRender.drawLine(oldX, oldY, x, y);
                oldX = x;
                oldY = y;
            }
        }

    }



    private ClickListener clickListener = new ClickListener() {
        @Override
        public void onClick(Widget w) {
            if (materialsPanel.getSelectedMaterial()==null) return;
            if (w.getTag().equals("<")) {
                quantity--;
                if (quantity < 1) {
                    quantity = 1;
                    SoundRenderer.playSound(denySound);
                } else {
                    SoundRenderer.playSound(tickSound);
                }
                quantityButton.setText("" + quantity);
                inventory.setContractQuantity(currentCommodity, quantity);
            } else if (w.getTag().equals(">")) {
                quantity++;
                if (quantity > 2000) {
                    quantity = 2000;
                    SoundRenderer.playSound(denySound);
                } else {
                    SoundRenderer.playSound(tickSound);
                }
                quantityButton.setText("" + quantity);
                inventory.setContractQuantity(currentCommodity, quantity);
            } else if (w.getTag().equals(BUY)) {
                market.buyOrder(inventory, currentCommodity, quantity);
                materialsPanel.updateData();
                SoundRenderer.playSound(tickSound);
            } else if (w.getTag().equals(SELL)) {
                market.sellOrder(inventory, currentCommodity, quantity);
                materialsPanel.updateData();
                SoundRenderer.playSound(tickSound);
            } else if (w.getTag().equals(PURCHASE_CONTRACT)) {
                inventory.signPurchaseContract(currentCommodity, autoBuyCB.isChecked());
                autoSellCB.setChecked(false);
                inventory.signSalesContract(currentCommodity, autoSellCB.isChecked());
            } else if (w.getTag().equals(SALES_CONTRACT)) {
                inventory.signSalesContract(currentCommodity, autoSellCB.isChecked());
                autoBuyCB.setChecked(false);
                inventory.signPurchaseContract(currentCommodity, autoBuyCB.isChecked());
            }
        }

    };


}
