package com.unicomer.qa.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Conversión entre montos mostrados en la interfaz (formato {@code $1,234.56}) y {@link BigDecimal}.
 */
public final class CurrencyUtils {

    private static final Pattern CURRENCY_FORMAT = Pattern.compile("^-?\\$\\d{1,3}(,\\d{3})*\\.\\d{2}$");

    private CurrencyUtils() {
    }

    public static boolean isValidCurrency(String text) {
        return text != null && CURRENCY_FORMAT.matcher(text.trim()).matches();
    }

    public static BigDecimal parse(String text) {
        if (!isValidCurrency(text)) {
            throw new IllegalArgumentException("Formato monetario inválido: '" + text + "'");
        }
        return new BigDecimal(text.trim().replace("$", "").replace(",", ""));
    }

    public static String format(BigDecimal amount) {
        DecimalFormat format = new DecimalFormat("$#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        String formatted = format.format(amount.abs().setScale(2, RoundingMode.HALF_UP));
        return amount.signum() < 0 ? "-" + formatted : formatted;
    }
}
