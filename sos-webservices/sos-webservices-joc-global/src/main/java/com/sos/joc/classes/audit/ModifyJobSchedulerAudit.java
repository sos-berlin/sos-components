package com.sos.joc.classes.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.jobscheduler.RegisterParameters;
import com.sos.joc.model.jobscheduler.UrlParameter;


public class ModifyJobSchedulerAudit extends UrlParameter implements IAuditLog {
    
    @JsonIgnore
    private String comment;
    
    @JsonIgnore
    private Integer timeSpent;
    
    @JsonIgnore
    private String ticketLink;
    
    private String controllerId;

    public ModifyJobSchedulerAudit(UrlParameter uriParamSchema) {
        if (uriParamSchema != null) {
            setAuditParams(uriParamSchema.getAuditLog());
            setUrl(uriParamSchema.getUrl());
            setWithFailover(uriParamSchema.getWithFailover());
            setJobschedulerId(uriParamSchema.getJobschedulerId()); 
        }
    }
    
    public ModifyJobSchedulerAudit(RegisterParameters uriParamSchema) {
        if (uriParamSchema != null) {
            setAuditParams(uriParamSchema.getAuditLog());
            setUrl(null);
            setWithFailover(null);
            this.controllerId = uriParamSchema.getJobschedulerId();
            setJobschedulerId(null); 
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

    @Override
    public String getControllerId() {
        return controllerId;
    }
}
