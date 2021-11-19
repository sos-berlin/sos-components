package com.sos.joc.event.bean.auditlog;

public class AuditlogChangedEvent extends AuditlogEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public AuditlogChangedEvent() {
        super();
    }
    
    public AuditlogChangedEvent(String controllerId) {
        super(controllerId);
    }
}
