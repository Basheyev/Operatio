package com.axiom.operatio.scenes.report;

import android.graphics.Color;

import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Text;
import com.axiom.atom.engine.ui.widgets.Caption;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.operatio.model.gameplay.GameManager;
import com.axiom.operatio.model.gameplay.GameMission;
import com.axiom.operatio.model.gameplay.Ledger;
import com.axiom.operatio.model.common.FormatUtils;
import com.axiom.operatio.model.inventory.Inventory;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.scenes.common.ItemWidget;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.GREEN;
import static android.graphics.Color.WHITE;

public class ReportPanel extends Panel {

    private Production production;
    private Caption panelCaption;
    private Caption incomeCaption;
    private Caption expenseCaption;
    private Caption reportCaption;
    private LineChart chart;
    private ItemWidget[] boughtMaterials;
    private ItemWidget[] manufacturedMaterials;
    private ItemWidget[] soldMaterials;
    private StringBuffer summary;


    public ReportPanel(Production production) {
        super();
        this.production = production;

        setLocalBounds(24,60, 1872, 880);
        setColor(0xCC505050);

        summary = new StringBuffer(512);

        panelCaption = new Caption("Operations daily report");
        panelCaption.setTextScale(1.7f);
        panelCaption.setTextColor(WHITE);
        panelCaption.setLocalBounds(30, getHeight() - 100, 350, 100);
        addChild(panelCaption);

        expenseCaption = new Caption("Purchase");
        expenseCaption.setTextColor(1,0.5f,0.5f, 1);
        expenseCaption.setTextScale(1.3f);
        expenseCaption.setLocalBounds(30, 330,300, 100);
        addChild(expenseCaption);
        boughtMaterials = new ItemWidget[32];
        float bx = 30, by = 270;
        for (int i=0; i<32; i++) {
            boughtMaterials[i] = new ItemWidget("");
            boughtMaterials[i].setLocalBounds(bx, by, 64, 64);
            boughtMaterials[i].setColor(BLACK);
            boughtMaterials[i].setTextColor(WHITE);
            boughtMaterials[i].setTextScale(0.9f);
            addChild(boughtMaterials[i]);
            bx += 70;
            if (bx > 570) {
                bx = 30;
                by -= 70;
            }
        }

        Caption manufCaption = new Caption("Manufactured");
        manufCaption.setTextColor(WHITE);
        manufCaption.setTextScale(1.3f);
        manufCaption.setLocalBounds(650, 330,300, 100);
        addChild(manufCaption);
        manufacturedMaterials = new ItemWidget[32];
        float mx = 650, my = 270;
        for (int i=0; i<32; i++) {
            manufacturedMaterials[i] = new ItemWidget("");
            manufacturedMaterials[i].setLocalBounds(mx, my, 64, 64);
            manufacturedMaterials[i].setColor(BLACK);
            manufacturedMaterials[i].setTextColor(WHITE);
            manufacturedMaterials[i].setTextScale(0.9f);
            addChild(manufacturedMaterials[i]);
            mx += 70;
            if (mx > 1150) {
                mx = 650;
                my -= 70;
            }
        }

        incomeCaption = new Caption("Sales");
        incomeCaption.setTextColor(GREEN);
        incomeCaption.setTextScale(1.3f);
        incomeCaption.setLocalBounds(1270, 330, 300, 100);
        addChild(incomeCaption);
        soldMaterials = new ItemWidget[32];
        float sx = 1270, sy = 270;
        for (int i=0; i<32; i++) {
            soldMaterials[i] = new ItemWidget("");
            soldMaterials[i].setLocalBounds(sx, sy, 64, 64);
            soldMaterials[i].setColor(BLACK);
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
        chart.updateData(0, ledger.getHistoryRevenue(), ledger.getHistoryCounter(), GREEN);
        chart.updateData(1, ledger.getHistoryExpenses(), ledger.getHistoryCounter(), Color.RED);
        addChild(chart);

    }


    /**
     * Обновляет данные
     */
    public void updateData() {
        Ledger ledger = production.getLedger();
        String revenueText = "Sold: " + FormatUtils.formatMoney(Math.round(ledger.getLastPeriodRevenue())) + "\n";
        String expensesText = "Purchased: " + FormatUtils.formatMoney(Math.round(ledger.getLastPeriodExpenses())) + "\n";

        synchronized (this) {

            // Обновить график
            chart.updateData(0, ledger.getHistoryRevenue(), ledger.getHistoryCounter(), GREEN);
            chart.updateData(1, ledger.getHistoryExpenses(), ledger.getHistoryCounter(), Color.RED);

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
                int boughtAmount = ledger.getCommodityBoughtByPeriod(i);
                if (boughtAmount > 0 && boughtCounter < boughtMaterials.length) {
                    Material material = Material.getMaterial(i);
                    boughtMaterials[boughtCounter].setBackground(material.getImage());
                    boughtMaterials[boughtCounter].setText("" + boughtAmount);
                    boughtMaterials[boughtCounter].setActive(true);
                    boughtCounter++;
                }

                // Вывести продуктивность производства
                int manufacturedAmount = ledger.getProductivity(i);
                if (manufacturedAmount > 0 && manufacturedCounter < manufacturedMaterials.length) {
                    Material material = Material.getMaterial(i);
                    manufacturedMaterials[manufacturedCounter].setBackground(material.getImage());
                    manufacturedMaterials[manufacturedCounter].setText("" + manufacturedAmount);
                    manufacturedMaterials[manufacturedCounter].setActive(true);
                    manufacturedCounter++;
                }

                // Вывести объем продаж
                int soldAmount = ledger.getCommoditySoldByPeriod(i);
                if (soldAmount > 0 && soldCounter < soldMaterials.length) {
                    Material material = Material.getMaterial(i);
                    soldMaterials[soldCounter].setBackground(material.getImage());
                    soldMaterials[soldCounter].setText("" + soldAmount);
                    soldMaterials[soldCounter].setActive(true);
                    soldCounter++;
                }

            }

            updateSummary(ledger);
            incomeCaption.setText(revenueText);
            expenseCaption.setText(expensesText);
            reportCaption.setText(summary);

            GameMission mission =  GameManager.getMission(production.getLevel());
            String goal = mission.getName() + " #" + mission.getID() + " - " + mission.getDescription();
            panelCaption.setText(goal);

        }
    }


