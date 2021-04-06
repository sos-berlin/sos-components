package com.sos.joc.classes.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SecurityAudit implements IAuditLog {

    @JsonIgnore
    private String comment;

    public SecurityAudit(String comment) {
        this.comment = comment;
    }

    @Override
    @JsonIgnore
    public String getComment() {
        return comment;
    }
    
    @Override
    @JsonIgnore
    public Integer getTimeSpent() {
        return null;
    }

    @Override
    @JsonIgnore
    public String getTicketLink() {
        return null;
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
    @JsonIgnore
    public String getWorkflow() {
        return null;
    }

    @Override
    @JsonIgnore
    public String getOrderId() {
        return null;
    }

    @Override
    @JsonIgnore
    public String getControllerId() {
        return null;
    }

    @Override
    @JsonIgnore
    public String getCalendar() {
        return null;
    }

    @Override
    @JsonIgnore
    public Long getDepHistoryId() {
        return null;
    }
}
