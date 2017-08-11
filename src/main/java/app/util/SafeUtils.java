package app.util;

public class SafeUtils {

    public static boolean safeEquals(Long l1, Long l2) {
        return l1 == null ? l1 == l2 : l1.equals(l2);
    }

    public static boolean safeEquals(String s1, String s2) {
        return s1 == null ? s1 == s2 : s1.equals(s2);
    }
}
