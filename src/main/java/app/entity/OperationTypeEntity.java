package app.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity(name = "operation_types")
public class OperationTypeEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String comment;
    private byte severity;

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

    public byte getSeverity() {
        return severity;
    }

    public void setSeverity(byte severity) {
        this.severity = severity;
    }
}
