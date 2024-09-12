package com.sos.joc.db.inventory;

import java.util.Date;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Table(name = DBLayer.TABLE_INV_CHANGES, uniqueConstraints = {@UniqueConstraint(columnNames = { "[NAME]" }) })
public class DBItemInventoryChange extends DBItem {

    private static final long serialVersionUID = 2550793592142097849L;

    @Id
    @Column(name = "[ID]", nullable = false)
    private Long invId;
    
    @Column(name = "[NAME]", nullable = false)
    private String name;
    
    @Column(name = "[TITLE]", nullable = true)
    private String title;
    
    @Column(name = "[STATE]", nullable = false)
    private Integer state;
    
    @Column(name = "[CREATED]", nullable = false)
    private Date created;
    
    @Column(name = "[MODIFIED]", nullable = false)
    private Date modified;
    
    @Column(name = "[CLOSED]", nullable = true)
    private Date closed;
    
    @Column(name = "[OWNER]", nullable = false)
    private String owner;
    
    @Column(name = "[LAST_PUBLISHED_BY]", nullable = true)
    private String publishedBy;
    
    
    public Long getInvId() {
        return invId;
    }
    public void setInvId(Long invId) {
        this.invId = invId;
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
    
    public Date getCreated() {
        return created;
    }
    public void setCreated(Date created) {
        this.created = created;
    }
    
    public Date getModified() {
        return modified;
    }
    public void setModified(Date modified) {
        this.modified = modified;
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