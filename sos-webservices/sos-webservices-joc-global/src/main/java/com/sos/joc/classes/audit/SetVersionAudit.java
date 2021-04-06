package com.sos.joc.classes.audit;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.publish.SetVersionFilter;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SetVersionAudit extends SetVersionFilter implements IAuditLog {

    @JsonIgnore
    private String controllerId;
    
    @JsonIgnore
    private String workflow;
    
    @JsonProperty("reason")
    private String reason;

    @JsonProperty("paths")
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

    public SetVersionAudit(SetVersionFilter filter, Set<String> paths, String reason) {
        setAuditParams(filter.getAuditLog());
        this.paths = paths;
        setVersion(filter.getVersion());
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
    public String getControllerId() {
        return controllerId;
    }
    
    @Override
    @JsonIgnore
    public Long getDepHistoryId() {
        return depHistoryId;
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

	@JsonProperty("reason")
    public String getReason() {
        return reason;
    }
    
	@JsonProperty("paths")
    public Set<String> getPaths() {
        return paths;
    }

}
