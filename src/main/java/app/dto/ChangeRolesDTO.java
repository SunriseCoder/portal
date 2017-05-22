package app.dto;

import java.util.List;

import app.entity.RoleEntity;

public class ChangeRolesDTO {
    private Long id;
    private List<RoleEntity> roles;

    public ChangeRolesDTO() {
        // Default constructor
    }

    public ChangeRolesDTO(Long id, List<RoleEntity> roles) {
        this.id = id;
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<RoleEntity> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleEntity> roles) {
        this.roles = roles;
    }
}
