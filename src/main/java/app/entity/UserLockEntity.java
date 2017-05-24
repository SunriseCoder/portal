package app.entity;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity(name = "z_user_locks")
@Embeddable
public class UserLockEntity {
    @Id
    @GeneratedValue
    private Long id;
    @OneToOne
    private UserEntity user;
    private String reason;
    @ManyToOne
    private UserEntity lockedBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public UserEntity getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(UserEntity lockedBy) {
        this.lockedBy = lockedBy;
    }
}
