package com.sos.joc.event.bean.problem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.event.bean.JOCEvent;


public class ProblemEvent extends JOCEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public ProblemEvent() {
    }

    public ProblemEvent(String key, String controllerId, String errorMsg) {
        super(key, controllerId, null);
        putVariable("message", errorMsg);
        putVariable("onlyHint", false);
    }
    
    public ProblemEvent(String key, String controllerId, String errorMsg, Boolean onlyHint) {
        super(key, controllerId, null);
        putVariable("message", errorMsg);
        putVariable("onlyHint", onlyHint);
    }
    
    @JsonIgnore
    public String getMessage() {
        return (String) getVariables().get("message");
    }
    
    @JsonIgnore
    public boolean isOnlyHint() {
        return ((Boolean) getVariables().get("onlyHint")) == Boolean.TRUE;
    }
}
