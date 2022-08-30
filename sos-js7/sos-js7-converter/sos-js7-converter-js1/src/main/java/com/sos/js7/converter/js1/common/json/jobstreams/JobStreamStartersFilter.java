
package com.sos.js7.converter.js1.common.json.jobstreams;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JobStreamStarters Filter
 * <p>
 * Reset Workflow, unconsume expressions
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "jobStreamId",
    "starterName",
    "jobStream",
    "state",
    "limit"
})
public class JobStreamStartersFilter {

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStreamId")
    private Long jobStreamId;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("starterName")
    private String starterName;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStream")
    private String jobStream;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    private String state;
    @JsonProperty("limit")
    private Integer limit = 10000;

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStreamId")
    public Long getJobStreamId() {
        return jobStreamId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStreamId")
    public void setJobStreamId(Long jobStreamId) {
        this.jobStreamId = jobStreamId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("starterName")
    public String getStarterName() {
        return starterName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("starterName")
    public void setStarterName(String starterName) {
        this.starterName = starterName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStream")
    public String getJobStream() {
        return jobStream;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStream")
    public void setJobStream(String jobStream) {
        this.jobStream = jobStream;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public String getState() {
        return state;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public void setState(String state) {
        this.state = state;
    }

    @JsonProperty("limit")
    public Integer getLimit() {
        return limit;
    }

    @JsonProperty("limit")
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("jobStreamId", jobStreamId).append("starterName", starterName).append("jobStream", jobStream).append("state", state).append("limit", limit).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(starterName).append(jobStream).append(limit).append(state).append(jobschedulerId).append(jobStreamId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobStreamStartersFilter) == false) {
            return false;
        }
        JobStreamStartersFilter rhs = ((JobStreamStartersFilter) other);
        return new EqualsBuilder().append(starterName, rhs.starterName).append(jobStream, rhs.jobStream).append(limit, rhs.limit).append(state, rhs.state).append(jobschedulerId, rhs.jobschedulerId).append(jobStreamId, rhs.jobStreamId).isEquals();
    }

}
