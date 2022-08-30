
package com.sos.js7.converter.js1.common.json.jobstreams;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.js7.converter.js1.common.json.NameValuePair;
import com.sos.js7.converter.js1.common.json.schedule.RunTime;


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
    "title",
    "nextStart",
    "endOfJobStream",
    "requiredJob",
    "jobs",
    "runTime",
    "params"
})
public class JobStreamStarter {

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
    @JsonProperty("jobs")
    private List<JobStreamJob> jobs = new ArrayList<JobStreamJob>();
    /**
     * runTime
     * <p>
     * 
     * 
     */
    @JsonProperty("runTime")
    private RunTime runTime;
    /**
     * params or environment variables
     * <p>
     * 
     * 
     */
    @JsonProperty("params")
    private List<NameValuePair> params = new ArrayList<NameValuePair>();

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

    @JsonProperty("jobs")
    public List<JobStreamJob> getJobs() {
        return jobs;
    }

    @JsonProperty("jobs")
    public void setJobs(List<JobStreamJob> jobs) {
        this.jobs = jobs;
    }

    /**
     * runTime
     * <p>
     * 
     * 
     */
    @JsonProperty("runTime")
    public RunTime getRunTime() {
        return runTime;
    }

    /**
     * runTime
     * <p>
     * 
     * 
     */
    @JsonProperty("runTime")
    public void setRunTime(RunTime runTime) {
        this.runTime = runTime;
    }

    /**
     * params or environment variables
     * <p>
     * 
     * 
     */
    @JsonProperty("params")
    public List<NameValuePair> getParams() {
        return params;
    }

    /**
     * params or environment variables
     * <p>
     * 
     * 
     */
    @JsonProperty("params")
    public void setParams(List<NameValuePair> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("state", state).append("jobStreamStarterId", jobStreamStarterId).append("starterName", starterName).append("title", title).append("nextStart", nextStart).append("endOfJobStream", endOfJobStream).append("requiredJob", requiredJob).append("jobs", jobs).append("runTime", runTime).append("params", params).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(endOfJobStream).append(starterName).append(jobs).append(jobStreamStarterId).append(requiredJob).append(state).append(nextStart).append(runTime).append(title).append(params).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobStreamStarter) == false) {
            return false;
        }
        JobStreamStarter rhs = ((JobStreamStarter) other);
        return new EqualsBuilder().append(endOfJobStream, rhs.endOfJobStream).append(starterName, rhs.starterName).append(jobs, rhs.jobs).append(jobStreamStarterId, rhs.jobStreamStarterId).append(requiredJob, rhs.requiredJob).append(state, rhs.state).append(nextStart, rhs.nextStart).append(runTime, rhs.runTime).append(title, rhs.title).append(params, rhs.params).isEquals();
    }

}
