package com.sos.joc.event.bean.deploy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.event.bean.JOCEvent;

public class DeploymentHistoryMoveEvent extends JOCEvent {

    public DeploymentHistoryMoveEvent(String name, String folder, Integer objectType, Long inventoryId, Long auditLogId) {
        super("", null, null);
        putVariable("name", name);
        putVariable("folder", folder);
        putVariable("objectType", objectType);
        putVariable("inventoryId", inventoryId);
        putVariable("auditLogId", auditLogId);
    }

    @JsonIgnore
    public String getName() {
        return (String) getVariables().get("name");
    }

    @JsonIgnore
    public String getFolder() {
        return (String) getVariables().get("folder");
    }

    @JsonIgnore
    public Integer getObjectType() {
        return (Integer) getVariables().get("objectType");
    }

    @JsonIgnore
    public Long getInventoryId() {
        return (Long) getVariables().get("inventoryId");
    }

    @JsonIgnore
    public Long getAuditLogId() {
        return (Long) getVariables().get("auditLogId");
    }

}
