package com.sos.joc.event.bean.deploy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sos.joc.event.bean.JOCEvent;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "TYPE", visible = true)
@JsonSubTypes({ @JsonSubTypes.Type(DeployHistoryWorkflowEvent.class), @JsonSubTypes.Type(DeployHistoryJobResourceEvent.class)

})

public class DeployHistoryEvent extends JOCEvent {

    /** No args constructor for use in serialization */
    public DeployHistoryEvent() {
    }

    public DeployHistoryEvent(String controllerId, String name, String commitId, String path, Integer objectType) {
        super("DeployHistoryUpdated", controllerId, null);
        putVariable("name", name);
        putVariable("commitId", commitId);
        putVariable("path", path);
        putVariable("objectType", objectType);
    }

    public DeployHistoryEvent(String key, String controllerId, String name, String commitId, String path, Integer objectType) {
        super(key, controllerId, null);
        putVariable("name", name);
        putVariable("commitId", commitId);
        putVariable("path", path);
        putVariable("objectType", objectType);
    }

    @JsonIgnore
    public String getName() {
        return (String) getVariables().get("name");
    }

    @JsonIgnore
    public String getCommitId() {
        return (String) getVariables().get("commitId");
    }

    @JsonIgnore
    public String getPath() {
        return (String) getVariables().get("path");
    }

    @JsonIgnore
    public Integer getObjectType() {
        return (Integer) getVariables().get("historyId");
    }
}
