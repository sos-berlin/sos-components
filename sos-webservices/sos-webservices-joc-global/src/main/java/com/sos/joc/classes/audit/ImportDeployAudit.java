package com.sos.joc.classes.audit;

import java.nio.file.Paths;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.publish.ImportDeployFilter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "workflow",
    "update",
    "delete"
})
public class ImportDeployAudit extends ImportDeployFilter implements IAuditLog {

    private String controllerId;
    
    private String workflowPath;

    private Boolean update;
    
    private Boolean delete;
    
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

    public ImportDeployAudit(ImportDeployFilter filter) {
        setAuditParams(filter.getAuditLog());
    }

    public ImportDeployAudit(ImportDeployFilter filter, String controllerId, String workflowPath, Long depHistoryId, boolean update) {
        setAuditParams(filter.getAuditLog());
        this.controllerId = controllerId;
        this.workflowPath = workflowPath;
        this.folder = Paths.get(workflowPath).getParent().toString().replace('\\', '/');
        this.depHistoryId = depHistoryId;
        if (update) {
            this.update = true;
            this.delete = null;
        } else {
            this.update = null;
            this.delete = true;
        }
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

    public Boolean getUpdate() {
        return update;
    }

    public Boolean getDelete() {
        return delete;
    }

}
