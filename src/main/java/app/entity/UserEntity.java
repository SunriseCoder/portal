package app.entity;

import java.util.HashSet;
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
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String login;

    @Column
    private String pass;

    @Transient
    private String confirm;

    @Column(unique = true)
    private String displayName;

    @Column
    private String email;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "zz_users_roles", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private List<RoleEntity> roles;

    @Transient
    private Set<String> permissions;

    @OneToOne(mappedBy = "user")
    private UserConfirmEntity confirmation;

    @OneToOne(mappedBy = "user")
    private UserLockEntity lock;

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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<RoleEntity> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleEntity> roles) {
        this.roles = roles;
    }

    public Set<String> getPermissions() {
        checkInitPermissions();
        return permissions;
    }

    public Boolean hasPermission(String permission) {
        if (isLocked()) {
            return Boolean.FALSE;
        }

        checkInitPermissions();

        boolean result = permissions.contains(permission);
        return result;
    }

    public boolean isConfirmed() {
        return confirmation != null;
    }

    public UserConfirmEntity getConfirmation() {
        return confirmation;
    }

    public void setConfirmation(UserConfirmEntity confirmation) {
        this.confirmation = confirmation;
    }

    public boolean isLocked() {
        return lock != null;
    }

    public UserLockEntity getLock() {
        return lock;
    }

    public void setLock(UserLockEntity lock) {
        this.lock = lock;
    }

    private void checkInitPermissions() {
        if (permissions != null) {
            return;
        }

        if (roles == null) {
            permissions = new HashSet<>();
            return;
        }

        permissions = roles.stream()
                        .flatMap(r -> r.getPermissions().stream())
                        .map(PermissionEntity::getName)
                        .collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                        .setExcludeFieldNames("pass", "roles", "permissions")
                        .toString();
    }
}
