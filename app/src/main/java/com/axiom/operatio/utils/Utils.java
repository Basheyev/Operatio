package com.axiom.operatio.utils;


import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.Locale;


public class Utils {

    private static DecimalFormat moneyFormat;
    private static final StringBuffer buffer = new StringBuffer(128);
    private static final FieldPosition position = new FieldPosition(0);

    private static void initializeFormatting() {
        moneyFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        moneyFormat.applyPattern("$###,###,###,###,###.##");
    }

    public static void formatMoney(double sum, StringBuffer targetBuffer) {
        if (targetBuffer==null) return;
        if (moneyFormat==null) initializeFormatting();
        synchronized (buffer) {
            buffer.delete(0, buffer.length());
            moneyFormat.format(sum, buffer, position);
            targetBuffer.append(buffer);
        }
    }

    public static String formatMoney(double sum) {
        if (moneyFormat==null) initializeFormatting();
        return moneyFormat.format(sum);
    }

}
