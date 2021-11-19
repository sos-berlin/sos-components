package com.sos.joc.event.bean.auditlog;

import java.nio.file.Path;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AuditlogWorkflowEvent extends AuditlogEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public AuditlogWorkflowEvent() {
    }
    
    /**
     * 
     * @param controllerId
     * @param workflowPath
     * @param versionId
     */
    public AuditlogWorkflowEvent(String controllerId, Path workflowPath, String versionId) {
        super("WorkflowAuditLogChanged", controllerId);
        putVariable("workflowPath", workflowPath.toString().replace('\\', '/'));
        putVariable("versionId", versionId);
    }
    
    /**
     * 
     * @param controllerId
     * @param workflowPath
     * @param versionId
     */
    public AuditlogWorkflowEvent(String controllerId, String workflowPath, String versionId) {
        super("WorkflowAuditLogChanged", controllerId);
        putVariable("workflowPath", workflowPath);
        putVariable("versionId", versionId);
    }
    
    public AuditlogWorkflowEvent(String controllerId, Path workflowPath) {
        super("WorkflowAuditLogChanged", controllerId);
        putVariable("workflowPath", workflowPath.toString().replace('\\', '/'));
    }
    
    public AuditlogWorkflowEvent(String controllerId, String workflowPath) {
        super("WorkflowAuditLogChanged", controllerId);
        putVariable("workflowPath", workflowPath);
    }
    
    @JsonIgnore
    public String getWorkflowPath() {
        return (String) getVariables().get("workflowPath");
    }
    
    @JsonIgnore
    public String getVersionId() {
        return (String) getVariables().get("versionId");
    }
}
