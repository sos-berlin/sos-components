package com.sos.jobscheduler.db.calendar;

import java.io.Serializable;
import java.util.Date;

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

import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.db.JocDBItemConstants;

@Entity
@Table(name = JocDBItemConstants.TABLE_CLUSTER_CALENDARS)
@SequenceGenerator(name = JocDBItemConstants.TABLE_CLUSTER_CALENDARS_SEQUENCE, sequenceName = JocDBItemConstants.TABLE_CLUSTER_CALENDARS_SEQUENCE, allocationSize = 1)
public class DBItemInventoryClusterCalendar implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int TITLE_MAX_LENGTH = 255;

    /** Primary key */
    private Long id;

    /** Others */
    private String schedulerId;
    private String name;
    private String baseName;
    private String directory;
    private String category;
    private String type;
    private String title;
    private Date created;
    private Date modified;
    private String configuration;

    
    public DBItemInventoryClusterCalendar() {
    }

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = JocDBItemConstants.TABLE_CLUSTER_CALENDARS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    public Long getId() {
        return this.id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = JocDBItemConstants.TABLE_CLUSTER_CALENDARS_SEQUENCE)
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
    
    @Column(name = "[NAME]", nullable = false)
    public void setName(String val) {
        this.name = val;
    }

    @Column(name = "[NAME]", nullable = false)
    public String getName() {
        return this.name;
    }

    @Column(name = "[BASENAME]", nullable = false)
    public void setBaseName(String val) {
        this.baseName = val;
    }

    @Column(name = "[BASENAME]", nullable = false)
    public String getBaseName() {
        return this.baseName;
    }
    
    @Column(name = "[DIRECTORY]", nullable = false)
    public void setDirectory(String val) {
        this.directory = val;
    }

    @Column(name = "[DIRECTORY]", nullable = false)
    public String getDirectory() {
        return this.directory;
    }
    
    @Column(name = "[CATEGORY]", nullable = true)
    public void setCategory(String val) {
        this.category = val;
    }

    @Column(name = "[CATEGORY]", nullable = true)
    public String getCategory() {
        return this.category;
    }
    
    @Column(name = "[TYPE]", nullable = false)
    public void setType(String val) {
        this.type = val;
    }

    @Column(name = "[TYPE]", nullable = false)
    public String getType() {
        return this.type;
    }

    @Column(name = "[TITLE]", nullable = true)
    public void setTitle(String val) {
        if (SOSString.isEmpty(val)) {
            val = null;
        } else {
            if (val.length() > TITLE_MAX_LENGTH) {
                val = val.substring(0, TITLE_MAX_LENGTH);
            }
        }
        this.title = val;
    }

    @Column(name = "[TITLE]", nullable = true)
    public String getTitle() {
        return this.title;
    }
    
    @Column(name = "[CONFIGURATION]", nullable = false)
    public void setConfiguration(String val) {
        this.configuration = val;
    }

    @Column(name = "[CONFIGURATION]", nullable = false)
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
    public void setModified(Date val) {
        this.modified = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[MODIFIED]", nullable = false)
    public Date getModified() {
        return this.modified;
    }

    @Override
    public int hashCode() {
        // always build on unique constraint
        return new HashCodeBuilder().append(schedulerId).append(name).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        // always compare on unique constraint
        if (other == this) {
            return true;
        }
        if (!(other instanceof DBItemInventoryClusterCalendar)) {
            return false;
        }
        DBItemInventoryClusterCalendar rhs = ((DBItemInventoryClusterCalendar) other);
        return new EqualsBuilder().append(schedulerId, rhs.schedulerId).append(name, rhs.name).isEquals();
    }

}