
package com.sos.jitl.jobs.sap.common.bean;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * log
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "runId",
    "httpStatus",
    "runStatus",
    "runState",
    "statusMessage",
    "runText"
})
public class ScheduleLog {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("runId")
    private String runId;
    @JsonProperty("httpStatus")
    private Integer httpStatus;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("runStatus")
    private String runStatus;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("runState")
    private String runState;
    @JsonProperty("statusMessage")
    private String statusMessage;
    /**
     * contains serialized JSON object
     * 
     */
    @JsonProperty("runText")
    @JsonPropertyDescription("contains serialized JSON object")
    private String runText;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("runId")
    public String getRunId() {
        return runId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("runId")
    public void setRunId(String runId) {
        this.runId = runId;
    }

    public ScheduleLog withRunId(String runId) {
        this.runId = runId;
        return this;
    }

    @JsonProperty("httpStatus")
    public Integer getHttpStatus() {
        return httpStatus;
    }

    @JsonProperty("httpStatus")
    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
    }

    public ScheduleLog withHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("runStatus")
    public String getRunStatus() {
        return runStatus;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("runStatus")
    public void setRunStatus(String runStatus) {
        this.runStatus = runStatus;
    }

    public ScheduleLog withRunStatus(String runStatus) {
        this.runStatus = runStatus;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("runState")
    public String getRunState() {
        return runState;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("runState")
    public void setRunState(String runState) {
        this.runState = runState;
    }

    public ScheduleLog withRunState(String runState) {
        this.runState = runState;
        return this;
    }

    @JsonProperty("statusMessage")
    public String getStatusMessage() {
        return statusMessage;
    }

    @JsonProperty("statusMessage")
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public ScheduleLog withStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
        return this;
    }

    /**
     * contains serialized JSON object
     * 
     */
    @JsonProperty("runText")
    public String getRunText() {
        return runText;
    }

    /**
     * contains serialized JSON object
     * 
     */
    @JsonProperty("runText")
    public void setRunText(String runText) {
        this.runText = runText;
    }

    public ScheduleLog withRunText(String runText) {
        this.runText = runText;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public ScheduleLog withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("runId", runId).append("httpStatus", httpStatus).append("runStatus", runStatus).append("runState", runState).append("statusMessage", statusMessage).append("runText", runText).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(httpStatus).append(runId).append(runText).append(additionalProperties).append(runState).append(runStatus).append(statusMessage).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ScheduleLog) == false) {
            return false;
        }
        ScheduleLog rhs = ((ScheduleLog) other);
        return new EqualsBuilder().append(httpStatus, rhs.httpStatus).append(runId, rhs.runId).append(runText, rhs.runText).append(additionalProperties, rhs.additionalProperties).append(runState, rhs.runState).append(runStatus, rhs.runStatus).append(statusMessage, rhs.statusMessage).isEquals();
    }

}
