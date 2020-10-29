package com.sos.joc.classes.audit;

import java.nio.file.Paths;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.publish.DeployFilter;

public class DeployAudit extends DeployFilter implements IAuditLog {

    @JsonIgnore
    private String comment;

    @JsonIgnore
    private Integer timeSpent;
    
    @JsonIgnore
    private String ticketLink;
    
    @JsonIgnore
    private Long depHistoryId;
    
    @JsonIgnore
    private String folder;

    private String controllerId;
    
    private String workflowPath;
    
    
    public DeployAudit(DeployFilter filter) {
        setAuditParams(filter.getAuditLog());
    }

    public DeployAudit(DeployFilter filter, String controllerId, String workflowPath, Long depHistoryId) {
        setAuditParams(filter.getAuditLog());
        this.setControllers(null);
        this.setUpdate(null);
        this.setDelete(null);
        this.controllerId = controllerId;
        this.workflowPath = workflowPath;
        this.folder = Paths.get(workflowPath).getParent().toString().replace('\\', '/');
        this.depHistoryId = depHistoryId;
    }

    private void setAuditParams(AuditParams auditParams) {
        if (auditParams != null) {
            this.comment = auditParams.getComment();
            this.timeSpent = auditParams.getTimeSpent();
            this.ticketLink = auditParams.getTicketLink(); 
        }
    }

    @Override
    public String getFolder() {
        return folder;
    }

    @Override
    public String getWorkflow() {
        return workflowPath;
    }

    @Override
    public String getControllerId() {
        return controllerId;
    }
    
    @Override
    public Long getDepHistoryId() {
        return depHistoryId;
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
	public String getJob() {
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

}
