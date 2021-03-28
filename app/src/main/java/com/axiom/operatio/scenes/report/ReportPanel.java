package com.axiom.operatio.scenes.report;

import android.graphics.Color;

import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Text;
import com.axiom.atom.engine.ui.widgets.Caption;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.operatio.model.gameplay.MissionManager;
import com.axiom.operatio.model.gameplay.GameMission;
import com.axiom.operatio.model.ledger.Ledger;
import com.axiom.operatio.model.common.FormatUtils;
import com.axiom.operatio.model.inventory.Inventory;
import com.axiom.operatio.model.ledger.LedgerPeriod;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.scenes.common.ItemWidget;

import static android.graphics.Color.GREEN;
import static android.graphics.Color.WHITE;

public class ReportPanel extends Panel {

    public static final int ITEM_BACKGROUND = 0x80000000;

    private Production production;
    private Caption panelCaption;
    private Caption salesCaption;
    private Caption manufacturedCaption;
    private Caption purchaseCaption;
    private Caption reportCaption;
    private LineChart chart;
    private ItemWidget[] boughtMaterials;
    private ItemWidget[] manufacturedMaterials;
    private ItemWidget[] soldMaterials;
    private StringBuffer summary;

    private double[] revenueData = new double[Ledger.HISTORY_LENGTH];
    private double[] expensesData = new double[Ledger.HISTORY_LENGTH];

    private StringBuffer salesText;
    private StringBuffer purchaseText;
    private StringBuffer manufacturedText;


    public ReportPanel(Production production) {
        super();
        this.production = production;

        setLocalBounds(24,50, 1872, 880);
        setColor(0xCC505050);

        summary = new StringBuffer(512);
        salesText = new StringBuffer(32);
        purchaseText = new StringBuffer(32);
        manufacturedText = new StringBuffer(32);

        panelCaption = new Caption("Operations daily report");
        panelCaption.setTextScale(1.7f);
        panelCaption.setTextColor(WHITE);
        panelCaption.setLocalBounds(30, getHeight() - 100, 350, 100);
        addChild(panelCaption);

        purchaseCaption = new Caption("Purchase");
        purchaseCaption.setTextColor(1,0.5f,0.5f, 1);
        purchaseCaption.setTextScale(1.3f);
        purchaseCaption.setLocalBounds(30, 330,300, 100);
        addChild(purchaseCaption);
        boughtMaterials = new ItemWidget[32];
        float bx = 30, by = 270;
        for (int i=0; i<32; i++) {
            boughtMaterials[i] = new ItemWidget("");
            boughtMaterials[i].setLocalBounds(bx, by, 64, 64);
            boughtMaterials[i].setColor(ITEM_BACKGROUND);
            boughtMaterials[i].setTextColor(WHITE);
            boughtMaterials[i].setTextScale(0.9f);
            addChild(boughtMaterials[i]);
            bx += 70;
            if (bx > 570) {
                bx = 30;
                by -= 70;
            }
        }

        manufacturedCaption = new Caption("Manufactured");
        manufacturedCaption.setTextColor(WHITE);
        manufacturedCaption.setTextScale(1.3f);
        manufacturedCaption.setLocalBounds(650, 330,300, 100);
        addChild(manufacturedCaption);
        manufacturedMaterials = new ItemWidget[32];
        float mx = 650, my = 270;
        for (int i=0; i<32; i++) {
            manufacturedMaterials[i] = new ItemWidget("");
            manufacturedMaterials[i].setLocalBounds(mx, my, 64, 64);
            manufacturedMaterials[i].setColor(ITEM_BACKGROUND);
            manufacturedMaterials[i].setTextColor(WHITE);
            manufacturedMaterials[i].setTextScale(0.9f);
            addChild(manufacturedMaterials[i]);
            mx += 70;
            if (mx > 1150) {
                mx = 650;
                my -= 70;
            }
        }

        salesCaption = new Caption("Sales");
        salesCaption.setTextColor(GREEN);
        salesCaption.setTextScale(1.3f);
        salesCaption.setLocalBounds(1270, 330, 300, 100);
        addChild(salesCaption);
        soldMaterials = new ItemWidget[32];
        float sx = 1270, sy = 270;
        for (int i=0; i<32; i++) {
            soldMaterials[i] = new ItemWidget("");
            soldMaterials[i].setLocalBounds(sx, sy, 64, 64);
            soldMaterials[i].setColor(ITEM_BACKGROUND);
            soldMaterials[i].setTextColor(WHITE);
            soldMaterials[i].setTextScale(0.9f);
            addChild(soldMaterials[i]);
            sx += 70;
            if (sx > 1770) {
                sx = 1270;
                sy -= 70;
            }
        }


        reportCaption = new Caption("Cashflow");
        reportCaption.setTextColor(WHITE);
        reportCaption.setTextScale(1.3f);
        reportCaption.setLocalBounds(1400, 435, 400, 350);
        reportCaption.setVerticalAlignment(Text.ALIGN_TOP);
        addChild(reportCaption);


        chart = new LineChart(2);
        chart.setLocalBounds(25,435, 1350, 350);
        Ledger ledger = production.getLedger();
        loadLedgerDataToChart(ledger);
        addChild(chart);

    }



