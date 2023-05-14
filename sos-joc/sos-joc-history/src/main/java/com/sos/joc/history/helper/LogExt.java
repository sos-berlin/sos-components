package com.sos.joc.history.helper;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LogExt implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Type {
        order_all, order_history, order, task
    }

    private final Type type;
    private final String file;// String because of serialize
    private final Long logId;
    private final String workflowName;
    private final String orderId;
    private final Long orderHistoryId;
    private final Long taskHistoryId;
    private final String jobLabel;

    private boolean deserialized;
    private boolean processed;

    public LogExt(Type type, Path file, Long logId, String workflowName, String orderId, Long orderHistoryId, Long taskHistoryId, String jobLabel) {
        this.type = type;
        this.file = file.toString();
        this.logId = logId;
        this.workflowName = workflowName;
        this.orderId = orderId;
        this.orderHistoryId = orderHistoryId;
        this.taskHistoryId = taskHistoryId;
        this.jobLabel = jobLabel;
    }

    public Type getType() {
        return type;
    }

    public boolean isTask() {
        return type.equals(Type.task);
    }

    public boolean isOrderAll() {
        return type.equals(Type.order_all);
    }

    public static boolean isOrderHistory(Type type) {
        return type.equals(Type.order_history);
    }

    public Path getFile() {
        return Paths.get(file);
    }

    public Long getLogId() {
        return logId;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public String getOrderId() {
        return orderId;
    }

    public Long getOrderHistoryId() {
        return orderHistoryId;
    }

    public Long getTaskHistoryId() {
        return taskHistoryId;
    }

    public String getJobLabel() {
        return jobLabel;
    }

    public boolean isDeserialized() {
        return deserialized;
    }

    public void setDeserialized() {
        deserialized = true;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed() {
        processed = true;
    }
}
