package app.dto;

public class ChangeLoginDTO {
    private Long id;
    private String login;

    public ChangeLoginDTO() {
        // Default constructor
    }

    public ChangeLoginDTO(Long id, String login) {
        this.id = id;
        this.login = login;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
