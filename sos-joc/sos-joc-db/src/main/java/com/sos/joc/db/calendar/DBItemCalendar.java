package com.sos.joc.db.calendar;

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
import javax.persistence.UniqueConstraint;

import com.sos.commons.util.SOSString;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_CALENDARS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[JOBSCHEDULER_ID]", "[NAME]" }) })
@SequenceGenerator(name = DBLayer.TABLE_CALENDARS_SEQUENCE, sequenceName = DBLayer.TABLE_CALENDARS_SEQUENCE, allocationSize = 1)
public class DBItemCalendar extends DBItem {

    private static final long serialVersionUID = 1L;

    private static final int TITLE_MAX_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_CALENDARS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[JOBSCHEDULER_ID]", nullable = false)
    private String schedulerId;

    @Column(name = "[NAME]", nullable = false)
    private String name;

    @Column(name = "[BASENAME]", nullable = false)
    private String baseName;

    @Column(name = "[DIRECTORY]", nullable = false)
    private String directory;

    @Column(name = "[CATEGORY]", nullable = false)
    private String category;

    @Column(name = "[TYPE]", nullable = false)
    private String type;

    @Column(name = "[TITLE]", nullable = false)
    private String title;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[MODIFIED]", nullable = false)
    private Date modified;

    @Column(name = "[CONFIGURATION]", nullable = false)
    private String configuration;

    public DBItemCalendar() {
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

    public void setName(String val) {
        this.name = val;
    }

    public String getName() {
        return this.name;
    }

    public void setBaseName(String val) {
        this.baseName = val;
    }

    public String getBaseName() {
        return this.baseName;
    }

    public void setDirectory(String val) {
        this.directory = val;
    }

    public String getDirectory() {
        return this.directory;
    }

    public void setCategory(String val) {
        this.category = val;
    }

    public String getCategory() {
        return this.category;
    }

    public void setType(String val) {
        this.type = val;
    }

    public String getType() {
        return this.type;
    }

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

    public String getTitle() {
        return this.title;
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

    public void setModified(Date val) {
        this.modified = val;
    }

    public Date getModified() {
        return this.modified;
    }

}