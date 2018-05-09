package com.sos.commons.db.joc;

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

@Entity
@Table(name = JocDBItemConstants.TABLE_CALENDARS)
@SequenceGenerator(
		name = JocDBItemConstants.TABLE_CALENDARS_SEQUENCE,
		sequenceName = JocDBItemConstants.TABLE_CALENDARS_SEQUENCE,
		allocationSize = 1)
public class DBItemCalendar implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int TITLE_MAX_LENGTH = 255;

    /** Primary key */
    private Long id;

    /** Others */
    private Long instanceId;
    private String name;
    private String baseName;
    private String directory;
    private String category;
    private String type;
    private String title;
    private Date created;
    private Date modified;
    private String configuration;

    
    public DBItemCalendar() {
    }

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = JocDBItemConstants.TABLE_CALENDARS_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return this.id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = JocDBItemConstants.TABLE_CALENDARS_SEQUENCE)
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
    
    @Column(name = "`NAME`", nullable = false)
    public void setName(String val) {
        this.name = val;
    }

    @Column(name = "`NAME`", nullable = false)
    public String getName() {
        return this.name;
    }

    @Column(name = "`BASENAME`", nullable = false)
    public void setBaseName(String val) {
        this.baseName = val;
    }

    @Column(name = "`BASENAME`", nullable = false)
    public String getBaseName() {
        return this.baseName;
    }
    
    @Column(name = "`DIRECTORY`", nullable = false)
    public void setDirectory(String val) {
        this.directory = val;
    }

    @Column(name = "`DIRECTORY`", nullable = false)
    public String getDirectory() {
        return this.directory;
    }
    
    @Column(name = "`CATEGORY`", nullable = true)
    public void setCategory(String val) {
        this.category = val;
    }

    @Column(name = "`CATEGORY`", nullable = true)
    public String getCategory() {
        return this.category;
    }
    
    @Column(name = "`TYPE`", nullable = false)
    public void setType(String val) {
        this.type = val;
    }

    @Column(name = "`TYPE`", nullable = false)
    public String getType() {
        return this.type;
    }

    @Column(name = "`TITLE`", nullable = true)
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

    @Column(name = "`TITLE`", nullable = true)
    public String getTitle() {
        return this.title;
    }
    
    @Column(name = "`CONFIGURATION`", nullable = false)
    public void setConfiguration(String val) {
        this.configuration = val;
    }

    @Column(name = "`CONFIGURATION`", nullable = false)
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
    public void setModified(Date val) {
        this.modified = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public Date getModified() {
        return this.modified;
    }

    @Override
    public int hashCode() {
        // always build on unique constraint
        return new HashCodeBuilder().append(instanceId).append(name).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        // always compare on unique constraint
        if (other == this) {
            return true;
        }
        if (!(other instanceof DBItemCalendar)) {
            return false;
        }
        DBItemCalendar rhs = ((DBItemCalendar) other);
        return new EqualsBuilder().append(instanceId, rhs.instanceId).append(name, rhs.name).isEquals();
    }

}