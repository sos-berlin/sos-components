package com.sos.joc.monitoring.model.bean;

import java.io.Serializable;

import org.jspecify.annotations.NonNull;

import com.sos.joc.cluster.bean.history.HistoryOrderStepBean;

public class NotificationObject implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long orderStepHistoryId;
    private final String orderStepJobName;
    private final String orderStepWorkflowPosition;
    private final long orderStepEventIdInSeconds;

    public NotificationObject(@NonNull final HistoryOrderStepBean orderStep) {
        this.orderStepHistoryId = orderStep.getHistoryId();
        this.orderStepJobName = orderStep.getJobName();
        this.orderStepWorkflowPosition = orderStep.getWorkflowPosition();
        this.orderStepEventIdInSeconds = orderStep.getEventIdInSeconds().longValue();
    }

    public String getOrderStepJobName() {
        return orderStepJobName;
    }

    public String getOrderStepWorkflowPosition() {
        return orderStepWorkflowPosition;
    }

    public Long getOrderStepHistoryId() {
        return orderStepHistoryId;
    }

    public long getOrderStepEventIdInSeconds() {
        return orderStepEventIdInSeconds;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("job=" + orderStepJobName);
        sb.append(", workflowPosition=" + orderStepWorkflowPosition);
        sb.append(", historyId=" + orderStepHistoryId);
        return sb.toString();
    }

}
