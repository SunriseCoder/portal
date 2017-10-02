package app.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import app.util.NumberUtils;

public class NumbersTag extends SimpleTagSupport {
    private Long size;

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    @Override
    public void doTag() throws JspException, IOException {
        JspWriter out = getJspContext().getOut();
        String humanReadableSize = NumberUtils.humanReadableSize(size);
        out.println(humanReadableSize);
    }
}
