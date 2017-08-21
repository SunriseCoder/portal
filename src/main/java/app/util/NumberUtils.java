package app.util;

import java.text.DecimalFormat;

public class NumberUtils {
    private static final String SIZES_ABBREVIATION = "kMGTPE";

    public static boolean isValidLong(String string) {
        return isValidLong(string, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public static boolean isValidLong(String string, long minValue, long maxValue) {
        if (string == null || string.trim().isEmpty()) {
            return false;
        }

        try {
            long value = Long.valueOf(string);
            boolean result = value >= minValue && value <= maxValue;
            return result;
        } catch (Exception e) {
            return false;
        }
    }

    public static String format(double notation, String pattern) {
        DecimalFormat format = new DecimalFormat(pattern);
        String result = format.format(notation);
        return result;
    }

    public static String humanReadableSize(long size) {
        int unit = 1024;
        if (size < unit) {
            return size + " B";
        }

        int exp = (int) (Math.log(size) / Math.log(unit));
        char suffix = SIZES_ABBREVIATION.charAt(exp - 1);
        double notation = size / Math.pow(unit, exp);
        String result = NumberUtils.format(notation, "#,##0.#") + " " + suffix + "B";
        return result;
    }
}
