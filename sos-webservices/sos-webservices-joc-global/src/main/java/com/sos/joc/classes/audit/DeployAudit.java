package com.sos.joc.classes.audit;

import java.nio.file.Paths;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.publish.DeployFilter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "reason",
    "commitId",
    "profile"
})
public class DeployAudit implements IAuditLog {

    private String controllerId;
    
    private String commitId;

    private String reason;
    
    @JsonIgnore
    private String comment;

    @JsonIgnore
    private Integer timeSpent;
    
    @JsonIgnore
    private String ticketLink;
    
    private String profile;

    public DeployAudit(DeployFilter filter, String reason) {
        setAuditParams(filter.getAuditLog());
        this.reason = reason;
    }

    public DeployAudit(DeployFilter filter, String controllerId, String commitId, String reason, String profile) {
        setAuditParams(filter.getAuditLog());
        this.reason = reason;
        this.commitId = commitId;
        this.controllerId = controllerId;
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
	public String getFolder() {
		return null;
	}

	@Override
	public String getWorkflow() {
		return null;
	}

	@Override
	public Long getDepHistoryId() {
		return null;
	}

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("commitId", commitId).append("reason", reason)
        		.append("profile", profile).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(commitId).append(reason).append(profile)
                .toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeployAudit) == false) {
            return false;
        }
        DeployAudit rhs = ((DeployAudit) other);
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(commitId, rhs.commitId).append(reason, rhs.reason)
        		.append(profile, rhs.profile).isEquals();
    }

}
