package com.sos.joc.classes.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.docu.DocumentationImport;


public class ImportDocumentationAudit extends DocumentationImport implements IAuditLog {
    
    @JsonIgnore
    private String comment;

    @JsonIgnore
    private Integer timeSpent;
    
    @JsonIgnore
    private String ticketLink;
    
    private String controllerId;
    
    public ImportDocumentationAudit(DocumentationImport documentationImport) {
        setFile(documentationImport.getFile());
        setFolder(documentationImport.getFolder());
        setAuditParams(documentationImport.getAuditLog());
        this.controllerId = documentationImport.getJobschedulerId();
        setJobschedulerId(null);
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
    public String getWorkflow() {
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
    public Long getDepHistoryId() {
        return null;
    }

    @Override
    public String getControllerId() {
        return controllerId;
    }
}
