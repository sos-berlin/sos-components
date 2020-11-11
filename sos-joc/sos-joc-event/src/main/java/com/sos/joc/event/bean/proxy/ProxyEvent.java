package com.sos.joc.event.bean.proxy;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sos.joc.event.bean.JOCEvent;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "TYPE", visible = true)
@JsonSubTypes({ 
    @JsonSubTypes.Type(ProxyStarted.class),
    @JsonSubTypes.Type(ProxyRestarted.class),
    @JsonSubTypes.Type(ProxyRemoved.class)
})

public abstract class ProxyEvent extends JOCEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public ProxyEvent() {
    }

    /**
     * @param key
     * @param controllerId
     * @param variables
     */
    public ProxyEvent(String key, String controllerId, Map<String, String> variables) {
        super(key, controllerId, variables);
    }
}
