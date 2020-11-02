package com.sos.joc.classes.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.model.audit.AuditParams;

public class GenerateKeyAudit implements IAuditLog {

    @JsonIgnore
    private String comment;

    @JsonIgnore
    private Integer timeSpent;
    
    @JsonIgnore
    private String ticketLink;
    
    public GenerateKeyAudit() {
        setAuditParams(new AuditParams());
    }

    public GenerateKeyAudit(String comment) {
        setAuditParams(new AuditParams());
        this.comment = comment;
    }

    private void setAuditParams(AuditParams auditParams) {
        if (auditParams != null) {
            this.comment = auditParams.getComment();
            this.timeSpent = auditParams.getTimeSpent();
            this.ticketLink = auditParams.getTicketLink(); 
        }
    }

	@Override
	public String getComment() {
		return comment;
	}

	@Override
	public Integer getTimeSpent() {
		return timeSpent;
	}

	@Override
	public String getTicketLink() {
		return ticketLink;
	}

	@Override
	public String getFolder() {
		return null;
	}

	@Override
	public String getJob() {
		return null;
	}

	@Override
	public String getWorkflow() {
		return null;
	}

	@Override
	public String getOrderId() {
		return null;
	}

	@Override
	public String getCalendar() {
		return null;
	}

	@Override
	public String getControllerId() {
		return null;
	}

    @Override
    @JsonIgnore
    public Long getDepHistoryId() {
        return null;
    }

}
