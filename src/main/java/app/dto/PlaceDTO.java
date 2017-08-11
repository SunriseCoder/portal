package app.dto;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import app.entity.PlaceEntity;

public class PlaceDTO {
    private Long id;
    private String name;
    private boolean system;
    private PlaceDTO parent;
    private List<PlaceDTO> children;
    private String path;

    public PlaceDTO() {
        // Default constructor
    }

    public PlaceDTO(PlaceEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        if (entity.getChildren() == null) {
            return;
        }
        this.system = entity.isSystem();

        PlaceEntity parentEntity = entity.getParent();
        if (parentEntity != null) {
            parent = new PlaceDTO();
            parent.setId(parentEntity.getId());
            parent.setName(parentEntity.getName());
        }

        children = new ArrayList<>();
        for (PlaceEntity childEntity : entity.getChildren()) {
            PlaceDTO childDto = new PlaceDTO(childEntity);
            childDto.setParent(this);
            children.add(childDto);
        }
    }

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

    public boolean isSystem() {
        return system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

    public PlaceDTO getParent() {
        return parent;
    }

    public void setParent(PlaceDTO parent) {
        this.parent = parent;
    }

    public List<PlaceDTO> getChildren() {
        return children;
    }

    public void setChildren(List<PlaceDTO> children) {
        this.children = children;
    }

    public String getPath() {
        if (path != null) {
            return path;
        }

        String path = name;
        if (parent != null) {
            path = parent.getPath() + " > " + path;
        }
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
