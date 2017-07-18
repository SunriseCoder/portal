package app.dto;

import java.text.SimpleDateFormat;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import app.entity.FestivalEntity;

public class FestivalDTO {
    private Long id;
    private String details;
    private String start;
    private String end;
    private String place;
    private String addedBy;
    private Long ownerId;

    public FestivalDTO() {
        // Default constructor
    }

    public FestivalDTO(FestivalEntity entity) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.id = entity.getId();
        this.details = entity.getDetails();
        this.start = dateFormat.format(entity.getStart());
        this.end = dateFormat.format(entity.getEnd());
        this.place = entity.getPlace().getFirstAndLast();
        this.addedBy = entity.getAddedBy().getDisplayName();
        this.ownerId = entity.getAddedBy().getId();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
