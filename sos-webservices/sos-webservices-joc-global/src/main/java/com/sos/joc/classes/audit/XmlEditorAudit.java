package com.sos.joc.classes.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sos.joc.classes.xmleditor.JocXmlEditor;
import com.sos.joc.classes.xmleditor.jobscheduler.JobSchedulerXmlEditor;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.model.xmleditor.deploy.DeployConfiguration;
import com.sos.joc.model.xmleditor.read.ReadConfiguration;
import com.sos.joc.model.xmleditor.store.StoreConfiguration;
import com.sos.joc.model.xmleditor.validate.ValidateConfiguration;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class XmlEditorAudit implements IAuditLog {

    @JsonProperty("objectType")
    private ObjectType objectType;

    @JsonProperty("name")
    private String name;

    @JsonProperty("schemaLocation")
    private String schemaLocation;

    @JsonProperty("controllerId")
    private String controllerId;

    @JsonIgnore
    private String folder;

    @JsonIgnore
    private String comment;

    @JsonIgnore
    private Integer timeSpent;

    @JsonIgnore
    private String ticketLink;


    public XmlEditorAudit(DeployConfiguration in) {
        controllerId = in.getControllerId();
        objectType = in.getObjectType();
        name = JocXmlEditor.getConfigurationName(objectType);
        schemaLocation = JocXmlEditor.getStandardRelativeSchemaLocation(objectType);
        folder = JobSchedulerXmlEditor.getNormalizedLiveFolder(objectType);
        setAuditParams(in.getAuditLog());
    }

    public XmlEditorAudit(ReadConfiguration in) {
        controllerId = in.getControllerId();
        objectType = in.getObjectType();
        name = String.valueOf(in.getId());
        folder = JobSchedulerXmlEditor.getNormalizedLiveFolder(objectType);
    }

    public XmlEditorAudit(StoreConfiguration in, String schemaPath) {
        controllerId = in.getControllerId();
        objectType = in.getObjectType();
        name = in.getName();
        schemaLocation = schemaPath;
        folder = JobSchedulerXmlEditor.getNormalizedLiveFolder(objectType);
    }

    public XmlEditorAudit(ValidateConfiguration in, String schemaPath) {
        controllerId = in.getControllerId();
        objectType = in.getObjectType();
        schemaLocation = schemaPath;
        folder = JobSchedulerXmlEditor.getNormalizedLiveFolder(objectType);
    }

    private void setAuditParams(AuditParams auditParams) {
        if (auditParams != null) {
            comment = auditParams.getComment();
            timeSpent = auditParams.getTimeSpent();
            ticketLink = auditParams.getTicketLink();
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
        return null;
    }

    @Override
    @JsonIgnore
    public String getOrderId() {
        return null;
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
        return null;
    }

    @Override
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    @JsonProperty("objectType")
    public ObjectType getObjectType() {
        return objectType;
    }
    
    @JsonProperty("schemaLocation")
    public String getSchemaLocation() {
        return schemaLocation;
    }
    
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @Override
    @JsonIgnore
    public String getWorkflow() {
        return null;
    }

    @Override
    @JsonIgnore
    public Long getDepHistoryId() {
        return null;
    }

}
