package com.sos.joc.event.bean.event;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sos.joc.event.bean.JOCEvent;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "TYPE", visible = true)
@JsonSubTypes({ 
    @JsonSubTypes.Type(EventServiceStart.class),
    @JsonSubTypes.Type(EventServiceStop.class)
})


public abstract class EventServiceEvent extends JOCEvent {
    
    private static String accessTokenKey = "accessToken";
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public EventServiceEvent() {
    }

    /**
     * @param key
     * @param controllerId
     * @param variables
     */
    public EventServiceEvent(String key, String controllerId, Map<String, Object> variables) {
        super(key, controllerId, variables);
    }
    
    public EventServiceEvent(String key, String controllerId, String accessToken) {
        super();
        setKey(key);
        setControllerId(controllerId);
        if (accessToken != null) {
            putVariable(accessTokenKey, accessToken);
        }
    }
    
    public String getAccessToken() {
        return (String) getVariables().get(accessTokenKey);
    }
}
