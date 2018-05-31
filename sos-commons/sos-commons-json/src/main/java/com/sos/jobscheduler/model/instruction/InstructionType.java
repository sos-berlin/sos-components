
package com.sos.jobscheduler.model.instruction;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum InstructionType {

    WORKFLOW("Workflow"),
    JOB("Job"),
    IF("If"),
    TRY_CATCH("TryCatch"),
    FORK_JOIN("ForkJoin");
    private final String value;
    private final static Map<String, InstructionType> CONSTANTS = new HashMap<String, InstructionType>();

    static {
        for (InstructionType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private InstructionType(String value) {
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
    public static InstructionType fromValue(String value) {
        InstructionType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
