package com.sos.jobscheduler.db.calendar;

import java.beans.Transient;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Type;

import com.sos.jobscheduler.db.DBItem;
import com.sos.jobscheduler.db.DBLayer;

 
@Entity
@Table( name = DBLayer.TABLE_CALENDAR_USAGE,
		uniqueConstraints = { @UniqueConstraint(columnNames = { "[SCHEDULER_ID]","[CALENDAR_ID]","[OBJECT_TYPE]","[PATH]" }) })
@SequenceGenerator(
		name = DBLayer.TABLE_CALENDAR_USAGE_SEQUENCE, 
		sequenceName = DBLayer.TABLE_CALENDAR_USAGE_SEQUENCE,
        allocationSize = 1)
public class DBItemCalendarUsage extends DBItem {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String schedulerId;
    
    private Long calendarId;
    
    private String objectType;
    
    private String path;
    
    private Boolean edited;
    
    private String configuration;
    
    private Date created;
    
    private Date modified;
    
    private List<String> basedDates;
    
    public DBItemCalendarUsage() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_CALENDAR_USAGE_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    public Long getId() {
        return this.id;
    }
    
    public void setId(Long val) {
        this.id = val;
    }

    @Column(name = "[SCHEDULER_ID]", nullable = false)
    public String getSchedulerId() {
        return this.schedulerId;
    }
    
    public void setSchedulerId(String val) {
        this.schedulerId = val;
    }
    
    @Column(name = "[CALENDAR_ID]", nullable = false)
    public Long getCalendarId() {
        return this.calendarId;
    }
    
    public void setCalendarId(Long val) {
        this.calendarId = val;
    }
    
    @Column(name = "[OBJECT_TYPE]", nullable = false)
    public String getObjectType() {
        return this.objectType;
    }
    
    public void setObjectType(String val) {
        this.objectType = val;
    }
    
    @Column(name = "[PATH]", nullable = false)
    public String getPath() {
        return this.path;
    }
    
    public void setPath(String val) {
        this.path = val;
    }
    
    @Column(name = "[EDITED]", nullable = false)
    @Type(type = "numeric_boolean")
    public Boolean getEdited() {
        return edited;
    }
    
    public void setEdited(Boolean edited) {
        this.edited = edited;
    }

    @Column(name = "[CONFIGURATION]", nullable = false)
    public String getConfiguration() {
        return configuration;
    }
    
    public void setConfiguration(String val) {
        this.configuration = val;
    }
    
    @Column(name = "[CREATED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getCreated() {
        return this.created;
    }

    public void setCreated(Date val) {
        this.created = val;
    }
    
    @Column(name = "[MODIFIED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getModified() {
        return modified;
    }
    
    public void setModified(Date modified) {
        this.modified = modified;
    }
    
    @Transient
    public List<String> basedDates() {
        return this.basedDates;
    }

    @Transient
    public void setBasedDates(List<String> basedDates) {
        this.basedDates = basedDates;
    }

}