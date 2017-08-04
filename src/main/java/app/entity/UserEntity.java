package app.entity;

import java.util.EnumSet;
import java.util.Set;

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

import app.enums.Permissions;

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
    private Set<RoleEntity> roles;

    @Transient
    private EnumSet<Permissions> permissions;

    @OneToOne(mappedBy = "user")
    private UserConfirmEntity confirmation;

    @OneToOne(mappedBy = "user")
    private UserLockEntity lock;

    @Column
    private boolean shouldChangePassword;

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

    public Set<RoleEntity> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleEntity> roles) {
        this.roles = roles;
    }

    public EnumSet<Permissions> getPermissions() {
        checkInitPermissions();
        return permissions;
    }

    public Boolean hasPermission(Permissions permission) {
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

    public boolean isShouldChangePassword() {
        return shouldChangePassword;
    }

    public void setShouldChangePassword(boolean shouldChangePassword) {
        this.shouldChangePassword = shouldChangePassword;
    }

    private void checkInitPermissions() {
        if (permissions != null) {
            return;
        }

        permissions = EnumSet.noneOf(Permissions.class);

        if (roles == null) {
            return;
        }

        roles.stream().flatMap(role -> role.getPermissions().stream())
                .map(pe -> Permissions.valueOf(pe.getName()))
                .forEach(p -> permissions.add(p));
    }

    public void clearPasswords() {
        setPass("");
        setConfirm("");
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                        .setExcludeFieldNames("pass", "confirm", "roles", "permissions")
                        .toString();
    }
}
