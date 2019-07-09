package com.sos.jobscheduler.db.calendar;

import java.beans.Transient;
import java.io.Serializable;
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

import com.sos.jobscheduler.db.DBLayer;

 
@Entity
@Table( name = DBLayer.TABLE_CALENDAR_USAGE,
		uniqueConstraints = { @UniqueConstraint(columnNames = { "[SCHEDULER_ID]","[CALENDAR_ID]","[OBJECT_TYPE]","[PATH]" }) })
@SequenceGenerator(
		name = DBLayer.TABLE_CALENDAR_USAGE_SEQUENCE, 
		sequenceName = DBLayer.TABLE_CALENDAR_USAGE_SEQUENCE,
        allocationSize = 1)
public class DBItemCalendarUsage implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_CALENDAR_USAGE_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[SCHEDULER_ID]", nullable = false)
    private String schedulerId;
    
    @Column(name = "[CALENDAR_ID]", nullable = false)
    private Long calendarId;
    
    @Column(name = "[OBJECT_TYPE]", nullable = false)
    private String objectType;
    
    @Column(name = "[PATH]", nullable = false)
    private String path;
    
    @Column(name = "[EDITED]", nullable = false)
    @Type(type = "numeric_boolean")
    private Boolean edited;
    
    @Column(name = "[CONFIGURATION]", nullable = false)
    private String configuration;
    
    @Column(name = "[CREATED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    
    @Column(name = "[MODIFIED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;
    
    private List<String> basedDates;
    
    public DBItemCalendarUsage() {
    }

    public Long getId() {
        return this.id;
    }
    public void setId(Long val) {
        this.id = val;
    }

    public void setSchedulerId(String val) {
        this.schedulerId = val;
    }
    public String getSchedulerId() {
        return this.schedulerId;
    }
    
    public void setCalendarId(Long val) {
        this.calendarId = val;
    }
    public Long getCalendarId() {
        return this.calendarId;
    }
    
    public void setObjectType(String val) {
        this.objectType = val;
    }
    public String getObjectType() {
        return this.objectType;
    }
    
    public void setPath(String val) {
        this.path = val;
    }
    public String getPath() {
        return this.path;
    }
    
    public Boolean getEdited() {
        return edited;
    }
    @Type(type = "numeric_boolean")
    public void setEdited(Boolean edited) {
        this.edited = edited;
    }

    public void setConfiguration(String val) {
        this.configuration = val;
    }
    public String getConfiguration() {
        return configuration;
    }
    
    public void setCreated(Date val) {
        this.created = val;
    }
    public Date getCreated() {
        return this.created;
    }

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