package com.sos.joc.event.bean.command;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.event.bean.JOCEvent;

public class WorkflowDeletedEvent extends JOCEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public WorkflowDeletedEvent() {
    }

    /**
     * @param folder
     */
    public WorkflowDeletedEvent(String controllerId, String path, String versionId) {
        super(WorkflowDeletedEvent.class.getSimpleName(), controllerId, null);
        putVariable("path", path);
        putVariable("versionId", versionId);
    }
    
    @JsonIgnore
    public String getPath() {
        return (String) getVariables().get("path");
    }
    
    @JsonIgnore
    public String getVersionId() {
        return (String) getVariables().get("versionId");
    }
}
