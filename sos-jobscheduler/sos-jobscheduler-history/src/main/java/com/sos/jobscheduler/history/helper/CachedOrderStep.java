package com.sos.jobscheduler.history.helper;

import java.util.Date;

import com.sos.jobscheduler.db.DBItemJobSchedulerOrderStepHistory;

public class CachedOrderStep {

    private final Long id;
    private final Long mainOrderHistoryId;
    private final Long orderHistoryId;
    private final String orderKey;
    private final String jobPath;
    private final String agentUri;
    private final String workflowPosition;
    private final Date endTime;

    public CachedOrderStep(DBItemJobSchedulerOrderStepHistory item) {
        id = item.getId();
        mainOrderHistoryId = item.getMainOrderHistoryId();
        orderHistoryId = item.getOrderHistoryId();
        orderKey = item.getOrderKey();
        jobPath = item.getJobPath();
        agentUri = item.getAgentUri();
        workflowPosition = item.getWorkflowPosition();
        endTime = item.getEndTime();
    }

    public Long getId() {
        return id;
    }

    public Long getMainOrderHistoryId() {
        return mainOrderHistoryId;
    }

    public Long getOrderHistoryId() {
        return orderHistoryId;
    }

    public String getOrderKey() {
        return orderKey;
    }

    public String getJobPath() {
        return jobPath;
    }

    public String getAgentUri() {
        return agentUri;
    }

    public String getWorkflowPosition() {
        return workflowPosition;
    }

    public Date getEndTime() {
        return endTime;
    }
}
