package com.sos.joc.classes.audit;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.publish.ImportDeployFilter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "commitId",
    "controllerId",
    "reason",
    "profile"
})
public class ImportDeployAudit /* extends ImportDeployFilter*/ implements IAuditLog {

    private String controllerId;
    
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

    public ImportDeployAudit(ImportDeployFilter filter, String commitId, String controllerId, String reason, String profile) {
        setAuditParams(filter.getAuditLog());
        this.commitId = commitId;
        this.controllerId = controllerId;
        this.reason = reason;
        this.profile = profile;
    }

    private void setAuditParams(AuditParams auditParams) {
        if (auditParams != null) {
            this.comment = auditParams.getComment();
            this.timeSpent = auditParams.getTimeSpent();
            this.ticketLink = auditParams.getTicketLink(); 
        }
    }

    public String getCommitId() {
        return commitId;
    }

    @Override
    public String getControllerId() {
        return controllerId;
    }
    
    public String getReason() {
        return reason;
    }
    
    public String getProfile() {
        return profile;
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
    public Long getDepHistoryId() {
        return depHistoryId;
    }

    @Override
    public String getFolder() {
        return null;
    }

    @Override
    public String getWorkflow() {
        return null;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("commitId", commitId).append("controllerId", controllerId).append("reason", reason)
        		.append("profile", profile).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(commitId).append(controllerId).append(reason).append(profile).toHashCode();
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
        return new EqualsBuilder().append(commitId, rhs.commitId).append(controllerId, rhs.controllerId).append(reason, rhs.reason)
        		.append(profile, rhs.profile).isEquals();
    }

}
