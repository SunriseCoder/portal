package app.dto;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import app.util.NumberUtils;

public class FileInfoDTO {
    private static final Pattern DATE_PATTERN = Pattern.compile("^([0-9\\?]{2})\\.([0-9\\?]{2})\\.([0-9\\?]{4})$");

    private Long id;
    private String title;
    private int eventDay;
    private int eventMonth;
    private int eventYear;
    private int position;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getEventDay() {
        return eventDay;
    }

    public void setEventDay(int eventDay) {
        this.eventDay = eventDay;
    }

    public int getEventMonth() {
        return eventMonth;
    }

    public void setEventMonth(int eventMonth) {
        this.eventMonth = eventMonth;
    }

    public int getEventYear() {
        return eventYear;
    }

    public void setEventYear(int eventYear) {
        this.eventYear = eventYear;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public static FileInfoDTO fromRequest(HttpServletRequest request) {
        FileInfoDTO fileInfo = new FileInfoDTO();
        fileInfo.setId(Long.parseLong(request.getParameter("id")));
        fileInfo.setTitle(request.getParameter("title"));
        String dateStr = request.getParameter("date");
        Matcher matcher = DATE_PATTERN.matcher(dateStr);
        if (matcher.matches()) {
            fileInfo.setEventDay(getValueSafely(matcher.group(1)));
            fileInfo.setEventMonth(getValueSafely(matcher.group(2)));
            fileInfo.setEventYear(getValueSafely(matcher.group(3)));
        }
        String positionStr = request.getParameter("position");
        if (positionStr != null && !positionStr.isEmpty() && NumberUtils.isValidInt(positionStr)) {
            fileInfo.setPosition(Integer.parseInt(positionStr));
        }
        return fileInfo;
    }

    private static int getValueSafely(String string) {
        int result = 0;
        try {
            result = Integer.parseInt(string);
        } catch (Exception e) {
            // Nothing to do
        }
        return result;
    }
}
