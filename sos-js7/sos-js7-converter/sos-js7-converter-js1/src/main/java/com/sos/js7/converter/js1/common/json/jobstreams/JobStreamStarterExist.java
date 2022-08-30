
package com.sos.js7.converter.js1.common.json.jobstreams;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * jobStreamStarter
 * <p>
 * List of all jobStream starters
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "state",
    "jobStreamStarterId",
    "starterName",
    "exist",
    "title",
    "nextStart",
    "endOfJobStream",
    "requiredJob"
})
public class JobStreamStarterExist {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    private String state;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStreamStarterId")
    private Long jobStreamStarterId;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("starterName")
    private String starterName;
    @JsonProperty("exist")
    private Boolean exist;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    private String title;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("nextStart")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date nextStart;
    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("endOfJobStream")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    private String endOfJobStream;
    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("requiredJob")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    private String requiredJob;

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

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStreamStarterId")
    public Long getJobStreamStarterId() {
        return jobStreamStarterId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStreamStarterId")
    public void setJobStreamStarterId(Long jobStreamStarterId) {
        this.jobStreamStarterId = jobStreamStarterId;
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

    @JsonProperty("exist")
    public Boolean getExist() {
        return exist;
    }

    @JsonProperty("exist")
    public void setExist(Boolean exist) {
        this.exist = exist;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("nextStart")
    public Date getNextStart() {
        return nextStart;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("nextStart")
    public void setNextStart(Date nextStart) {
        this.nextStart = nextStart;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("endOfJobStream")
    public String getEndOfJobStream() {
        return endOfJobStream;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("endOfJobStream")
    public void setEndOfJobStream(String endOfJobStream) {
        this.endOfJobStream = endOfJobStream;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("requiredJob")
    public String getRequiredJob() {
        return requiredJob;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("requiredJob")
    public void setRequiredJob(String requiredJob) {
        this.requiredJob = requiredJob;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("state", state).append("jobStreamStarterId", jobStreamStarterId).append("starterName", starterName).append("exist", exist).append("title", title).append("nextStart", nextStart).append("endOfJobStream", endOfJobStream).append("requiredJob", requiredJob).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(exist).append(endOfJobStream).append(starterName).append(jobStreamStarterId).append(requiredJob).append(state).append(nextStart).append(title).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobStreamStarterExist) == false) {
            return false;
        }
        JobStreamStarterExist rhs = ((JobStreamStarterExist) other);
        return new EqualsBuilder().append(exist, rhs.exist).append(endOfJobStream, rhs.endOfJobStream).append(starterName, rhs.starterName).append(jobStreamStarterId, rhs.jobStreamStarterId).append(requiredJob, rhs.requiredJob).append(state, rhs.state).append(nextStart, rhs.nextStart).append(title, rhs.title).isEquals();
    }

}
