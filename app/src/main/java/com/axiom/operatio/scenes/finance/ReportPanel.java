package com.axiom.operatio.scenes.finance;

import android.graphics.Color;

import com.axiom.atom.engine.ui.widgets.Caption;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.operatio.scenes.finance.charts.LineChart;
import com.axiom.operatio.model.gameplay.Ledger;
import com.axiom.operatio.model.gameplay.Utils;
import com.axiom.operatio.model.inventory.Inventory;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.Production;

import static android.graphics.Color.GREEN;
import static android.graphics.Color.WHITE;

public class ReportPanel extends Panel {

    private Production production;
    private Caption panelCaption;
    private Caption incomeCaption;
    private Caption expenseCaption;
    private Caption reportCaption;
    private LineChart chart;
    private float[] chartData;

    public ReportPanel(Production production) {
        super();
        this.production = production;

        setLocalBounds(50,60, 1850, 880);
        setColor(0xCC505050);

        panelCaption = new Caption("Financial report");
        panelCaption.setTextScale(1.5f);
        panelCaption.setTextColor(WHITE);
        panelCaption.setLocalBounds(30, getHeight() - 90, 300, 100);
        addChild(panelCaption);

        incomeCaption = new Caption("Income");
        incomeCaption.setTextColor(GREEN);
        incomeCaption.setTextScale(1.3f);
        incomeCaption.setLocalBounds(50, 700, 300, 100);
        addChild(incomeCaption);

        expenseCaption = new Caption("Expense");
        expenseCaption.setTextColor(1,0.5f,0.5f, 1);
        expenseCaption.setTextScale(1.3f);
        expenseCaption.setLocalBounds(700, 700,300, 100);
        addChild(expenseCaption);

        reportCaption = new Caption("Cashflow");
        reportCaption.setTextColor(WHITE);
        reportCaption.setTextScale(1.3f);
        reportCaption.setLocalBounds(1200, 700, 300, 100);
        addChild(reportCaption);


        chart = new LineChart(2);
        chart.setLocalBounds(25,25, 800, 300);
        Ledger ledger = production.getLedger();
        chart.updateData(0, ledger.getHistoryRevenue(), ledger.getHistoryCounter(), GREEN);
        chart.updateData(1, ledger.getHistoryExpenses(), ledger.getHistoryCounter(), Color.RED);
        addChild(chart);

    }

    public void updateData() {
        Ledger ledger = production.getLedger();
        String revenueText = "REVENUE - " + Utils.moneyFormat(ledger.getTotalRevenue()) + "\n";
        String expensesText = "EXPENSES - " + Utils.moneyFormat(ledger.getTotalExpenses()) + "\n";

        int soldCounter = 0;
        int boughtCounter = 1;
        expensesText += "\n" + boughtCounter + ". Maintenance - " + Utils.moneyFormat(ledger.getTotalMaintenanceCost());

        // Обновить график
        chart.updateData(0, ledger.getHistoryRevenue(), ledger.getHistoryCounter(), GREEN);
        chart.updateData(1, ledger.getHistoryExpenses(), ledger.getHistoryCounter(), Color.RED);

        for (int i=0; i< Inventory.SKU_COUNT; i++) {
            double soldSum = ledger.getCommoditySoldSum(i);
            double boughtSum = ledger.getCommodityBoughtSum(i);
            int soldAmount = ledger.getCommoditySoldAmount(i);
            int boughtAmount = ledger.getCommodityBoughtAmount(i);
            if (soldSum > 0) {
                soldCounter++;
                revenueText += "\n" + soldCounter + ". "
                        + Material.getMaterial(i).getName() +  " - " + Utils.moneyFormat(soldSum)
                        + " (" + soldAmount + ")";
            }
            if (boughtSum > 0) {
                boughtCounter++;
                expensesText += "\n" +boughtCounter + ". "
                        + Material.getMaterial(i).getName() +  " - " + Utils.moneyFormat(boughtSum)
                        + " (" + boughtAmount + ")";
            }
        }

        double periodRevenue = ledger.getTotalRevenue();
        double margin = 0;
        if (periodRevenue > 0) margin = Math.round(ledger.getTotalMargin() / periodRevenue * 100);

        String report = "MARGIN - " + Utils.moneyFormat(ledger.getTotalMargin()) + " ("
                        + margin + "%)"
                        + "\n\nCash - " + Utils.moneyFormat(production.getCashBalance())
                        + "\nAssets - " + Utils.moneyFormat(production.getAssetsValuation())
                        + "\nWork in progress - " + Utils.moneyFormat(production.getWorkInProgressValuation())
                        + "\nInventory - " + Utils.moneyFormat(production.getInventory().getValuation())
                        + "\n\nCapitalization - " + Utils.moneyFormat(ledger.getCapitalization());

        panelCaption.setText("Financial report - " + production.getCurrentCycle() / Ledger.OPERATIONAL_DAY_CYCLES + " day");
        incomeCaption.setText(revenueText);
        expenseCaption.setText(expensesText);
        reportCaption.setText(report);

    }

}
