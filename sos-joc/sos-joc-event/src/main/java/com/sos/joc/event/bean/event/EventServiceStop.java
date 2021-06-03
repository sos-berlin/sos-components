package com.sos.joc.event.bean.event;

import java.util.Map;

public class EventServiceStop extends EventServiceEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public EventServiceStop() {
    }

    /**
     * @param key
     * @param controllerId
     * @param variables
     */
    public EventServiceStop(String key, String controllerId, Map<String, Object> variables) {
        super(key, controllerId, variables);
    }
    
    public EventServiceStop(String key, String controllerId, String accessToken) {
        super(key, controllerId, accessToken);
    }
}
