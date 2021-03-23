package com.sos.yade.engine.result;

import com.sos.yade.commons.result.YadeTransferResult;

public class YadeTransferResultJS1X extends YadeTransferResult {

    private static final long serialVersionUID = -8877189880331791745L;

    private String mandator;
    private String jobschedulerId;
    private String job;
    private String jobChain;
    private String jobChainNode;
    private Long taskId;
    private Long orderId;
    private Long auditLogId;
    private Boolean hasIntervention;
    private Long parentTransferId;

    public String getMandator() {
        return mandator;
    }

    public void setMandator(String val) {
        mandator = val;
    }

    public String getJobschedulerId() {
        return jobschedulerId;
    }

    public void setJobschedulerId(String val) {
        jobschedulerId = val;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String val) {
        job = val;
    }

    public String getJobChain() {
        return jobChain;
    }

    public void setJobChain(String val) {
        jobChain = val;
    }

    public String getJobChainNode() {
        return jobChainNode;
    }

    public void setJobChainNode(String val) {
        jobChainNode = val;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long val) {
        taskId = val;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long val) {
        orderId = val;
    }

    public Long getAuditLogId() {
        return auditLogId;
    }

    public void setAuditLogId(Long val) {
        auditLogId = val;
    }

    public Boolean getHasIntervention() {
        return hasIntervention;
    }

    public void setHasIntervention(Boolean val) {
        hasIntervention = val;
    }

    public Long getParentTransferId() {
        return parentTransferId;
    }

    public void setParentTransferId(Long val) {
        parentTransferId = val;
    }

}
