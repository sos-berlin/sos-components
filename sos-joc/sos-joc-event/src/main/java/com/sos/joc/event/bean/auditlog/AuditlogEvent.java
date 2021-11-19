package com.sos.joc.event.bean.auditlog;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sos.joc.event.bean.JOCEvent;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "TYPE", visible = true)
@JsonSubTypes({ 
    @JsonSubTypes.Type(AuditlogChangedEvent.class),
    @JsonSubTypes.Type(AuditlogWorkflowEvent.class)
})

public class AuditlogEvent extends JOCEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public AuditlogEvent() {
        super("AuditLogChanged", null, null);
    }
    
    /**
     * 
     * @param controllerId
     */
    public AuditlogEvent(String controllerId) {
        super("AuditLogChanged", controllerId, null);
    }
    
    public AuditlogEvent(String key, String controllerId) {
        super(key, controllerId, null);
    }
}
