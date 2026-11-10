package chbcars.util;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Small formatting helpers shared by the whole system. */
public final class Format {

    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.00");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Format() {
    }

    public static String money(double amount) {
        return "\u20B1" + MONEY.format(amount);
    }

    public static String now() {
        return LocalDateTime.now().format(DATE_TIME);
    }

    public static String today() {
        return LocalDate.now().toString();
    }

    public static int currentYear() {
        return LocalDate.now().getYear();
    }

    public static String pad(String text, int width) {
        if (text == null) {
            text = "";
        }
        return text.length() >= width ? text.substring(0, width) : text + " ".repeat(width - text.length());
    }
}
