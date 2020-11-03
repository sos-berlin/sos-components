package com.sos.joc.classes.audit;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.publish.SetVersionFilter;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SetVersionAudit extends SetVersionFilter implements IAuditLog {

    @JsonIgnore
    private String controllerId;
    
    @JsonIgnore
    private String workflow;
    
    private String reason;

    private String version;

    private Set<String> paths;
    
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

    public SetVersionAudit(SetVersionFilter filter, String reason) {
        setAuditParams(filter.getAuditLog());
        this.reason = reason;
    }

    public SetVersionAudit(SetVersionFilter filter, Set<String> paths, String reason) {
        setAuditParams(filter.getAuditLog());
        this.paths = paths;
        this.version = filter.getVersion();
        this.reason = reason;
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
        return workflow;
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

    public String getVersion() {
        return version;
    }
    
    public Set<String> getPaths() {
        return paths;
    }

}
