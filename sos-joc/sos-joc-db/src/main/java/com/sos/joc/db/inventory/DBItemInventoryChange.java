package com.sos.joc.db.inventory;

import java.time.LocalDateTime;
import java.util.Date;

import com.sos.commons.hibernate.annotations.SOSCreationTimestampUtc;
import com.sos.commons.hibernate.annotations.SOSCurrentTimestampUtc;
import com.sos.commons.hibernate.annotations.SOSIdGenerator;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = DBLayer.TABLE_INV_CHANGES, uniqueConstraints = { @UniqueConstraint(columnNames = { "[NAME]" }) })
public class DBItemInventoryChange extends DBItem {

    private static final long serialVersionUID = 2550793592142097849L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSIdGenerator(sequenceName = DBLayer.TABLE_INV_CHANGES_SEQUENCE)
    private Long id;

    @Column(name = "[NAME]", nullable = false)
    private String name;

    @Column(name = "[TITLE]", nullable = true)
    private String title;

    @Column(name = "[STATE]", nullable = false)
    private Integer state;

    @Column(name = "[CLOSED]", nullable = true)
    private Date closed;

    @Column(name = "[OWNER]", nullable = false)
    private String owner;

    @Column(name = "[LAST_PUBLISHED_BY]", nullable = true)
    private String publishedBy;

    @Column(name = "[CREATED]", nullable = false)
    @SOSCreationTimestampUtc
    private LocalDateTime created;

    @Column(name = "[MODIFIED]", nullable = false)
    @SOSCurrentTimestampUtc
    private LocalDateTime modified;

    public Long getId() {
        return id;
    }

    public void setId(Long invId) {
        this.id = invId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public LocalDateTime getModified() {
        return modified;
    }

    public Date getClosed() {
        return closed;
    }

    public void setClosed(Date closed) {
        this.closed = closed;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getPublishedBy() {
        return publishedBy;
    }

    public void setPublishedBy(String publishedBy) {
        this.publishedBy = publishedBy;
    }

}