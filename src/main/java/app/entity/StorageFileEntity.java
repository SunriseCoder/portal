package app.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity(name = "storage_files")
public class StorageFileEntity {
    @Id
    @GeneratedValue
    private Long id;
    @Column
    private String title;
    @Column
    private int eventDay;
    @Column
    private int eventMonth;
    @Column
    private int eventYear;
    @Column
    private String filename;
    @ManyToOne
    private UserEntity uploadedBy;
    @Column
    private long size;
    @Column
    private int chunkSize;
    @Column
    private String checkSum;
    @Column
    private long uploadedBytes;
    @Column(unique = true)
    private String path;
    @Column
    private boolean completed;
    @Column
    private boolean published;
    @Column
    private boolean deleted;
    @Column
    private Date lastUpdated;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getEventDay() {
        return eventDay;
    }

    public void setEventDay(int eventDay) {
        this.eventDay = eventDay;
    }

    public int getEventMonth() {
        return eventMonth;
    }

    public void setEventMonth(int eventMonth) {
        this.eventMonth = eventMonth;
    }

    public int getEventYear() {
        return eventYear;
    }

    public void setEventYear(int eventYear) {
        this.eventYear = eventYear;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public UserEntity getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(UserEntity uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public String getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(String checkSum) {
        this.checkSum = checkSum;
    }

    public long getUploadedBytes() {
        return uploadedBytes;
    }

    public void setUploadedBytes(long uploadedBytes) {
        this.uploadedBytes = uploadedBytes;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public int calclateUploadedChunksNumber() {
        int uploadedChunksNumber = (int) (uploadedBytes / chunkSize);
        return uploadedChunksNumber;
    }
}
