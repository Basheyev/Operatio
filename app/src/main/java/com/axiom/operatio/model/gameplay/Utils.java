package com.axiom.operatio.model.gameplay;

import java.text.DecimalFormat;

public class Utils {

    private static DecimalFormat moneyFormat = new DecimalFormat("$###,###,###,###,###.##");

    // fixme не работает на Galaxy Note
    public static String moneyFormat(double sum) {
        return moneyFormat.format(sum);
    }

}
