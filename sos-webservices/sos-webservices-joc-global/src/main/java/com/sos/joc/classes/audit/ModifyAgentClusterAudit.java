package com.sos.joc.classes.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.model.agent.StoreAgents;
import com.sos.joc.model.audit.AuditParams;


public class ModifyAgentClusterAudit extends StoreAgents implements IAuditLog {
    
    @JsonIgnore
    private String comment;
    
    @JsonIgnore
    private Integer timeSpent;
    
    @JsonIgnore
    private String ticketLink;

    public ModifyAgentClusterAudit(StoreAgents agentStoreParameter) {
        if (agentStoreParameter != null) {
            setAuditParams(agentStoreParameter.getAuditLog());
            setAgents(agentStoreParameter.getAgents());
            setControllerId(agentStoreParameter.getControllerId()); 
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
