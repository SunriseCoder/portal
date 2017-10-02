package app.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class CompositeDateTag extends SimpleTagSupport {
    private int day;
    private int month;
    private int year;

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @Override
    public void doTag() throws JspException, IOException {
        JspWriter out = getJspContext().getOut();
        String parsedYear = parseNumber(year, 4);
        String parsedMonth = parseNumber(month, 2);
        String parsedDay = parseNumber(day, 2);
        String humanReadableDate = parsedYear + "-" + parsedMonth + "-" + parsedDay;
        out.println(humanReadableDate);
    }

    private String parseNumber(int number, int digits) {
        String result;
        if (number > 0) {
            result = parseKnownNumber(number, digits);
        } else {
            result = generateQuestionMarks(digits);
        }
        return result;
    }

    private String parseKnownNumber(int number, int digits) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf(number));
        while (sb.length() < digits) {
            sb.insert(0, "0");
        }
        return sb.toString();
    }

    private String generateQuestionMarks(int digits) {
        StringBuilder sb = new StringBuilder();
        while (sb.length() < digits) {
            sb.append("?");
        }
        return sb.toString();
    }
}
