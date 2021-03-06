package app.dto;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ChangePasswordDTO {
    private Long id;
    private String pass;
    private String confirm;

    public ChangePasswordDTO() {
        // Default constructor
    }

    public ChangePasswordDTO(Long id, String pass) {
        this.id = id;
        this.pass = pass;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getConfirm() {
        return confirm;
    }

    public void setConfirm(String confirm) {
        this.confirm = confirm;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public void clearPasses() {
        setPass("");
        setConfirm("");
    }
}
