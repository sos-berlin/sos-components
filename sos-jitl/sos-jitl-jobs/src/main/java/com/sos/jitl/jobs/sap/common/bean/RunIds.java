
package com.sos.jitl.jobs.sap.common.bean;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ids
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobId",
    "scheduleId",
    "runId",
    "scope"
})
public class RunIds {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobId")
    private Long jobId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("scheduleId")
    private String scheduleId;
    @JsonProperty("runId")
    private String runId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("scope")
    private RunIds.Scope scope;

    /**
     * No args constructor for use in serialization
     * 
     */
    public RunIds() {
    }

    /**
     * 
     * @param jobId
     * @param scope
     * @param scheduleId
     */
    public RunIds(Long jobId, String scheduleId, RunIds.Scope scope) {
        super();
        this.jobId = jobId;
        this.scheduleId = scheduleId;
        this.scope = scope;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobId")
    public Long getJobId() {
        return jobId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobId")
    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public RunIds withJobId(Long jobId) {
        this.jobId = jobId;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("scheduleId")
    public String getScheduleId() {
        return scheduleId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("scheduleId")
    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public RunIds withScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
        return this;
    }

    @JsonProperty("runId")
    public String getRunId() {
        return runId;
    }

    @JsonProperty("runId")
    public void setRunId(String runId) {
        this.runId = runId;
    }

    public RunIds withRunId(String runId) {
        this.runId = runId;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("scope")
    public RunIds.Scope getScope() {
        return scope;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("scope")
    public void setScope(RunIds.Scope scope) {
        this.scope = scope;
    }

    public RunIds withScope(RunIds.Scope scope) {
        this.scope = scope;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobId", jobId).append("scheduleId", scheduleId).append("runId", runId).append("scope", scope).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobId).append(runId).append(scheduleId).append(scope).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RunIds) == false) {
            return false;
        }
        RunIds rhs = ((RunIds) other);
        return new EqualsBuilder().append(jobId, rhs.jobId).append(runId, rhs.runId).append(scheduleId, rhs.scheduleId).append(scope, rhs.scope).isEquals();
    }

    public enum Scope {

        JOB("JOB"),
        SCHEDULE("SCHEDULE");
        private final String value;
        private final static Map<String, RunIds.Scope> CONSTANTS = new HashMap<String, RunIds.Scope>();

        static {
            for (RunIds.Scope c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private Scope(String value) {
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
        public static RunIds.Scope fromValue(String value) {
            RunIds.Scope constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
