package com.axiom.operatio.model.gameplay;


import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.Locale;


public class Utils {

    private static DecimalFormat moneyFormat;
    private static final StringBuffer buffer = new StringBuffer(128);
    private static final FieldPosition position = new FieldPosition(0);

    private static void initialize() {
        moneyFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        moneyFormat.applyPattern("$###,###,###,###,###.##");
    }

    public static StringBuffer moneyAsBuffer(double sum) {
        if (moneyFormat==null) initialize();
        synchronized (buffer) {
            buffer.delete(0, buffer.length());
            moneyFormat.format(sum, buffer, position);
        }
        return buffer;
    }

    public static String moneyAsString(double sum) {
        if (moneyFormat==null) initialize();
        return moneyFormat.format(sum);
    }

}