    private void loadLedgerDataToChart(Ledger ledger) {
        LedgerPeriod[] history = ledger.getHistory();
        for (int i=0; i<ledger.getHistoryCounter(); i++) {
            revenueData[i] = history[i].getRevenue();
            expensesData[i] = history[i].getExpenses();
        }
        chart.updateData(0, revenueData, ledger.getHistoryCounter(), GREEN);
        chart.updateData(1, expensesData, ledger.getHistoryCounter(), Color.RED);
    }


    /**
     * Обновляет данные
     */
    public void updateData() {
        Ledger ledger = production.getLedger();

        salesText.setLength(0);
        salesText.append("Sold: ");
        double salesSum = Math.round(ledger.getLastPeriod().getRevenue());
        FormatUtils.formatMoneyAppend(salesSum, salesText);

        manufacturedText.setLength(0);
        manufacturedText.append("Manufacture costs: ");
        double manufactureCost = Math.round(ledger.getLastPeriod().getMaintenanceCost());
        FormatUtils.formatMoneyAppend(manufactureCost, manufacturedText);

        purchaseText.setLength(0);
        purchaseText.append("Purchased: ");
        double purchaseSum = Math.round(ledger.getLastPeriod().getExpenses() - manufactureCost);
        FormatUtils.formatMoneyAppend(purchaseSum, purchaseText);

        synchronized (this) {

            // Обновить график
            loadLedgerDataToChart(ledger);

            // Очистить все ячейки отображения материалов
            for (int i = 0; i < manufacturedMaterials.length; i++) {
                boughtMaterials[i].setActive(false);
                manufacturedMaterials[i].setActive(false);
                soldMaterials[i].setActive(false);
            }

            int manufacturedCounter = 0;
            int soldCounter = 0;
            int boughtCounter = 0;
            for (int i = 0; i < Inventory.SKU_COUNT; i++) {

                // Вывести объем закупа
                int boughtAmount = ledger.getMaterialRecord(i).getBoughtAmountByPeriod();
                if (boughtAmount > 0 && boughtCounter < boughtMaterials.length) {
                    Material material = Material.getMaterial(i);
                    boughtMaterials[boughtCounter].setBackground(material.getImage());
                    boughtMaterials[boughtCounter].setText("" + boughtAmount);
                    boughtMaterials[boughtCounter].setActive(true);
                    boughtCounter++;
                }

                // Вывести продуктивность производства
                int manufacturedAmount = ledger.getMaterialRecord(i).getProductivity();
                if (manufacturedAmount > 0 && manufacturedCounter < manufacturedMaterials.length) {
                    Material material = Material.getMaterial(i);
                    manufacturedMaterials[manufacturedCounter].setBackground(material.getImage());
                    manufacturedMaterials[manufacturedCounter].setText("" + manufacturedAmount);
                    manufacturedMaterials[manufacturedCounter].setActive(true);
                    manufacturedCounter++;
                }

                // Вывести объем продаж
                int soldAmount = ledger.getMaterialRecord(i).getSoldAmountByPeriod();
                if (soldAmount > 0 && soldCounter < soldMaterials.length) {
                    Material material = Material.getMaterial(i);
                    soldMaterials[soldCounter].setBackground(material.getImage());
                    soldMaterials[soldCounter].setText("" + soldAmount);
                    soldMaterials[soldCounter].setActive(true);
                    soldCounter++;
                }

            }

            updateSummary(ledger);
            salesCaption.setText(salesText);
            manufacturedCaption.setText(manufacturedText);
            purchaseCaption.setText(purchaseText);
            reportCaption.setText(summary);

            GameMission mission =  MissionManager.getMission(production.getLevel());
            String goal = mission.getName() + " #" + mission.getID() + " - " + mission.getDescription();
            panelCaption.setText(goal);

        }
    }


    private void updateSummary(Ledger ledger) {
        double totalRevenue = ledger.getTotal().getRevenue();
        double margin = 0;
        if (totalRevenue > 0) margin = Math.round(ledger.getTotal().getMargin() / totalRevenue * 100);
        summary.delete(0, summary.length());
        summary.append("Operational:\n");
        summary.append("- income: ");
        FormatUtils.formatMoneyAppend(Math.round(ledger.getLastPeriod().getRevenue()), summary);
        summary.append("\n- expenses: ");
        FormatUtils.formatMoneyAppend(Math.round(ledger.getLastPeriod().getExpenses()),summary);
        summary.append("\n- margin: ");
        FormatUtils.formatMoneyAppend(Math.round(ledger.getLastPeriod().getMargin()),summary);
        summary.append("\n\nTotal balance: ");
        FormatUtils.formatMoneyAppend(Math.round(ledger.getTotal().getMargin()), summary);
        summary.append(" (");
        summary.append(margin);
        summary.append("%)");
        summary.append("\nAssets: ");
        FormatUtils.formatMoneyAppend(Math.round(production.getAssetsValuation()), summary);
        summary.append("\nWork in progress: ");
        FormatUtils.formatMoneyAppend(Math.round(production.getWorkInProgressValuation()),summary);
        summary.append("\nInventory: ");
        FormatUtils.formatMoneyAppend(Math.round(production.getInventory().getValuation()), summary);
        summary.append("\n\nCapitalization: ");
        FormatUtils.formatMoneyAppend(Math.round(ledger.getCapitalization()), summary);
    }


    @Override
    public void draw(Camera camera) {
        synchronized (this) {
            super.draw(camera);
        }
    }
}
