package com.sos.joc.classes.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.publish.ExportFilter;

public class ExportAudit extends ExportFilter implements IAuditLog {

    @JsonIgnore
    private String comment;

    @JsonIgnore
    private Integer timeSpent;
    
    @JsonIgnore
    private String ticketLink;
    
    private String reason;
    
    public ExportAudit(ExportFilter filter, String reason) {
        setAuditParams(filter.getAuditLog());
        this.reason = reason;
    }

    private void setAuditParams(AuditParams auditParams) {
        if (auditParams != null) {
            this.comment = auditParams.getComment();
            this.timeSpent = auditParams.getTimeSpent();
            this.ticketLink = auditParams.getTicketLink(); 
        }
    }

    public String getReason() {
        return reason;
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

	@JsonIgnore
	@Override
	public String getFolder() {
		return null;
	}

    @JsonIgnore
	@Override
	public String getJob() {
		return null;
	}

	@Override
	public String getWorkflow() {
		return null;
	}

    @JsonIgnore
	@Override
	public String getOrderId() {
		return null;
	}

    @JsonIgnore
	@Override
	public String getCalendar() {
		return null;
	}

    @JsonIgnore
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
