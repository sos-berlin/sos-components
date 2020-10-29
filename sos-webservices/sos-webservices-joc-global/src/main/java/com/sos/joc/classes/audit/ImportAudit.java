package com.sos.joc.classes.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.publish.ImportFilter;

public class ImportAudit extends ImportFilter implements IAuditLog {

    @JsonIgnore
    private String comment;

    @JsonIgnore
    private Integer timeSpent;
    
    @JsonIgnore
    private String ticketLink;
    
    public ImportAudit(ImportFilter filter) {
        setAuditParams(filter.getAuditLog());
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
	public String getJobschedulerId() {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    @JsonIgnore
    public Long getDepHistoryId() {
        return null;
    }

}
