package app.util;

public class NumberUtils {

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

}
