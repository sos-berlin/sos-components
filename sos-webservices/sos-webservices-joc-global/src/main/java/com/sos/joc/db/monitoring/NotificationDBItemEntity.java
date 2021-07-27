package com.sos.joc.db.monitoring;

import java.util.Date;

public class NotificationDBItemEntity {

    private Long id;
    private Integer type;
    private String notificationId;
    private boolean hasMonitors;
    private Long recoveredNotificationId;
    private Date created;

    private Long orderHistoryId;
    private String controllerId;
    private String orderId;
    private String workflowPath;
    private String orderErrorText;

    private Long orderStepHistoryId;
    private String orderStepJobName;
    private String orderStepJobLabel;
    private String orderStepJobTitle;
    private Integer orderStepJobCriticality;
    private Date orderStepStartTime;
    private Date orderStepEndTime;
    private String orderStepWorkflowPosition;
    private Integer orderStepSeverity;
    private String orderStepAgentUri;
    private Integer orderStepReturnCode;
    private Boolean orderStepError;
    private String orderStepErrorText;
    private Integer orderStepWarn;
    private String orderStepWarnText;

    private String acknowledgementAccount;
    private String acknowledgementComment;
    private Date acknowledgementCreated;

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer val) {
        type = val;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String val) {
        notificationId = val;
    }

    public boolean getHasMonitors() {
        return hasMonitors;
    }

    public void setHasMonitors(boolean val) {
        hasMonitors = val;
    }

    public Long getRecoveredNotificationId() {
        return recoveredNotificationId;
    }

    public void setRecoveredNotificationId(Long val) {
        recoveredNotificationId = val;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date val) {
        created = val;
    }

    public Long getOrderHistoryId() {
        return orderHistoryId;
    }

    public void setOrderHistoryId(Long val) {
        orderHistoryId = val;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String val) {
        controllerId = val;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String val) {
        orderId = val;
    }

    public String getWorkflowPath() {
        return workflowPath;
    }

    public void setWorkflowPath(String val) {
        workflowPath = val;
    }

    public String getOrderErrorText() {
        return orderErrorText;
    }

    public void setOrderErrorText(String val) {
        orderErrorText = val;
    }

    public Long getOrderStepHistoryId() {
        return orderStepHistoryId;
    }

    public void setOrderStepHistoryId(Long val) {
        orderStepHistoryId = val;
    }

    public String getOrderStepJobName() {
        return orderStepJobName;
    }

    public void setOrderStepJobName(String val) {
        orderStepJobName = val;
    }

    public String getOrderStepJobLabel() {
        return orderStepJobLabel;
    }

    public void setOrderStepJobLabel(String val) {
        orderStepJobLabel = val;
    }

    public String getOrderStepJobTitle() {
        return orderStepJobTitle;
    }

    public void setOrderStepJobTitle(String val) {
        orderStepJobTitle = val;
    }

    public Integer getOrderStepJobCriticality() {
        return orderStepJobCriticality;
    }

    public void setOrderStepJobCriticality(Integer val) {
        orderStepJobCriticality = val;
    }

    public Date getOrderStepStartTime() {
        return orderStepStartTime;
    }

    public void setOrderStepStartTime(Date val) {
        orderStepStartTime = val;
    }

    public Date getOrderStepEndTime() {
        return orderStepEndTime;
    }

    public void setOrderStepEndTime(Date val) {
        orderStepEndTime = val;
    }

    public String getOrderStepWorkflowPosition() {
        return orderStepWorkflowPosition;
    }

    public void setOrderStepWorkflowPosition(String val) {
        orderStepWorkflowPosition = val;
    }

    public Integer getOrderStepSeverity() {
        return orderStepSeverity;
    }

    public void setOrderStepSeverity(Integer val) {
        orderStepSeverity = val;
    }

    public String getOrderStepAgentUri() {
        return orderStepAgentUri;
    }

    public void setOrderStepAgentUri(String val) {
        orderStepAgentUri = val;
    }

    public Integer getOrderStepReturnCode() {
        return orderStepReturnCode;
    }

    public void setOrderStepReturnCode(Integer val) {
        orderStepReturnCode = val;
    }

    public Boolean isOrderStepError() {
        return orderStepError;
    }

    public void setOrderStepError(Boolean val) {
        orderStepError = val;
    }

    public String getOrderStepErrorText() {
        return orderStepErrorText;
    }

    public void setOrderStepErrorText(String val) {
        orderStepErrorText = val;
    }

    public Integer getOrderStepWarn() {
        return orderStepWarn;
    }

    public void setOrderStepWarn(Integer val) {
        orderStepWarn = val;
    }

    public String getOrderStepWarnText() {
        return orderStepWarnText;
    }

    public void setOrderStepWarnText(String val) {
        orderStepWarnText = val;
    }

    public String getAcknowledgementAccount() {
        return acknowledgementAccount;
    }

    public void setAcknowledgementAccount(String val) {
        acknowledgementAccount = val;
    }

    public String getAcknowledgementComment() {
        return acknowledgementComment;
    }

    public void setAcknowledgementComment(String val) {
        acknowledgementComment = val;
    }

    public Date getAcknowledgementCreated() {
        return acknowledgementCreated;
    }

    public void setAcknowledgementCreated(Date val) {
        acknowledgementCreated = val;
    }
}
