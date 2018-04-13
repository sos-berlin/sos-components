
package com.sos.joc.model.common;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JobSchedulerObjectType {

    JOB("JOB"),
    JOBCHAIN("JOBCHAIN"),
    ORDER("ORDER"),
    PROCESSCLASS("PROCESSCLASS"),
    AGENTCLUSTER("AGENTCLUSTER"),
    LOCK("LOCK"),
    SCHEDULE("SCHEDULE"),
    WORKINGDAYSCALENDAR("WORKINGDAYSCALENDAR"),
    NONWORKINGDAYSCALENDAR("NONWORKINGDAYSCALENDAR"),
    FOLDER("FOLDER"),
    JOBSCHEDULER("JOBSCHEDULER"),
    OTHER("OTHER");
    private final String value;
    private final static Map<String, JobSchedulerObjectType> CONSTANTS = new HashMap<String, JobSchedulerObjectType>();

    static {
        for (JobSchedulerObjectType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private JobSchedulerObjectType(String value) {
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
    public static JobSchedulerObjectType fromValue(String value) {
        JobSchedulerObjectType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
