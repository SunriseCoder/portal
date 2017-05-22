package app.dto;

public class ChangeDisplayNameDTO {
    private Long id;
    private String displayName;

    public ChangeDisplayNameDTO() {
        // Default constructor
    }

    public ChangeDisplayNameDTO(Long id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
