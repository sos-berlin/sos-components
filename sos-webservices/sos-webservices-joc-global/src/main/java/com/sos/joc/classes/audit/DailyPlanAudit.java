package com.sos.joc.classes.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilter;

public class DailyPlanAudit implements IAuditLog {

    @JsonIgnore
    private String folder;

    private String workflow;

    @JsonIgnore
    private String orderId;

    @JsonIgnore
    private String comment;

    @JsonIgnore
    private Integer timeSpent;

    @JsonIgnore
    private String ticketLink;

    // @JsonIgnore
    private String controllerId;

    public DailyPlanAudit(String controllerId, AuditParams auditParams) {
        if (auditParams != null) {
            setAuditParams(auditParams);
            this.controllerId = controllerId;
        }
    }

    private void setAuditParams(AuditParams auditParams) {
        if (auditParams != null) {
            this.comment = auditParams.getComment();
            this.timeSpent = auditParams.getTimeSpent();
            this.ticketLink = auditParams.getTicketLink();
        }
    }

    @Override
    @JsonIgnore
    public String getComment() {
        return comment;
    }

    @Override
    @JsonIgnore
    public Integer getTimeSpent() {
        return timeSpent;
    }

    @Override
    @JsonIgnore
    public String getTicketLink() {
        return ticketLink;
    }

    @Override
    @JsonIgnore
    public String getFolder() {
        return folder;
    }

    @Override
    @JsonIgnore
    public String getJob() {
        return null;
    }

    @Override
    public String getWorkflow() {
        return workflow;
    }

    @Override
    @JsonIgnore
    public String getCalendar() {
        return null;
    }

    @Override
    // @JsonIgnore
    public String getControllerId() {
        return controllerId;
    }

    @Override
    @JsonIgnore
    public Long getDepHistoryId() {
        return null;
    }

    @Override
    public String getOrderId() {
        return orderId;
    }

}
