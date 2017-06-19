package app.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity(name = "audit_events")
public class AuditEventEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private UserEntity user;

    // TODO Rewrite it and test to use ZonedDateTime if JPA 2.2 will support it
    private Date date;
    private String ip;

    @ManyToOne
    private OperationTypeEntity operation;
    @ManyToOne
    private AuditEventTypeEntity type;

    private String objectBefore;
    private String objectAfter;
    private String error;

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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public OperationTypeEntity getOperation() {
        return operation;
    }

    public void setOperation(OperationTypeEntity operation) {
        this.operation = operation;
    }

    public AuditEventTypeEntity getType() {
        return type;
    }

    public void setType(AuditEventTypeEntity type) {
        this.type = type;
    }

    public String getObjectBefore() {
        return objectBefore;
    }

    public void setObjectBefore(String objectBefore) {
        this.objectBefore = objectBefore;
    }

    public String getObjectAfter() {
        return objectAfter;
    }

    public void setObjectAfter(String objectAfter) {
        this.objectAfter = objectAfter;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
    
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
