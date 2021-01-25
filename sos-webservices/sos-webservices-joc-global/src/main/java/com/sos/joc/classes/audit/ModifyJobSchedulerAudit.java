package com.sos.joc.classes.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.controller.RegisterParameters;
import com.sos.joc.model.controller.UrlParameter;


public class ModifyJobSchedulerAudit extends UrlParameter implements IAuditLog {
    
    @JsonIgnore
    private String comment;
    
    @JsonIgnore
    private Integer timeSpent;
    
    @JsonIgnore
    private String ticketLink;
    
    public ModifyJobSchedulerAudit(UrlParameter uriParamSchema) {
        if (uriParamSchema != null) {
            setAuditParams(uriParamSchema.getAuditLog());
            setUrl(uriParamSchema.getUrl());
            setWithFailover(uriParamSchema.getWithFailover());
            setControllerId(uriParamSchema.getControllerId()); 
        }
    }
    
    public ModifyJobSchedulerAudit(RegisterParameters uriParamSchema) {
        if (uriParamSchema != null) {
            setAuditParams(uriParamSchema.getAuditLog());
            setUrl(null);
            setWithFailover(null);
            setControllerId(uriParamSchema.getControllerId()); 
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
    public String getCalendar() {
        return null;
    }

    @Override
    @JsonIgnore
    public Long getDepHistoryId() {
        return null;
    }
}