    private void updateSummary(Ledger ledger) {
        double totalRevenue = ledger.getTotalRevenue();
        double margin = 0;
        if (totalRevenue > 0) margin = Math.round(ledger.getTotalMargin() / totalRevenue * 100);
        summary.delete(0, summary.length());
        summary.append("Income: ");
        FormatUtils.formatMoney(Math.round(ledger.getLastPeriodRevenue()), summary);
        summary.append("\nExpenses: ");
        FormatUtils.formatMoney(Math.round(ledger.getLastPeriodExpenses()),summary);
        summary.append("\nMargin: ");
        FormatUtils.formatMoney(Math.round(ledger.getLastPeriodMargin()),summary);
        summary.append("\n\nTotal margin: ");
        FormatUtils.formatMoney(Math.round(ledger.getTotalMargin()), summary);
        summary.append(" (");
        summary.append(margin);
        summary.append("%)");
        summary.append("\nCash: ");
        FormatUtils.formatMoney(Math.round(production.getCashBalance()), summary);
        summary.append("\nAssets: ");
        FormatUtils.formatMoney(Math.round(production.getAssetsValuation()), summary);
        summary.append("\nWork in progress: ");
        FormatUtils.formatMoney(Math.round(production.getWorkInProgressValuation()),summary);
        summary.append("\nInventory: ");
        FormatUtils.formatMoney(Math.round(production.getInventory().getValuation()), summary);
        summary.append("\n\nCapitalization: ");
        FormatUtils.formatMoney(Math.round(ledger.getCapitalization()), summary);
    }


    @Override
    public void draw(Camera camera) {
        synchronized (this) {
            super.draw(camera);
        }
    }
}
