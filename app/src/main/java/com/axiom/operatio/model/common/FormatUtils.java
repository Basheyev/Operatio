package com.axiom.operatio.model.common;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Класс для форматирования текста с минимальным использованием памяти
 */
public class FormatUtils {

    private static DecimalFormat moneyFormat;
    private static final StringBuffer buffer = new StringBuffer(32);
    private static final FieldPosition position = new FieldPosition(0);

    private static void initializeFormatting() {
        moneyFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        moneyFormat.applyPattern("$###,###,###,###,###.##");
    }

    public static StringBuffer formatMoneyAppend(double sum, StringBuffer targetBuffer) {
        if (targetBuffer==null) return null;
        if (moneyFormat==null) initializeFormatting();
        synchronized (buffer) {
            buffer.setLength(0);
            moneyFormat.format(sum, buffer, position);
            targetBuffer.append(buffer);
        }
        return targetBuffer;
    }


    public static StringBuffer formatMoney(double sum, StringBuffer targetBuffer) {
        if (targetBuffer==null) return null;
        if (moneyFormat==null) initializeFormatting();
        targetBuffer.setLength(0);
        moneyFormat.format(sum, targetBuffer, position);
        return targetBuffer;
    }


    public static StringBuffer formatLong(long value, StringBuffer targetBuffer) {
        if (targetBuffer==null) return null;
        targetBuffer.delete(0, targetBuffer.length());
        return targetBuffer.append(value);
    }


    public static String formatDateAndTime() {
        SimpleDateFormat timeStampFormat = new SimpleDateFormat("HH:mm dd-MMM-yyyy", Locale.getDefault());
        Date myDate = new Date();
        return timeStampFormat.format(myDate);
    }

}
