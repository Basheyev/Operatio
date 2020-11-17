package com.axiom.operatio.scenes.finance;

import com.axiom.atom.engine.ui.widgets.Caption;
import com.axiom.atom.engine.ui.widgets.Panel;
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
    }

    public void updateData() {
        Ledger ledger = production.getLedger();
        String revenueText = "REVENUE - " + Utils.moneyFormat(ledger.getPeriodOperRevenue()) + "\n";
        String expensesText = "EXPENSES - " + Utils.moneyFormat(ledger.getPeriodOperExpenses()) + "\n";

        int soldCounter = 0;
        int boughtCounter = 1;
        expensesText += "\n" + boughtCounter + ". Maintenance - " + Utils.moneyFormat(ledger.getPeriodMaintenanceCost());

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

        String report = "MARGIN - " + Utils.moneyFormat(ledger.getPeriodOperMargin()) + " ("
                        + Math.round(ledger.getPeriodOperMargin() / ledger.getPeriodOperRevenue() * 100) + "%)"
                        + "\n\nCash - " + Utils.moneyFormat(production.getCashBalance())
                        + "\nAssets - " + Utils.moneyFormat(production.getAssetsValuation())
                        + "\nWork in progress - " + Utils.moneyFormat(production.getWorkInProgressValuation())
                        + "\nInventory - " + Utils.moneyFormat(production.getInventory().getValuation())
                        + "\n\nCapitalization - " + Utils.moneyFormat(ledger.getCapitalization(production));

        panelCaption.setText("Financial report - " + production.getCurrentCycle() / Ledger.REPORTING_PERIOD + " day");
        incomeCaption.setText(revenueText);
        expenseCaption.setText(expensesText);
        reportCaption.setText(report);

    }

}
