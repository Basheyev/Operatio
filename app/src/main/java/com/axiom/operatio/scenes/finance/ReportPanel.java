package com.axiom.operatio.scenes.finance;

import com.axiom.atom.engine.ui.widgets.Caption;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.operatio.model.gameplay.Ledger;
import com.axiom.operatio.model.gameplay.Utils;
import com.axiom.operatio.model.inventory.Inventory;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.Production;

import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;
import static android.graphics.Color.WHITE;

public class ReportPanel extends Panel {

    private Production production;
    private Caption incomeCaption;
    private Caption expenseCaption;
    private Caption reportCaption;

    public ReportPanel(Production production) {
        super();
        this.production = production;

        setLocalBounds(50,60, 1880, 880);
        setColor(0xCC505050);

        Caption caption = new Caption("Financial report");
        caption.setTextScale(1.5f);
        caption.setTextColor(WHITE);
        caption.setLocalBounds(30, getHeight() - 90, 300, 100);
        addChild(caption);

        incomeCaption = new Caption("Income");
        incomeCaption.setTextColor(GREEN);
        incomeCaption.setTextScale(1.3f);
        incomeCaption.setLocalBounds(50, 700, 300, 100);
        addChild(incomeCaption);

        expenseCaption = new Caption("Expense");
        expenseCaption.setTextColor(RED);
        expenseCaption.setTextScale(1.3f);
        expenseCaption.setLocalBounds(800, 700,300, 100);
        addChild(expenseCaption);

        reportCaption = new Caption("Report");
        reportCaption.setTextColor(WHITE);
        reportCaption.setTextScale(1.3f);
        reportCaption.setLocalBounds(1400, 700, 300, 100);
        addChild(reportCaption);
    }

    public void updateData() {
        Ledger ledger = production.getLedger();
        String incomeText = "INCOME - " + Utils.moneyFormat(ledger.getPeriodIncome()) + "\n";
        String expensesText = "EXPENSES - " + Utils.moneyFormat(ledger.getPeriodExpenses()) + "\n";

        int soldCounter = 0;
        int boughtCounter = 0;
        for (int i=0; i< Inventory.SKU_COUNT; i++) {
            double soldSum = ledger.getCommoditySold(i);
            double boughtSum = ledger.getCommodityBought(i);
            if (soldSum > 0) {
                soldCounter++;
                incomeText += "\n" + soldCounter + ". "
                        + Material.getMaterial(i).getName() +  " - " + Utils.moneyFormat(soldSum);
            }
            if (boughtSum > 0) {
                boughtCounter++;
                expensesText += "\n" +boughtCounter + ". "
                        + Material.getMaterial(i).getName() +  " - " + Utils.moneyFormat(boughtSum);
            }
        }

        incomeCaption.setText(incomeText);
        expenseCaption.setText(expensesText);
        reportCaption.setText("CASHFLOW " + Utils.moneyFormat(ledger.getPeriodCashflow()));
    }

}
