package com.axiom.operatio.model.ledger;

public class LedgerPeriod {

    protected double revenue = 0;                             // Операционная выручка за период
    protected double expenses = 0;                            // Операционные расходы за период
    protected double margin = 0;
    protected double cashBalance = 0;                         // Операционная маржа за период
    protected double maintenanceCost = 0;
    protected double assetsBought = 0;
    protected double assetsSold = 0;

    public double getRevenue() {
        return revenue;
    }

    public double getExpenses() {
        return expenses;
    }

    public double getCashBalance() {
        return cashBalance;
    }

    public void copy(LedgerPeriod from) {
        this.revenue = from.revenue;
        this.expenses = from.expenses;
        this.margin = from.margin;
        this.cashBalance = from.cashBalance;
        this.maintenanceCost = from.maintenanceCost;
        this.assetsBought = from.assetsBought;
        this.assetsSold = from.assetsSold;
    }
}
