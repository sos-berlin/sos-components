package com.sos.joc.classes.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.model.audit.AuditParams;

public class DailyPlanAudit implements IAuditLog {

    @JsonIgnore
    private String comment;

    @JsonIgnore
    private Integer timeSpent;

    @JsonIgnore
    private String ticketLink;

    private String controllerId;

    public DailyPlanAudit(String controllerId, AuditParams auditParams) {
        this.controllerId = controllerId;
        if (auditParams != null) {
            setAuditParams(auditParams);
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
        return null;
    }

    @Override
    @JsonIgnore
    public String getJob() {
        return null;
    }

    @Override
    public String getWorkflow() {
        return null;
    }

    @Override
    @JsonIgnore
    public String getCalendar() {
        return null;
    }

    @Override
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
        return null;
    }

}
