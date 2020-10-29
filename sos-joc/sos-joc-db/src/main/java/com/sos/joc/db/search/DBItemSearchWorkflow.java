package com.sos.joc.db.search;

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

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import com.sos.commons.hibernate.type.SOSHibernateJsonType;
import com.sos.commons.util.SOSString;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_SEARCH_WORKFLOWS)
@SequenceGenerator(name = DBLayer.TABLE_SEARCH_WORKFLOWS_SEQUENCE, sequenceName = DBLayer.TABLE_SEARCH_WORKFLOWS_SEQUENCE, allocationSize = 1)
@TypeDefs({ @TypeDef(name = SOSHibernateJsonType.TYPE_NAME, typeClass = SOSHibernateJsonType.class) })
public class DBItemSearchWorkflow extends DBItem {

    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_JSON_VALUE = "{}";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_SEARCH_WORKFLOWS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[INV_CID]", nullable = false) /* INV_CONFIGURATIONS.ID */
    private Long inventoryConfigurationId;

    @Column(name = "[DEPLOYED]", nullable = false)
    @Type(type = "numeric_boolean")
    private boolean deployed;

    @Column(name = "[CONTENT_HASH]", nullable = false)
    private String contentHash;

    @Column(name = "[JOBS_COUNT]", nullable = false)
    private Integer jobsCount;

    @Column(name = "[JOBS]", nullable = false)
    @Type(type = SOSHibernateJsonType.TYPE_NAME)
    private String jobs;

    @Column(name = "[JOBS_ARGS]", nullable = false)
    @Type(type = SOSHibernateJsonType.TYPE_NAME)
    private String jobsArgs;

    @Column(name = "[JOBS_SCRIPTS]", nullable = false)
    @Type(type = SOSHibernateJsonType.TYPE_NAME)
    private String jobsScripts;

    @Column(name = "[INSTRUCTIONS]", nullable = false)
    @Type(type = SOSHibernateJsonType.TYPE_NAME)
    private String instructions;

    @Column(name = "[INSTRUCTIONS_ARGS]", nullable = false)
    @Type(type = SOSHibernateJsonType.TYPE_NAME)
    private String instructionsArgs;

    @Column(name = "[CREATED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name = "[MODIFIED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public Long getInventoryConfigurationId() {
        return inventoryConfigurationId;
    }

    public void setInventoryConfigurationId(Long val) {
        inventoryConfigurationId = val;
    }

    public boolean getDeployed() {
        return deployed;
    }

    public void setDeployed(boolean val) {
        deployed = val;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String val) {
        contentHash = val;
    }

    public Integer getJobsCount() {
        return jobsCount;
    }

    public void setJobsCount(Integer val) {
        jobsCount = val;
    }

    public String getJobs() {
        return jobs;
    }

    public void setJobs(String val) {
        if (SOSString.isEmpty(val)) {
            val = DEFAULT_JSON_VALUE;
        }
        jobs = val;
    }

    public String getJobsArgs() {
        return jobsArgs;
    }

    public void setJobsArgs(String val) {
        if (SOSString.isEmpty(val)) {
            val = DEFAULT_JSON_VALUE;
        }
        jobsArgs = val;
    }

    public String getJobsScripts() {
        return jobsScripts;
    }

    public void setJobsScripts(String val) {
        if (SOSString.isEmpty(val)) {
            val = DEFAULT_JSON_VALUE;
        }
        jobsScripts = val;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String val) {
        if (SOSString.isEmpty(val)) {
            val = DEFAULT_JSON_VALUE;
        }
        instructions = val;
    }

    public String getInstructionsArgs() {
        return instructionsArgs;
    }

    public void setInstructionsArgs(String val) {
        if (SOSString.isEmpty(val)) {
            val = DEFAULT_JSON_VALUE;
        }
        instructionsArgs = val;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date val) {
        created = val;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date val) {
        modified = val;
    }
}
