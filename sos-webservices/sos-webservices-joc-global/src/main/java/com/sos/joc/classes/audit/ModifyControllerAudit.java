package com.sos.joc.classes.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.controller.RegisterParameters;
import com.sos.joc.model.controller.UrlParameter;


public class ModifyControllerAudit extends UrlParameter implements IAuditLog {
    
    @JsonIgnore
    private String comment;
    
    @JsonIgnore
    private Integer timeSpent;
    
    @JsonIgnore
    private String ticketLink;
    
    public ModifyControllerAudit(UrlParameter uriParamSchema) {
        if (uriParamSchema != null) {
            setAuditParams(uriParamSchema.getAuditLog());
            setUrl(uriParamSchema.getUrl());
            setWithSwitchover(uriParamSchema.getWithSwitchover());
            setControllerId(uriParamSchema.getControllerId()); 
        }
    }
    
    public ModifyControllerAudit(RegisterParameters uriParamSchema) {
        if (uriParamSchema != null) {
            setAuditParams(uriParamSchema.getAuditLog());
            setUrl(null);
            setWithSwitchover(null);
            setControllerId(uriParamSchema.getControllerId()); 
        }
    }
    
    public ModifyControllerAudit(String controllerId, AuditParams auditLog) {
        setAuditParams(auditLog);
        setUrl(null);
        setWithSwitchover(null);
        setControllerId(controllerId);
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
