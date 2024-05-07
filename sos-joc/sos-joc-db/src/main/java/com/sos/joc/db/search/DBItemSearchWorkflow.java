package com.sos.joc.db.search;

import java.util.Date;

import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.Type;
import org.hibernate.type.NumericBooleanConverter;

import com.sos.commons.hibernate.type.SOSHibernateJsonType;
import com.sos.commons.util.SOSString;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = DBLayer.TABLE_SEARCH_WORKFLOWS)
@SequenceGenerator(name = DBLayer.TABLE_SEARCH_WORKFLOWS_SEQUENCE, sequenceName = DBLayer.TABLE_SEARCH_WORKFLOWS_SEQUENCE, allocationSize = 1)
//@TypeDefs({ @TypeDef(name = SOSHibernateJsonType.TYPE_NAME, typeClass = SOSHibernateJsonType.class) })
//TODO 6.4.5.Final ? is @TypeDef still necessary?
//@Convert(attributeName = "sos_json", converter = SOSHibernateJsonType.class)
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
    @Convert(converter = NumericBooleanConverter.class)
    private boolean deployed;

    @Column(name = "[CONTENT_HASH]", nullable = false)
    private String contentHash;

    @Column(name = "[JOBS_COUNT]", nullable = false)
    private Integer jobsCount;

    @Column(name = "[JOBS]", nullable = false)
    @Type(value = SOSHibernateJsonType.class)
    @ColumnTransformer(write = SOSHibernateJsonType.COLUMN_TRANSFORMER_WRITE_DEFAULT)
    private String jobs;

    @Column(name = "[ARGS]", nullable = false)
    @Type(value = SOSHibernateJsonType.class)
    @ColumnTransformer(write = SOSHibernateJsonType.COLUMN_TRANSFORMER_WRITE_DEFAULT)
    private String args;

    @Column(name = "[JOBS_SCRIPTS]", nullable = false)
    @Type(value = SOSHibernateJsonType.class)
    @ColumnTransformer(write = SOSHibernateJsonType.COLUMN_TRANSFORMER_WRITE_DEFAULT)
    private String jobsScripts;

    @Column(name = "[INSTRUCTIONS]", nullable = false)
    @Type(value = SOSHibernateJsonType.class)
    @ColumnTransformer(write = SOSHibernateJsonType.COLUMN_TRANSFORMER_WRITE_DEFAULT)
    private String instructions;

    @Column(name = "[INSTRUCTIONS_ARGS]", nullable = false)
    @Type(value = SOSHibernateJsonType.class)
    @ColumnTransformer(write = SOSHibernateJsonType.COLUMN_TRANSFORMER_WRITE_DEFAULT)
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

    public String getArgs() {
        return args;
    }

    public void setArgs(String val) {
        if (SOSString.isEmpty(val)) {
            val = DEFAULT_JSON_VALUE;
        }
        args = val;
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
