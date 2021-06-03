package com.sos.joc.event.bean.problem;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.event.bean.JOCEvent;


public class ProblemEvent extends JOCEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public ProblemEvent() {
    }

    /**
     * @param key
     * @param controllerId
     * @param variables
     */
    public ProblemEvent(String key, String controllerId, Map<String, Object> variables) {
        super(key, controllerId, variables);
    }
    
    public ProblemEvent(String key, String controllerId, String errorMsg) {
        super();
        setKey(key);
        setControllerId(controllerId);
        putVariable("message", errorMsg);
    }
    
    @JsonIgnore
    public String getMessage() {
        return (String) getVariables().get("message");
    }
}
