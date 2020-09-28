package com.sos.joc.db.inventory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.inventory.common.JobCriticality;
import com.sos.joc.model.inventory.common.JobLogLevel;
import com.sos.joc.model.inventory.common.JobReturnCodeMeaning;
import com.sos.joc.model.inventory.common.JobType;

@Entity
@Table(name = DBLayer.TABLE_INV_WORKFLOW_JOBS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CID_WORKFLOW]", "[NAME]" }) })
@SequenceGenerator(name = DBLayer.TABLE_INV_WORKFLOW_JOBS_SEQUENCE, sequenceName = DBLayer.TABLE_INV_WORKFLOW_JOBS_SEQUENCE, allocationSize = 1)

public class DBItemInventoryWorkflowJob extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INV_WORKFLOW_JOBS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[CID_WORKFLOW]", nullable = false)
    private Long cidWorkflow;

    @Column(name = "[CID_AGENT_CLUSTER]", nullable = false)
    private Long cidAgentCluster;

    @Column(name = "[CID_JOB_CLASS]", nullable = false)
    private Long cidJobClass;

    @Column(name = "[TYPE]", nullable = false)
    private Integer type;

    @Column(name = "[NAME]", nullable = false)
    private String name;

    @Column(name = "[TITLE]", nullable = true)
    private String title;

    @Column(name = "[CLASS_NAME]", nullable = true)
    private String className;

    @Column(name = "[LOG_LEVEL]", nullable = false)
    private Integer logLevel;

    @Column(name = "[CRITICALITY]", nullable = false)
    private Integer criticality;

    @Column(name = "[TASK_LIMIT]", nullable = false)
    private Integer taskLimit;

    @Column(name = "[RETURN_CODE_MEANING]", nullable = false)
    private Integer returnCodeMeaning;

    @Column(name = "[RETURN_CODE]", nullable = false)
    private String returnCode;

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public Long getCidWorkflow() {
        return cidWorkflow;
    }

    public void setCidWorkflow(Long val) {
        cidWorkflow = val;
    }

    public Long getCidAgentCluster() {
        return cidAgentCluster;
    }

    public void setCidAgentCluster(Long val) {
        cidAgentCluster = val;
    }

    public Long getCidJobClass() {
        return cidJobClass;
    }

    public void setCidJobClass(Long val) {
        cidJobClass = val;
    }

    public Integer getType() {
        return type;
    }

    @Transient
    public JobType getTypeAsEnum() {
        return JobType.fromValue(type);
    }

    public void setType(Integer val) {
        type = val;
    }

    @Transient
    public void setType(JobType val) {
        setType(val == null ? null : val.intValue());
    }

    public String getName() {
        return name;
    }

    public void setName(String val) {
        name = val;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String val) {
        title = val;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String val) {
        className = val;
    }

    public Integer getLogLevel() {
        return logLevel;
    }

    @Transient
    public JobLogLevel getLogLevelAsEnum() {
        return JobLogLevel.fromValue(type);
    }

    public void setLogLevel(Integer val) {
        logLevel = val;
    }

    @Transient
    public void setLogLevel(JobLogLevel val) {
        setLogLevel(val == null ? null : val.intValue());
    }

    public Integer getCriticality() {
        return criticality;
    }

    @Transient
    public JobCriticality getCriticalityAsEnum() {
        return JobCriticality.fromValue(criticality);
    }

    public void setCriticality(Integer val) {
        criticality = val;
    }

    @Transient
    public void setCriticality(JobCriticality val) {
        setCriticality(val == null ? JobCriticality.NORMAL.intValue() : val.intValue());
    }

    public Integer getTaskLimit() {
        return taskLimit;
    }

    public void setTaskLimit(Integer val) {
        if (val == null) {
            val = 0;
        }
        taskLimit = val;
    }

    public Integer getReturnCodeMeaning() {
        return returnCodeMeaning;
    }

    @Transient
    public JobReturnCodeMeaning getReturnCodeMeaningAsEnum() {
        return JobReturnCodeMeaning.fromValue(type);
    }

    public void setReturnCodeMeaning(Integer val) {
        returnCodeMeaning = val;
    }

    @Transient
    public void setReturnCodeMeaning(JobReturnCodeMeaning val) {
        setReturnCodeMeaning(val == null ? null : val.intValue());
    }

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String val) {
        returnCode = val;
    }
}
