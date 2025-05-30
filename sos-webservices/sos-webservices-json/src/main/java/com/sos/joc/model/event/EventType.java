
package com.sos.joc.model.event;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EventType {

    JOCCLUSTER("JOCCLUSTER"),
    CONTROLLER("CONTROLLER"),
    CONTROLLERCLUSTER("CONTROLLERCLUSTER"),
    AGENT("AGENT"),
    PROBLEM("PROBLEM"),
    FOLDER("FOLDER"),
    TAG("TAG"),
    WORKFLOW("WORKFLOW"),
    ORDER("ORDER"),
    LOCK("LOCK"),
    JOB("JOB"),
    FILEORDERSOURCE("FILEORDERSOURCE"),
    NOTICEBOARD("NOTICEBOARD"),
    JOBCLASS("JOBCLASS"),
    JOBTEMPLATE("JOBTEMPLATE"),
    JOBRESOURCE("JOBRESOURCE"),
    SCHEDULE("SCHEDULE"),
    INCLUDESCRIPT("INCLUDESCRIPT"),
    REPORT("REPORT"),
    CALENDAR("CALENDAR"),
    WORKINGDAYSCALENDAR("WORKINGDAYSCALENDAR"),
    NONWORKINGDAYSCALENDAR("NONWORKINGDAYSCALENDAR"),
    DAILYPLAN("DAILYPLAN"),
    ORDERHISTORY("ORDERHISTORY"),
    TASKHISTORY("TASKHISTORY"),
    FILETRANSFER("FILETRANSFER"),
    MONITORINGNOTIFICATION("MONITORINGNOTIFICATION"),
    MONITORINGCONTROLLER("MONITORINGCONTROLLER"),
    MONITORINGAGENT("MONITORINGAGENT"),
    AUDITLOG("AUDITLOG"),
    PLAN("PLAN"),
    APPROVAL("APPROVAL");
    private final String value;
    private final static Map<String, EventType> CONSTANTS = new HashMap<String, EventType>();

    static {
        for (EventType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private EventType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @JsonValue
    public String value() {
        return this.value;
    }

    @JsonCreator
    public static EventType fromValue(String value) {
        EventType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
