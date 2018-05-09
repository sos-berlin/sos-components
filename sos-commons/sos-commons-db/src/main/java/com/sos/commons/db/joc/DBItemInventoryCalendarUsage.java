package com.sos.commons.db.joc;

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

@Entity
@Table(name = JocDBItemConstants.TABLE_INVENTORY_CALENDAR_USAGE)
@SequenceGenerator(
		name = JocDBItemConstants.TABLE_INVENTORY_CALENDAR_USAGE_SEQUENCE,
		sequenceName = JocDBItemConstants.TABLE_INVENTORY_CALENDAR_USAGE_SEQUENCE,
		allocationSize = 1)
public class DBItemInventoryCalendarUsage implements Serializable {

    private static final long serialVersionUID = 1L;

     /** Primary key */
    private Long id;

    /** Others */
    private Long instanceId;
    private Long calendarId;
    private String objectType;
    private String path;
    private Boolean edited;
    private String configuration;
    private Date created;
    private Date modified;
    private List<String> basedDates;
    
    public DBItemInventoryCalendarUsage() {
    }

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = JocDBItemConstants.TABLE_INVENTORY_CALENDAR_USAGE_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return this.id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = JocDBItemConstants.TABLE_INVENTORY_CALENDAR_USAGE_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public void setId(Long val) {
        this.id = val;
    }

    /** Others */
    @Column(name = "`INSTANCE_ID`", nullable = false)
    public void setInstanceId(Long val) {
        this.instanceId = val;
    }

    @Column(name = "`INSTANCE_ID`", nullable = false)
    public Long getInstanceId() {
        return this.instanceId;
    }

    @Column(name = "`CALENDAR_ID`", nullable = false)
    public void setCalendarId(Long val) {
        this.calendarId = val;
    }

    @Column(name = "`CALENDAR_ID`", nullable = false)
    public Long getCalendarId() {
        return this.calendarId;
    }
    
    @Column(name = "`OBJECT_TYPE`", nullable = false)
    public void setObjectType(String val) {
        this.objectType = val;
    }

    @Column(name = "`OBJECT_TYPE`", nullable = false)
    public String getObjectType() {
        return this.objectType;
    }
    
    @Column(name = "`PATH`", nullable = false)
    public void setPath(String val) {
        this.path = val;
    }

    @Column(name = "`PATH`", nullable = false)
    public String getPath() {
        return this.path;
    }
    
    @Column(name = "`EDITED`", nullable = false)
    @Type(type = "numeric_boolean")
    public Boolean getEdited() {
        return edited;
    }

    @Column(name = "`EDITED`", nullable = false)
    @Type(type = "numeric_boolean")
    public void setEdited(Boolean edited) {
        this.edited = edited;
    }

    @Column(name = "`CONFIGURATION`", nullable = true)
    public void setConfiguration(String val) {
        this.configuration = val;
    }

    @Column(name = "`CONFIGURATION`", nullable = true)
    public String getConfiguration() {
        return configuration;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public void setCreated(Date val) {
        this.created = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public Date getCreated() {
        return this.created;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public Date getModified() {
        return modified;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
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
        return new HashCodeBuilder().append(instanceId).append(calendarId).append(objectType).append(path).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        // always compare on unique constraint
        if (other == this) {
            return true;
        }
        if (!(other instanceof DBItemInventoryCalendarUsage)) {
            return false;
        }
        DBItemInventoryCalendarUsage rhs = ((DBItemInventoryCalendarUsage) other);
        return new EqualsBuilder().append(instanceId,rhs.instanceId).append(calendarId, rhs.calendarId)
                .append(objectType, rhs.objectType).append(path, rhs.path).isEquals();
    }

}