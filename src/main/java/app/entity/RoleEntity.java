package app.entity;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

@Entity(name = "roles")
public class RoleEntity {
    @Id
    @GeneratedValue
    private Long id;
    @Column(unique = true)
    private String name;
    private String comment;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "zz_roles_permissions", joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id", referencedColumnName = "id"))
    private List<PermissionEntity> permissions;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "zz_roles_roles", joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "included_role_id", referencedColumnName = "id"))
    private List<RoleEntity> includedRoles;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<PermissionEntity> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionEntity> permissions) {
        this.permissions = permissions;
    }

    public List<RoleEntity> getIncludedRoles() {
        return includedRoles;
    }

    public void setIncludedRoles(List<RoleEntity> includedRoles) {
        this.includedRoles = includedRoles;
    }

    public Set<String> getAllPermissions() {
        Set<String> permissions = this.permissions.stream()
                .map(PermissionEntity::getName).collect(Collectors.toSet());
        includedRoles.stream().forEach(role -> permissions.addAll(role.getAllPermissions()));
        return permissions;
    }
}
