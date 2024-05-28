package com.sos.inventory.model.report;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TemplateId {

    WORKFLOWS_FREQUENTLY_FAILED(1, true, "Top ${hits} frequently failed workflows"),
    JOBS_FREQUENTLY_FAILED(2, true, "Top ${hits} frequently failed jobs"),
    AGENTS_PARALLEL_JOB_EXECUTIONS(3, true, "Top ${hits} agents with most parallel job execution"),
    JOBS_HIGH_LOW_EXECUTION_PERIODS(4, true, "Top ${hits} periods of low and high parallelism of job executions"),
    JOBS_EXECUTIONS_FREQUENCY(5, true, "Top ${hits} high criticality failed jobs"),
    ORDERS_EXECUTIONS_FREQUENCY(6, true, "Top ${hits} frequently failed workflows with cancelled orders"),
    WORKFLOWS_LONGEST_EXECUTION_TIMES(7, true, "Top ${hits} workflows with the longest execution time"),
    JOBS_LONGEST_EXECUTION_TIMES(8, true, "Top ${hits} jobs with the longest execution time"),
    PERIODS_MOST_ORDER_EXECUTIONS(9, true, "Top ${hits} periods during which mostly workflows executed"),
    PERIODS_MOST_JOB_EXECUTIONS(10, true, "Top ${hits} periods during which mostly jobs executed");
    
    private final Boolean supported;
    private final Integer intValue;
    private final String title;
    private final static Map<String, TemplateId> CONSTANTS = new HashMap<String, TemplateId>();
    private final static Map<Integer, TemplateId> INTCONSTANTS = new HashMap<Integer, TemplateId>();

    static {
        for (TemplateId c: values()) {
            CONSTANTS.put(c.name(), c);
        }
    }

    static {
        for (TemplateId c: values()) {
            INTCONSTANTS.put(c.intValue, c);
        }
    }

    private TemplateId(Integer intValue, Boolean supported, String title) {
        this.intValue = intValue;
        this.supported = supported;
        this.title = title;
    }

    @Override
    public String toString() {
        return this.name();
    }

    @JsonValue
    public String value() {
        return this.name();
    }

    public Integer intValue() {
        return this.intValue;
    }
    
    public Boolean isSupported() {
        return this.supported;
    }
    
    public String getTitle() {
        return this.title;
    }

    @JsonCreator
    public static TemplateId fromValue(String value) {
        TemplateId constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }
    
    public static TemplateId fromValue(Integer value) {
        TemplateId constant = INTCONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value + "");
        } else {
            return constant;
        }
    }
}
