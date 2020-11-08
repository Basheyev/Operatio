package com.axiom.operatio.scenes.inventory;

import android.graphics.Color;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Caption;
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

    protected ClickListener clickListener = new ClickListener() {
        @Override
        public void onClick(Widget w) {

            Button button = (Button) w;

            if (button.getTag().equals("<")) {
                quantity--;
                if (quantity < 1) quantity = 1;
                quantityButton.setText("" + quantity);
            } else if (button.getTag().equals(">")) {
                quantity++;
                if (quantity > 100) quantity = 100;
                quantityButton.setText("" + quantity);

            } else if (button.getTag().equals("BUY")) {
                market.buyOrder(inventory, currentCommodity, quantity);
                cashBalance.setText(String.format(MONEY_FORMAT_0F, production.getCashBalance()));
                materialsPanel.updateData();
            } else if (button.getTag().equals("SELL")) {
                market.sellOrder(inventory, currentCommodity, quantity);
                cashBalance.setText(String.format(MONEY_FORMAT_0F, production.getCashBalance()));
                materialsPanel.updateData();
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

        setLocalBounds(900, 430, 1000, 550);
        setColor(0xCC505050);
        caption = new Caption("Market");
        caption.setTextScale(1.5f);
        caption.setTextColor(WHITE);
        caption.setLocalBounds(30, getHeight() - 100, 300, 100);
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
    }


    public void updateValues() {
        synchronized (values) {
            Material material = null;
            if (materialsPanel!=null) material = materialsPanel.getSelectedMaterial();
            if (material!=null) currentCommodity = material.getMaterialID(); else currentCommodity = 0;
            if (currentCommodity!=previousCommodity) {
                commodityName = Material.getMaterial(currentCommodity).getName();
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

        caption.setText(commodityName + " price " + String.format(MONEY_FORMAT_2F, market.getValue(currentCommodity)));
        GraphicsRender.setZOrder(zOrder + 1);

        synchronized (values) {
            float floor = 200;
            float x = wBounds.min.x + 25;
            float y = wBounds.min.y + floor;
            float oldX = x;
            float oldY;
            GraphicsRender.setColor(Color.GREEN);
            GraphicsRender.drawLine(x, y, x + values.length * 10, y);
            y += (int) (values[0] / maxValue * 200);
            oldY = y;
            for (int i = 0; i < counter; i++) {
                x = wBounds.min.x + i * 10 + 25;
                y = wBounds.min.y + floor + (int) (values[i] / maxValue * 200);
                if (oldY > y) GraphicsRender.setColor(Color.RED);
                else GraphicsRender.setColor(Color.GREEN);
                GraphicsRender.drawLine(oldX, oldY, x, y);
                oldX = x;
                oldY = y;
            }
        }

    }

}
