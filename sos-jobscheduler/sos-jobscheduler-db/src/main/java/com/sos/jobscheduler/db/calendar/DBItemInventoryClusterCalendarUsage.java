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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;
import com.sos.jobscheduler.db.JocDBItemConstants;

 
@Entity
@Table(name = JocDBItemConstants.TABLE_INVENTORY_CLUSTER_CALENDAR_USAGE)
@SequenceGenerator(name = JocDBItemConstants.TABLE_INVENTORY_CLUSTER_CALENDAR_USAGE_SEQUENCE, sequenceName = JocDBItemConstants.TABLE_INVENTORY_CLUSTER_CALENDAR_USAGE_SEQUENCE,
    allocationSize = 1)
public class DBItemInventoryClusterCalendarUsage implements Serializable {

    private static final long serialVersionUID = 1L;

     /** Primary key */
    private Long id;

    /** Others */
    private String schedulerId;
    private Long calendarId;
    private String objectType;
    private String path;
    private Boolean edited;
    private String configuration;
    private Date created;
    private Date modified;
    private List<String> basedDates;
    
    public DBItemInventoryClusterCalendarUsage() {
    }

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = JocDBItemConstants.TABLE_INVENTORY_CLUSTER_CALENDAR_USAGE_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    public Long getId() {
        return this.id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = JocDBItemConstants.TABLE_INVENTORY_CLUSTER_CALENDAR_USAGE_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    public void setId(Long val) {
        this.id = val;
    }

    /** Others */
    @Column(name = "[SCHEDULER_ID]", nullable = false)
    public void setSchedulerId(String val) {
        this.schedulerId = val;
    }

    @Column(name = "[SCHEDULER_ID]", nullable = false)
    public String getSchedulerId() {
        return this.schedulerId;
    }
    
    @Column(name = "[CALENDAR_ID]", nullable = false)
    public void setCalendarId(Long val) {
        this.calendarId = val;
    }

    @Column(name = "[CALENDAR_ID]", nullable = false)
    public Long getCalendarId() {
        return this.calendarId;
    }
    
    @Column(name = "[OBJECT_TYPE]", nullable = false)
    public void setObjectType(String val) {
        this.objectType = val;
    }

    @Column(name = "[OBJECT_TYPE]", nullable = false)
    public String getObjectType() {
        return this.objectType;
    }
    
    @Column(name = "[PATH]", nullable = false)
    public void setPath(String val) {
        this.path = val;
    }

    @Column(name = "[PATH]", nullable = false)
    public String getPath() {
        return this.path;
    }
    
    @Column(name = "[EDITED]", nullable = false)
    @Type(type = "numeric_boolean")
    public Boolean getEdited() {
        return edited;
    }

    @Column(name = "[EDITED]", nullable = false)
    @Type(type = "numeric_boolean")
    public void setEdited(Boolean edited) {
        this.edited = edited;
    }

    @Column(name = "[CONFIGURATION]", nullable = true)
    public void setConfiguration(String val) {
        this.configuration = val;
    }

    @Column(name = "[CONFIGURATION]", nullable = true)
    public String getConfiguration() {
        return configuration;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]", nullable = false)
    public void setCreated(Date val) {
        this.created = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]", nullable = false)
    public Date getCreated() {
        return this.created;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[MODIFIED]", nullable = false)
    public Date getModified() {
        return modified;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[MODIFIED]", nullable = false)
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

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(schedulerId).append(calendarId).append(objectType).append(path).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        // always compare on unique constraint
        if (other == this) {
            return true;
        }
        if (!(other instanceof DBItemInventoryClusterCalendarUsage)) {
            return false;
        }
        DBItemInventoryClusterCalendarUsage rhs = ((DBItemInventoryClusterCalendarUsage) other);
        return new EqualsBuilder().append(schedulerId,rhs.schedulerId).append(calendarId, rhs.calendarId)
                .append(objectType, rhs.objectType).append(path, rhs.path).isEquals();
    }

}