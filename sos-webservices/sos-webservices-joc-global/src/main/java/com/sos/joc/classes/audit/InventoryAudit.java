package com.sos.joc.classes.audit;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class InventoryAudit implements IAuditLog {

    @JsonProperty("objectType")
    private ConfigurationType objectType;

    @JsonProperty("path")
    private String path;
    
    @JsonIgnore
    private String workflow;
    
    @JsonIgnore
    private String job;
    
    @JsonIgnore
    private String orderId;
    
    @JsonIgnore
    private String calendar;
    
    @JsonIgnore
    private String folder;
    
    @JsonIgnore
    private String comment;

    @JsonIgnore
    private Integer timeSpent;

    @JsonIgnore
    private String ticketLink;

    @JsonIgnore
    private Date startTime;

    public InventoryAudit(ConfigurationType type, String path, String folder, AuditParams auditParams) {
        this.objectType = type;
        this.path = path;
        this.folder = folder;
        switch (type) {
        case JOB:
            this.job = path;
            break;
        case WORKINGDAYSCALENDAR:
        case NONWORKINGDAYSCALENDAR:
            this.calendar = path;
            break;
        case FOLDER:
            this.path = folder;
            break;
        case SCHEDULE:
            this.orderId = path;
            break;
        case WORKFLOW:
            this.workflow = path;
            break;
        default:
            break;
        }
        setAuditParams(auditParams);
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
    public String getComment() {
        return comment;
    }

    @Override
    @JsonIgnore
    public String getFolder() {
        return folder;
    }

    @Override
    @JsonIgnore
    public String getJob() {
        return job;
    }

    @Override
    @JsonIgnore
    public String getOrderId() {
        return orderId;
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
    public String getCalendar() {
        return calendar;
    }

    @Override
    public String getControllerId() {
        return "-";
    }

    @Override
    public String getWorkflow() {
        return workflow;
    }
    
    @JsonProperty("objectType")
    public ConfigurationType getObjectType() {
        return objectType;
    }
    
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    @Override
    @JsonIgnore
    public Long getDepHistoryId() {
        return null;
    }

}
