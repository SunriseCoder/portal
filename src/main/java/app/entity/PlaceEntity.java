package app.entity;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity(name = "places")
public class PlaceEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String name;

    @ManyToOne
    private PlaceEntity parent;

    @OneToMany(mappedBy = "parent")
    private List<PlaceEntity> children;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PlaceEntity getParent() {
        return parent;
    }

    public void setParent(PlaceEntity parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PlaceEntity> getChildren() {
        return children;
    }

    public void setChildren(List<PlaceEntity> children) {
        this.children = children;
    }

    public String getPath() {
        String path = name;
        if (parent != null) {
            path = parent.getPath() + " > " + path;
        }
        return path;
    }

    public String getFirstAndLast() {
        String last = parent == null ? null : getRoot();
        String result = name;
        if (last != null) {
            result += " (" + last + ")";
        }
        return result;
    }

    private String getRoot() {
        if (parent == null) {
            return name;
        }
        return parent.getRoot();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[id=" + id + ",path=" + getPath() + "]";
    }
}
