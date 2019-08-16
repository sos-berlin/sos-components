
package com.sos.joc.model.jobscheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * jobscheduler agent (permanent part)
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "surveyDate",
    "version",
    "host",
    "url",
    "os",
    "state",
    "startedAt",
    "clusters"
})
public class AgentP {

    /**
     * survey date of the inventory data; last time the inventory job has checked the live folder
     * <p>
     * Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    @JsonPropertyDescription("Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ")
    private Date surveyDate;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    private String version;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("host")
    private String host;
    /**
     * url can be different against host/port if agent behind a proxy
     * (Required)
     * 
     */
    @JsonProperty("url")
    @JsonPropertyDescription("url can be different against host/port if agent behind a proxy")
    private String url;
    /**
     * jobscheduler platform
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("os")
    private OperatingSystem os;
    /**
     * jobscheduler state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    private JobSchedulerState state;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("startedAt")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date startedAt;
    /**
     * Collection of process class' paths
     * (Required)
     * 
     */
    @JsonProperty("clusters")
    @JsonPropertyDescription("Collection of process class' paths")
    private List<String> clusters = new ArrayList<String>();

    /**
     * survey date of the inventory data; last time the inventory job has checked the live folder
     * <p>
     * Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    public Date getSurveyDate() {
        return surveyDate;
    }

    /**
     * survey date of the inventory data; last time the inventory job has checked the live folder
     * <p>
     * Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("host")
    public String getHost() {
        return host;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("host")
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * url can be different against host/port if agent behind a proxy
     * (Required)
     * 
     */
    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    /**
     * url can be different against host/port if agent behind a proxy
     * (Required)
     * 
     */
    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * jobscheduler platform
     * <p>
     * 
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("os")
    public void setOs(OperatingSystem os) {
        this.os = os;
    }

    /**
     * jobscheduler state
     * <p>
     * 
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("state")
    public void setState(JobSchedulerState state) {
        this.state = state;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("startedAt")
    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    /**
     * Collection of process class' paths
     * (Required)
     * 
     */
    @JsonProperty("clusters")
    public List<String> getClusters() {
        return clusters;
    }

    /**
     * Collection of process class' paths
     * (Required)
     * 
     */
    @JsonProperty("clusters")
    public void setClusters(List<String> clusters) {
        this.clusters = clusters;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("surveyDate", surveyDate).append("version", version).append("host", host).append("url", url).append("os", os).append("state", state).append("startedAt", startedAt).append("clusters", clusters).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(surveyDate).append(os).append(host).append(startedAt).append(state).append(version).append(url).append(clusters).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AgentP) == false) {
            return false;
        }
        AgentP rhs = ((AgentP) other);
        return new EqualsBuilder().append(surveyDate, rhs.surveyDate).append(os, rhs.os).append(host, rhs.host).append(startedAt, rhs.startedAt).append(state, rhs.state).append(version, rhs.version).append(url, rhs.url).append(clusters, rhs.clusters).isEquals();
    }

}
