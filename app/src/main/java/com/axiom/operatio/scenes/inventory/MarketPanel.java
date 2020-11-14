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
import com.axiom.operatio.model.inventory.Inventory;
import com.axiom.operatio.model.market.Market;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.Production;

import static android.graphics.Color.GRAY;
import static android.graphics.Color.WHITE;

public class MarketPanel extends Panel {

    public static final String MONEY_FORMAT_0F = "$%.0f";
    public static final String MONEY_FORMAT_2F = "$%.2f";

    private Production production;
    private Inventory inventory;
    private Button cashBalance;
    private CheckBox autoBuyCB, autoSellCB;

    private Caption caption;
    private Market market;
    private final double[] values;
    private double maxValue;
    private int counter = 0;
    private String commodityName = "";
    private int currentCommodity = 0;
    private int previousCommodity = 0;
    private int quantity = 20;
    private MaterialsPanel materialsPanel;
    private Button sellButton, dealSum, buyButton;
    private Button leftButton, quantityButton, rightButton;

    private final int cashSound, tickSound, denySound;

    protected ClickListener clickListener = new ClickListener() {
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
                cashBalance.setText(String.format("$%,d", (long) production.getCashBalance()));
                materialsPanel.updateData();
                SoundRenderer.playSound(tickSound);
              //  SoundRenderer.playSound(cashSound);
            } else if (w.getTag().equals("SELL")) {
                market.sellOrder(inventory, currentCommodity, quantity);
                cashBalance.setText(String.format("$%,d", (long) production.getCashBalance()));
                materialsPanel.updateData();
                SoundRenderer.playSound(tickSound);
              //  SoundRenderer.playSound(cashSound);
            } else if (w.getTag().equals("Auto-buy")) {
                inventory.setAutoBuy(currentCommodity, autoBuyCB.isChecked());
                autoSellCB.setChecked(false);
            } else if (w.getTag().equals("Auto-sell")) {
                inventory.setAutoSell(currentCommodity, autoSellCB.isChecked());
                autoBuyCB.setChecked(false);
            }
        }

    };


    public MarketPanel(Button cashBalance, MaterialsPanel materialsPanel, Market market, Production production, Inventory inventory) {
        super();
        this.market = market;
        this.production = production;
        this.inventory = inventory;
        this.materialsPanel = materialsPanel;
        this.cashBalance = cashBalance;
        values = new double[Market.HISTORY_LENGTH];
        counter = 0;
        commodityName = Material.getMaterial(currentCommodity).getName();
        cashSound = SoundRenderer.loadSound(R.raw.cash_snd);
        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);
        denySound = SoundRenderer.loadSound(R.raw.deny_snd);;

        setLocalBounds(900, 390, 1000, 550);
        setColor(0xCC505050);
        caption = new Caption("Market");
        caption.setTextScale(1.5f);
        caption.setTextColor(WHITE);
        caption.setLocalBounds(30, 450, 300, 100);
        addChild(caption);

        buyButton = new Button("BUY");
        buyButton.setTag("BUY");
        buyButton.setLocalBounds(25, 35, 150, 80);
        buyButton.setTextScale(1.5f);
        buyButton.setTextColor(WHITE);
        buyButton.setColor(Color.RED);
        buyButton.setClickListener(clickListener);
        addChild(buyButton);


        leftButton = new Button("<");
        leftButton.setLocalBounds( 200, 35, 75, 80);
        leftButton.setTag("<");
        leftButton.setColor(GRAY);
        leftButton.setTextColor(WHITE);
        leftButton.setClickListener(clickListener);
        addChild(leftButton);

        quantityButton = new Button("" + quantity);
        quantityButton.setTextScale(1.5f);
        quantityButton.setLocalBounds( 275, 35, 150, 80);
        quantityButton.setColor(Color.BLACK);
        quantityButton.setTextColor(WHITE);
        addChild(quantityButton);

        rightButton = new Button(">");
        rightButton.setTag(">");
        rightButton.setLocalBounds( 425, 35, 75, 80);
        rightButton.setColor(GRAY);
        rightButton.setTextColor(WHITE);
        rightButton.setClickListener(clickListener);
        addChild(rightButton);

        dealSum = new Button(String.format(MONEY_FORMAT_2F, production.getCashBalance()));
        dealSum.setTextScale(1.5f);
        dealSum.setLocalBounds( 525, 35, 250, 80);
        dealSum.setColor(Color.BLACK);
        dealSum.setTextColor(WHITE);
        addChild(dealSum);

        sellButton = new Button("SELL");
        sellButton.setTag("SELL");
        sellButton.setLocalBounds(800, 35, 150, 80);
        sellButton.setTextScale(1.5f);
        sellButton.setTextColor(WHITE);
        sellButton.setColor(Color.GREEN);
        sellButton.setClickListener(clickListener);
        addChild(sellButton);


        autoBuyCB = new CheckBox("Auto-buy", false);
        autoBuyCB.setTag("Auto-buy");
        autoBuyCB.setLocalBounds(550, 475, 200, 100);
        autoBuyCB.setTextColor(WHITE);
        autoBuyCB.setTextScale(1.5f);
        autoBuyCB.setClickListener(clickListener);
        addChild(autoBuyCB);

        autoSellCB = new CheckBox("Auto-sell", false);
        autoSellCB.setTag("Auto-sell");
        autoSellCB.setLocalBounds(800, 475, 200, 100);
        autoSellCB.setTextColor(WHITE);
        autoSellCB.setTextScale(1.5f);
        autoSellCB.setClickListener(clickListener);
        addChild(autoSellCB);
    }


    public void updateValues() {
        synchronized (values) {
            Material material = null;
            if (materialsPanel!=null) material = materialsPanel.getSelectedMaterial();
            if (material!=null) currentCommodity = material.getMaterialID(); else currentCommodity = 0;
            if (currentCommodity!=previousCommodity) {
                commodityName = Material.getMaterial(currentCommodity).getName();
                autoBuyCB.setChecked(inventory.getAutoBuy(currentCommodity));
                autoSellCB.setChecked(inventory.getAutoSell(currentCommodity));
                previousCommodity = currentCommodity;
            }
            maxValue = market.getHistoryMaxValue(currentCommodity);
            counter = market.getHistoryLength(currentCommodity);
            market.getHistoryValues(currentCommodity, values);
            dealSum.setText(String.format(MONEY_FORMAT_2F, quantity * market.getValue(currentCommodity)));
        }
    }


    @Override
    public void draw(Camera camera) {
        super.draw(camera);

        AABB wBounds = getWorldBounds();
        AABB scissor = getScissors();

        caption.setText(commodityName + " - " + String.format(MONEY_FORMAT_2F, market.getValue(currentCommodity)));
        GraphicsRender.setZOrder(zOrder + 1);

        synchronized (values) {
            float graphWidth = 960;
            float graphHeight = 300;
            float floor = 150;
            float x = wBounds.min.x + 25;
            float y = wBounds.min.y + floor;
            float oldX = x;
            float oldY;
            GraphicsRender.setColor(Color.BLACK);
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

}
