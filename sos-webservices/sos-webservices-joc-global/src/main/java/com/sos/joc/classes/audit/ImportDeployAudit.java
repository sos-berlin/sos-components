package com.sos.joc.classes.audit;

import java.nio.file.Paths;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.publish.ArchiveFormat;
import com.sos.joc.model.publish.ImportDeployFilter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "workflow",
    "update",
    "delete",
    "reason",
    "commitId",
    "profile"
})
public class ImportDeployAudit /* extends ImportDeployFilter*/ implements IAuditLog {

    private String controllerId;
    
    private String workflowPath;

    private Boolean update;
    
    private Boolean delete;
    
    private String reason;
    
    private String commitId;
    
    private String profile;
    
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
    
    @JsonIgnore
    private String format;

    public ImportDeployAudit(ImportDeployFilter filter, String reason) {
        setAuditParams(filter.getAuditLog());
        this.reason = reason;
        this.controllerId = filter.getControllerId();
    }

    public ImportDeployAudit(ImportDeployFilter filter, boolean update, String controllerId, String commitId, Long depHistoryId, String path, String reason,
            String profile) {
        setAuditParams(filter.getAuditLog());
        this.reason = reason;
        this.commitId = commitId;
        this.controllerId = controllerId;
        this.depHistoryId = depHistoryId;
        this.workflowPath = path;
        this.folder = Paths.get(workflowPath).getParent().toString().replace('\\', '/');
        if (update) {
            this.update = true;
            this.delete = null;
        } else {
            this.update = null;
            this.delete = true;
        }
        this.profile = profile;
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

    public String getReason() {
        return reason;
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

    public Boolean getUpdate() {
        return update;
    }

    public Boolean getDelete() {
        return delete;
    }

    public String getCommitId() {
        return commitId;
    }

    public String getProfile() {
        return profile;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("commitId", commitId).append("workflowPath", workflowPath)
                .append("update", update).append("delete", delete).append("reason", reason).append("profile", profile).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(commitId).append(workflowPath).append(update).append(delete).append(reason).append(profile)
                .toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ImportDeployAudit) == false) {
            return false;
        }
        ImportDeployAudit rhs = ((ImportDeployAudit) other);
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(commitId, rhs.commitId).append(workflowPath, rhs.workflowPath)
                .append(update, rhs.update).append(delete, rhs.delete).append(reason, rhs.reason).append(profile, rhs.profile).isEquals();
    }

}
