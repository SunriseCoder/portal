package app.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity(name = "audit_event_types")
public class AuditEventTypeEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private boolean aware;

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

    public boolean isAware() {
        return aware;
    }

    public void setAware(boolean aware) {
        this.aware = aware;
    }
}
