
package com.sos.joc.model.jobscheduler;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * jobscheduler
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "surveyDate",
    "jobschedulerId",
    "host",
    "state",
    "url",
    "clusterType",
    "startedAt",
    "version",
    "os",
    "timeZone"
})
public class JobScheduler {

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    private Long id;
    /**
     * survey date of the JobScheduler Master/Agent
     * <p>
     * Current date of the JobScheduler Master/Agent. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("surveyDate")
    @JsonPropertyDescription("Current date of the JobScheduler Master/Agent. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ")
    private Date surveyDate;
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    @JsonProperty("host")
    private String host;
    /**
     * jobscheduler state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    private JobSchedulerState state;
    @JsonProperty("url")
    private String url;
    /**
     * jobscheduler cluster member type
     * <p>
     * 
     * 
     */
    @JsonProperty("clusterType")
    private ClusterMemberType clusterType;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("startedAt")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date startedAt;
    @JsonProperty("version")
    private String version;
    /**
     * jobscheduler platform
     * <p>
     * 
     * 
     */
    @JsonProperty("os")
    private OperatingSystem os;
    @JsonProperty("timeZone")
    private String timeZone;

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * survey date of the JobScheduler Master/Agent
     * <p>
     * Current date of the JobScheduler Master/Agent. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("surveyDate")
    public Date getSurveyDate() {
        return surveyDate;
    }

    /**
     * survey date of the JobScheduler Master/Agent
     * <p>
     * Current date of the JobScheduler Master/Agent. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("surveyDate")
    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    @JsonProperty("host")
    public String getHost() {
        return host;
    }

    @JsonProperty("host")
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * jobscheduler state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public JobSchedulerState getState() {
        return state;
    }

    /**
     * jobscheduler state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public void setState(JobSchedulerState state) {
        this.state = state;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * jobscheduler cluster member type
     * <p>
     * 
     * 
     */
    @JsonProperty("clusterType")
    public ClusterMemberType getClusterType() {
        return clusterType;
    }

    /**
     * jobscheduler cluster member type
     * <p>
     * 
     * 
     */
    @JsonProperty("clusterType")
    public void setClusterType(ClusterMemberType clusterType) {
        this.clusterType = clusterType;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("startedAt")
    public Date getStartedAt() {
        return startedAt;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("startedAt")
    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * jobscheduler platform
     * <p>
     * 
     * 
     */
    @JsonProperty("os")
    public OperatingSystem getOs() {
        return os;
    }

    /**
     * jobscheduler platform
     * <p>
     * 
     * 
     */
    @JsonProperty("os")
    public void setOs(OperatingSystem os) {
        this.os = os;
    }

    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    @JsonProperty("timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("surveyDate", surveyDate).append("jobschedulerId", jobschedulerId).append("host", host).append("state", state).append("url", url).append("clusterType", clusterType).append("startedAt", startedAt).append("version", version).append("os", os).append("timeZone", timeZone).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(clusterType).append(surveyDate).append(os).append(host).append(startedAt).append(timeZone).append(id).append(state).append(jobschedulerId).append(version).append(url).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobScheduler) == false) {
            return false;
        }
        JobScheduler rhs = ((JobScheduler) other);
        return new EqualsBuilder().append(clusterType, rhs.clusterType).append(surveyDate, rhs.surveyDate).append(os, rhs.os).append(host, rhs.host).append(startedAt, rhs.startedAt).append(timeZone, rhs.timeZone).append(id, rhs.id).append(state, rhs.state).append(jobschedulerId, rhs.jobschedulerId).append(version, rhs.version).append(url, rhs.url).isEquals();
    }

}
