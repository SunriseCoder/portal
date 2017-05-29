package app.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity(name = "z_user_confirms")
public class UserConfirmEntity {
    @Id
    @GeneratedValue
    private Long id;
    @OneToOne
    private UserEntity user;
    private String comment;
    @ManyToOne
    private UserEntity confirmedBy;

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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public UserEntity getConfirmedBy() {
        return confirmedBy;
    }

    public void setConfirmedBy(UserEntity confirmedBy) {
        this.confirmedBy = confirmedBy;
    }
}
