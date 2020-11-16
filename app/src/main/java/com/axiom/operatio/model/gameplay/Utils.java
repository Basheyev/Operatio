package com.axiom.operatio.model.gameplay;

import java.text.DecimalFormat;

public class Utils {

    private static DecimalFormat decimalFormat = new DecimalFormat("$###,###,###,###.00");

    public static String moneyFormat(double sum) {
        return decimalFormat.format(sum);
    }

}
