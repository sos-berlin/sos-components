package com.sos.joc.event.bean.joc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.event.bean.JOCEvent;

public class RunningLogEvent extends JOCEvent {

    private final Object log4j2Event;

    public RunningLogEvent(Object log4j2Event) {
        super("RunningLogEvent", null, null);
        this.log4j2Event = log4j2Event;
    }

    /*
     * T should be always org.apache.logging.log4j.core.LogEvent
     * otherwise ClassCastException
     */
    @SuppressWarnings({ "unchecked" })
    @JsonIgnore
    public <T> T getLog4j2Event() throws ClassCastException {
        return (T) log4j2Event;
    }

}
