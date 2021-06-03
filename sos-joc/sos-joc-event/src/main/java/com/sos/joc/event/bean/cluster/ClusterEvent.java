package com.sos.joc.event.bean.cluster;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sos.joc.event.bean.JOCEvent;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "TYPE", visible = true)
@JsonSubTypes({ @JsonSubTypes.Type(ActiveClusterChangedEvent.class) })

public abstract class ClusterEvent extends JOCEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public ClusterEvent() {
    }

    /**
     * @param key
     * @param controllerId
     * @param variables
     */
    public ClusterEvent(String key, String controllerId, Map<String, Object> variables) {
        super(key, controllerId, variables);
    }

}
