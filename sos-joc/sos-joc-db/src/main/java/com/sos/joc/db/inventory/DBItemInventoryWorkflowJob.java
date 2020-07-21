package com.sos.joc.db.inventory;

import java.beans.Transient;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.InventoryMeta.JobCriticality;
import com.sos.joc.db.inventory.InventoryMeta.JobLogLevel;
import com.sos.joc.db.inventory.InventoryMeta.JobRetunCodeMeaning;
import com.sos.joc.db.inventory.InventoryMeta.JobType;

@Entity
@Table(name = DBLayer.TABLE_INV_WORKFLOW_JOBS)
public class DBItemInventoryWorkflowJob extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[CID]", nullable = false)
    private Long cid;

    @Column(name = "[CID_WORKFLOW]", nullable = false)
    private Long cidWorkflow;

    @Column(name = "[CID_AGENT_CLUSTER]", nullable = false)
    private Long cidAgentCluster;

    @Column(name = "[CID_JOB_CLASS]", nullable = false)
    private Long cidJobClass;

    @Column(name = "[TYPE]", nullable = false)
    private Long type;

    @Column(name = "[CLASS_NAME]", nullable = true)
    private String className;

    @Column(name = "[LOG_LEVEL]", nullable = false)
    private Long logLevel;

    @Column(name = "[CRITICALITY]", nullable = false)
    private Long criticality;

    @Column(name = "[TASK_LIMIT]", nullable = false)
    private Long taskLimit;

    @Column(name = "[RETURN_CODE_MEANING]", nullable = false)
    private Long returnCodeMeaning;

    @Column(name = "[RETURN_CODE]", nullable = false)
    private String returnCode;

    public Long getCid() {
        return cid;
    }

    public void setCid(Long val) {
        cid = val;
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

    public Long getType() {
        return type;
    }

    @Transient
    public JobType getTypeAsEnum() {
        return JobType.fromValue(type);
    }

    public void setType(Long val) {
        type = val;
    }

    @Transient
    public void setType(JobType val) {
        setType(val == null ? null : val.value());
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String val) {
        className = val;
    }

    public Long getLogLevel() {
        return logLevel;
    }

    @Transient
    public JobLogLevel getLogLevelAsEnum() {
        return JobLogLevel.fromValue(type);
    }

    public void setLogLevel(Long val) {
        logLevel = val;
    }

    @Transient
    public void setLogLevel(JobLogLevel val) {
        setLogLevel(val == null ? null : val.value());
    }

    public Long getCriticality() {
        return criticality;
    }

    @Transient
    public JobCriticality getCriticalityAsEnum() {
        return JobCriticality.fromValue(type);
    }

    public void setCriticality(Long val) {
        criticality = val;
    }

    @Transient
    public void setCriticality(JobCriticality val) {
        setCriticality(val == null ? null : val.value());
    }

    public Long getTaskLimit() {
        return taskLimit;
    }

    public void setTaskLimit(Long val) {
        if (val == null) {
            val = 0L;
        }
        taskLimit = val;
    }

    public Long getReturnCodeMeaning() {
        return returnCodeMeaning;
    }

    @Transient
    public JobRetunCodeMeaning getReturnCodeMeaningAsEnum() {
        return JobRetunCodeMeaning.fromValue(type);
    }

    public void setReturnCodeMeaning(Long val) {
        returnCodeMeaning = val;
    }

    @Transient
    public void setReturnCodeMeaning(JobRetunCodeMeaning val) {
        setReturnCodeMeaning(val == null ? null : val.value());
    }

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String val) {
        returnCode = val;
    }
}
