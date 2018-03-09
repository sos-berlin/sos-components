
package com.sos.joc.model.jobscheduler;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sos.joc.model.common.Err;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * jobscheduler (volatile part)
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "surveyDate",
    "jobschedulerId",
    "host",
    "port",
    "state",
    "url",
    "clusterType",
    "startedAt",
    "error"
})
public class JobSchedulerV {

    /**
     * survey date of the JobScheduler Master/Agent
     * <p>
     * Current date of the JobScheduler Master/Agent. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("surveyDate")
    @JsonPropertyDescription("Current date of the JobScheduler Master/Agent. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ")
    @JacksonXmlProperty(localName = "surveyDate")
    private Date surveyDate;
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    private String jobschedulerId;
    @JsonProperty("host")
    @JacksonXmlProperty(localName = "host")
    private String host;
    /**
     * port
     * <p>
     * 
     * 
     */
    @JsonProperty("port")
    @JacksonXmlProperty(localName = "port")
    private Integer port;
    /**
     * jobscheduler state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
    private JobSchedulerState state;
    @JsonProperty("url")
    @JacksonXmlProperty(localName = "url")
    private String url;
    /**
     * jobscheduler cluster member type
     * <p>
     * 
     * 
     */
    @JsonProperty("clusterType")
    @JacksonXmlProperty(localName = "clusterType")
    private ClusterMemberType clusterType;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("startedAt")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    @JacksonXmlProperty(localName = "startedAt")
    private Date startedAt;
    /**
     * error
     * <p>
     * 
     * 
     */
    @JsonProperty("error")
    @JacksonXmlProperty(localName = "error")
    private Err error;

    /**
     * survey date of the JobScheduler Master/Agent
     * <p>
     * Current date of the JobScheduler Master/Agent. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("surveyDate")
    @JacksonXmlProperty(localName = "surveyDate")
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
    @JacksonXmlProperty(localName = "surveyDate")
    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    @JsonProperty("host")
    @JacksonXmlProperty(localName = "host")
    public String getHost() {
        return host;
    }

    @JsonProperty("host")
    @JacksonXmlProperty(localName = "host")
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * port
     * <p>
     * 
     * 
     */
    @JsonProperty("port")
    @JacksonXmlProperty(localName = "port")
    public Integer getPort() {
        return port;
    }

    /**
     * port
     * <p>
     * 
     * 
     */
    @JsonProperty("port")
    @JacksonXmlProperty(localName = "port")
    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * jobscheduler state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
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
    @JacksonXmlProperty(localName = "state")
    public void setState(JobSchedulerState state) {
        this.state = state;
    }

    @JsonProperty("url")
    @JacksonXmlProperty(localName = "url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    @JacksonXmlProperty(localName = "url")
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
    @JacksonXmlProperty(localName = "clusterType")
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
    @JacksonXmlProperty(localName = "clusterType")
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
    @JacksonXmlProperty(localName = "startedAt")
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
    @JacksonXmlProperty(localName = "startedAt")
    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    /**
     * error
     * <p>
     * 
     * 
     */
    @JsonProperty("error")
    @JacksonXmlProperty(localName = "error")
    public Err getError() {
        return error;
    }

    /**
     * error
     * <p>
     * 
     * 
     */
    @JsonProperty("error")
    @JacksonXmlProperty(localName = "error")
    public void setError(Err error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("surveyDate", surveyDate).append("jobschedulerId", jobschedulerId).append("host", host).append("port", port).append("state", state).append("url", url).append("clusterType", clusterType).append("startedAt", startedAt).append("error", error).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(clusterType).append(surveyDate).append(port).append(host).append(startedAt).append(state).append(jobschedulerId).append(error).append(url).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobSchedulerV) == false) {
            return false;
        }
        JobSchedulerV rhs = ((JobSchedulerV) other);
        return new EqualsBuilder().append(clusterType, rhs.clusterType).append(surveyDate, rhs.surveyDate).append(port, rhs.port).append(host, rhs.host).append(startedAt, rhs.startedAt).append(state, rhs.state).append(jobschedulerId, rhs.jobschedulerId).append(error, rhs.error).append(url, rhs.url).isEquals();
    }

}
