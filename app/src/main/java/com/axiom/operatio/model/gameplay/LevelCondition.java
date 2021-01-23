package com.axiom.operatio.model.gameplay;


public class LevelCondition {

    public static final int MANUFACTURED_AMOUNT = 1;           // Произведенное количество
    public static final int MANUFACTURE_PRODUCTIVITY = 2;      // Продуктивность за время
    public static final int SOLD_AMOUNT = 3;                   // Проданное количество
    public static final int CASH_BALANCE = 4;                  // Остаток денег
    public static final int OPERATING_MARGIN = 5;              // Операционная маржинальность
    public static final int CAPITALIZATION = 6;                // Капитализация

    protected int KPI;                                         // Показатель
    protected int materialID;                                  // ID материала
    protected double value;                                    // Значение

    public LevelCondition(int KPI, int materialID, double value) {
        this.KPI = KPI;
        this.materialID = materialID;
        this.value = value;
    }

    public boolean check(Ledger ledger) {
        switch (KPI) {
            case MANUFACTURED_AMOUNT:
                return checkManufacturedAmount(ledger);
            case MANUFACTURE_PRODUCTIVITY:
                return checkManufacturedProductivity(ledger);
            case SOLD_AMOUNT:
                return checkSoldAmount(ledger);
            case CASH_BALANCE:
                return checkCashBalance(ledger);
            case OPERATING_MARGIN:
                return checkOperatingMargin(ledger);
            case CAPITALIZATION:
                return checkCapitalization(ledger);
        }
        return false;
    }


    private boolean checkManufacturedAmount(Ledger ledger) {
        return ledger.getCommodityManufacturedAmount(materialID) >= value;
    }

    private boolean checkManufacturedProductivity(Ledger ledger) {
        // TODO За какой период считать продуктовность - продукции/время)?
        return false;
    }

    public boolean checkSoldAmount(Ledger ledger) {
        return ledger.getCommoditySoldAmount(materialID) >= value;
    }

    public boolean checkCashBalance(Ledger ledger) {
        return ledger.getCurrentCashBalance() >= value;
    }

    public boolean checkOperatingMargin(Ledger ledger) {
        return ledger.getPeriodOperMargin() >= value;
    }

    public boolean checkCapitalization(Ledger ledger) {
        return ledger.getCapitalization() >= value;
    }

}
