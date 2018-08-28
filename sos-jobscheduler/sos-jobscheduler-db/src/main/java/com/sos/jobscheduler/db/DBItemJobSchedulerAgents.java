package com.sos.jobscheduler.db;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = DBLayer.TABLE_JOBSCHEDULER_AGENTS)
@SequenceGenerator(name = DBLayer.TABLE_JOBSCHEDULER_AGENTS_SEQUENCE, sequenceName = DBLayer.TABLE_JOBSCHEDULER_AGENTS_SEQUENCE, allocationSize = 1)
public class DBItemJobSchedulerAgents implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Primary key */
    private Long id;// db id

    private String uri;
    private String timezone;
    private Date startTime;

    private Date created;
    private Date modified;

    public DBItemJobSchedulerAgents() {
    }

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_JOBSCHEDULER_AGENTS_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_JOBSCHEDULER_AGENTS_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public void setId(Long val) {
        id = val;
    }

    @Column(name = "`URI`", nullable = false)
    public String getUri() {
        return uri;
    }

    @Column(name = "`URI`", nullable = false)
    public void setUri(String val) {
        uri = val;
    }

    @Column(name = "`TIMEZONE`", nullable = false)
    public String getTimezone() {
        return timezone;
    }

    @Column(name = "`TIMEZONE`", nullable = false)
    public void setTimezone(String val) {
        timezone = val;
    }

    @Column(name = "`START_TIME`", nullable = false)
    public Date getStartTime() {
        return startTime;
    }

    @Column(name = "`START_TIME`", nullable = false)
    public void setStartTime(Date val) {
        startTime = val;
    }

    @Column(name = "`CREATED`", nullable = false)
    public void setCreated(Date val) {
        created = val;
    }

    @Column(name = "`CREATED`", nullable = false)
    public Date getCreated() {
        return created;
    }

    @Column(name = "`MODIFIED`", nullable = false)
    public void setModified(Date val) {
        modified = val;
    }

    @Column(name = "`MODIFIED`", nullable = false)
    public Date getModified() {
        return modified;
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof DBItemJobSchedulerAgents)) {
            return false;
        }
        DBItemJobSchedulerAgents item = (DBItemJobSchedulerAgents) o;
        if (!getId().equals(item.getId())) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return getId() == null ? new Long(0).hashCode() : getId().hashCode();
    }
}
