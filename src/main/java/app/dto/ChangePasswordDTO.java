package app.dto;

public class ChangePasswordDTO {
    private Long id;
    private String pass;

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
}
