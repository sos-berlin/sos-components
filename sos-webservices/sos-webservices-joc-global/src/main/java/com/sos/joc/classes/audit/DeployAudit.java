package com.sos.joc.classes.audit;

import java.nio.file.Paths;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "reason",
    "commitId",
    "profile"
})
public class DeployAudit implements IAuditLog {

    @JsonProperty("controllerId")
    private String controllerId;
    
    @JsonProperty("reason")
    private String reason;
    
    @JsonProperty("commitId")
    private String commitId;
    
    @JsonProperty("profile")
    private String profile;
    
    @JsonIgnore
    private String comment;

    @JsonIgnore
    private Integer timeSpent;
    
    @JsonIgnore
    private String ticketLink;
    
    @JsonIgnore
    private String workflow;
    
    @JsonIgnore
    private String folder;
    
    @JsonIgnore
    private Long depHistoryId;

    public DeployAudit(AuditParams auditParams, String reason) {
        setAuditParams(auditParams);
        this.reason = reason;
    }

    public DeployAudit(AuditParams auditParams, String controllerId, String commitId, String reason, String profile) {
        setAuditParams(auditParams);
        this.reason = reason;
        this.commitId = commitId;
        this.controllerId = controllerId;
        this.profile = profile;
    }
    
    public DeployAudit(AuditParams auditParams, String controllerId, String commitId, Long depHistoryId, String path, String reason) {
        setAuditParams(auditParams);
        this.reason = reason;
        this.commitId = commitId;
        this.controllerId = controllerId;
        this.depHistoryId = depHistoryId;
        this.workflow = path;
        if (path != null) {
            this.folder = Paths.get(path).getParent().toString().replace('\\', '/');
        }
    }

    private void setAuditParams(AuditParams auditParams) {
        if (auditParams != null) {
            this.comment = auditParams.getComment();
            this.timeSpent = auditParams.getTimeSpent();
            this.ticketLink = auditParams.getTicketLink(); 
        }
    }

    @JsonProperty("commitId")
    public String getCommitId() {
        return commitId;
    }
    
    @Override
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }
    
    @JsonProperty("reason")
    public String getReason() {
        return reason;
    }

    @JsonProperty("profile")
    public String getProfile() {
        return profile;
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
    public String getJob() {
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
    public String getFolder() {
		return folder;
	}

	@Override
	@JsonIgnore
    public String getWorkflow() {
		return workflow;
	}

	@Override
	@JsonIgnore
    public Long getDepHistoryId() {
		return depHistoryId;
	}

}
