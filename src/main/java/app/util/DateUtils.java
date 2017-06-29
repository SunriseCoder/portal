package app.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    public static Date parseDateSilent(SimpleDateFormat dateFormat, String string) {
        Date date = null;
        try {
            date = dateFormat.parse(string);
        } catch (NullPointerException | ParseException e) {
            // Just swallowing exception
        }
        return date;
    }
}
