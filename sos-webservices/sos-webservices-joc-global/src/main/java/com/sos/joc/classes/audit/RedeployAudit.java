package com.sos.joc.classes.audit;

import java.nio.file.Paths;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.publish.RedeployFilter;
//import com.sos.joc.model.publish.RedeployFilter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "workflow",
    "update",
    "delete"
})
public class RedeployAudit implements IAuditLog {

    private String controllerId;
    
    private String commitId;

    private String workflowPath;
    
    private String reason;
    
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

    public RedeployAudit(RedeployFilter filter, String reason) {
        setAuditParams(filter.getAuditLog());
        this.reason = reason;
    }

    public RedeployAudit(RedeployFilter filter, String controllerId, String commitId, Long depHistoryId, String path, String reason) {
        setAuditParams(filter.getAuditLog());
        this.reason = reason;
        this.commitId = commitId;
        this.controllerId = controllerId;
        this.depHistoryId = depHistoryId;
        this.workflowPath = path;
        this.folder = Paths.get(workflowPath).getParent().toString().replace('\\', '/');
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

    public String getReason() {
        return reason;
    }

    public String getCommitId() {
        return commitId;
    }

}
