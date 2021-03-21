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
import com.axiom.operatio.model.market.Market;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.Production;

public class MarketPanel extends Panel {

    public static final int GRAPH_BACKGROUND = 0x80000000;
    public static final int PANEL_COLOR = 0xCC505050;
    public static final int BUY_COLOR = 0xFF9d3e4d;
    public static final int SELL_COLOR = 0xFF80B380;

    private Production production;
    private Inventory inventory;

    private CheckBox autoBuyCB, autoSellCB;
    private Caption caption, materialCaption;
    private Market market;
    private final double[] values;
    private double maxValue;
    private int counter = 0;
    private String commodityName = "";
    private int currentCommodity = 0;
    private int previousCommodity = -1;
    private int quantity = 20;
    private MaterialsPanel materialsPanel;
    private Button sellButton, dealSum, buyButton;
    private Button leftButton, quantityButton, rightButton;

    private final int tickSound, denySound;


    public MarketPanel(MaterialsPanel materialsPanel, Market market, Production production, Inventory inventory) {
        super();
        this.market = market;
        this.production = production;
        this.inventory = inventory;
        this.materialsPanel = materialsPanel;

        values = new double[Market.HISTORY_LENGTH];
        counter = 0;
        commodityName = Material.getMaterial(currentCommodity).getName();
        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);
        denySound = SoundRenderer.loadSound(R.raw.deny_snd);;

        buildUI();

    }


    private void buildUI() {
        setLocalBounds(874, 50, 1022, 880);
        setColor(PANEL_COLOR);


        caption = new Caption("Market");
        caption.setTextScale(1.5f);
        caption.setTextColor(Color.WHITE);
        caption.setLocalBounds(30, 780, 300, 100);
        addChild(caption);

        materialCaption = new Caption("Material price");
        materialCaption.setTextScale(1.3f);
        materialCaption.setTextColor(Color.WHITE);
        materialCaption.setLocalBounds(50, 480, 300, 100);
        addChild(materialCaption);

        buyButton = buildButton("BUY", 25, 365, 150, 80, BUY_COLOR, 1.5f,true);
        sellButton = buildButton("SELL", 800, 365, 180, 80, SELL_COLOR, 1.5f, true);

        leftButton = buildButton("<", 200, 365, 75, 80, Color.GRAY, 1.5f,true);
        quantityButton = buildButton("" + quantity, 275, 365, 150, 80, Color.BLACK, 1.5f, false);
        rightButton = buildButton(">",425, 365, 75, 80,  Color.GRAY, 1.5f,true);

        String sumText = FormatUtils.formatMoney(production.getLedger().getCashBalance());
        dealSum = buildButton(sumText, 525, 365, 250, 80, Color.BLACK, 1.5f,false);

        autoBuyCB = buildCheckBox("Auto-buy", 550, 805, 200, 100);
        autoSellCB = buildCheckBox("Auto-sell", 800, 805, 200, 100);
    }


    private Button buildButton(String txt, float x, float y, float w, float h, int back, float textScale, boolean listener) {
        Button button = new Button(txt);
        button.setTag(txt);
        button.setLocalBounds(x, y, w, h);
        button.setColor(back);
        button.setTextScale(textScale);
        button.setTextColor(Color.WHITE);
        if (listener) button.setClickListener(clickListener);
        addChild(button);
        return button;
    }


    private CheckBox buildCheckBox(String txt, float x, float y, float w, float h) {
        CheckBox cb = new CheckBox(txt, false);
        cb.setTag(txt);
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
            if (material!=null) currentCommodity = material.getID(); else currentCommodity = 0;
            if (currentCommodity!=previousCommodity) {
                commodityName = Material.getMaterial(currentCommodity).getName();
                autoBuyCB.setChecked(inventory.isAutoBuy(currentCommodity));
                autoSellCB.setChecked(inventory.isAutoSell(currentCommodity));
                previousCommodity = currentCommodity;
            }
            maxValue = market.getHistoryMaxValue(currentCommodity);
            counter = market.getHistoryLength(currentCommodity);
            market.getHistoryValues(currentCommodity, values);
            dealSum.setText(FormatUtils.formatMoney(quantity * market.getValue(currentCommodity)));
            materialCaption.setText(commodityName + " - " + FormatUtils.formatMoney(market.getValue(currentCommodity)));
        }
    }


    @Override
    public void draw(Camera camera) {
        super.draw(camera);

        AABB wBounds = getWorldBounds();

        GraphicsRender.setZOrder(zOrder + 1);

        synchronized (values) {
            float graphWidth = 960;
            float graphHeight = 300;
            float floor = 480;
            float x = wBounds.min.x + 25;
            float y = wBounds.min.y + floor;
            float oldX = x;
            float oldY;
            GraphicsRender.setColor(GRAPH_BACKGROUND);
            GraphicsRender.setZOrder(zOrder + 1);
            GraphicsRender.drawRectangle(x, y, graphWidth,  graphHeight);
            GraphicsRender.setZOrder(zOrder + 2);
            float faceValueY = (float) (y + market.getFaceValue(currentCommodity) / maxValue * graphHeight * 0.8f);
            GraphicsRender.setColor(Color.DKGRAY);
            GraphicsRender.drawLine(x, faceValueY, x + graphWidth, faceValueY);
            GraphicsRender.setZOrder(zOrder + 3);
            y += (int) (values[0] / maxValue * graphHeight * 0.8f);
            oldY = y;
            for (int i = 0; i < counter; i++) {
                x = wBounds.min.x + i * 10 + 25;
                y = wBounds.min.y + floor + (int) (values[i] / maxValue * graphHeight * 0.8f);
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
            if (w.getTag().equals("<")) {
                quantity--;
                if (quantity < 1) {
                    quantity = 1;
                    SoundRenderer.playSound(denySound);
                } else {
                    SoundRenderer.playSound(tickSound);
                }
                quantityButton.setText("" + quantity);

            } else if (w.getTag().equals(">")) {
                quantity++;
                if (quantity > 100) {
                    quantity = 100;
                    SoundRenderer.playSound(denySound);
                } else {
                    SoundRenderer.playSound(tickSound);
                }
                quantityButton.setText("" + quantity);
            } else if (w.getTag().equals("BUY")) {
                market.buyOrder(inventory, currentCommodity, quantity);
                materialsPanel.updateData();
                SoundRenderer.playSound(tickSound);
            } else if (w.getTag().equals("SELL")) {
                market.sellOrder(inventory, currentCommodity, quantity);
                materialsPanel.updateData();
                SoundRenderer.playSound(tickSound);
            } else if (w.getTag().equals("Auto-buy")) {
                inventory.setAutoBuy(currentCommodity, autoBuyCB.isChecked());
                autoSellCB.setChecked(false);
                inventory.setAutoSell(currentCommodity, autoSellCB.isChecked());
            } else if (w.getTag().equals("Auto-sell")) {
                inventory.setAutoSell(currentCommodity, autoSellCB.isChecked());
                autoBuyCB.setChecked(false);
                inventory.setAutoBuy(currentCommodity, autoBuyCB.isChecked());
            }
        }

    };


}
