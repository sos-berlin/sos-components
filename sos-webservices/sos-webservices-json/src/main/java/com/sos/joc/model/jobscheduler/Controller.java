
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
 * JobScheduler Controller
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "surveyDate",
    "jobschedulerId",
    "title",
    "host",
    "url",
    "clusterUrl",
    "role",
    "isCoupled",
    "startedAt",
    "version",
    "os",
    "timeZone",
    "componentState",
    "connectionState",
    "clusterNodeState"
})
public class Controller {

    /**
     * non negative long
     * <p>
     * 
     * (Required)
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
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("title")
    private String title;
    @JsonProperty("host")
    private String host;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("url")
    private String url;
    @JsonProperty("clusterUrl")
    private String clusterUrl;
    /**
     * jobscheduler role
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("role")
    private Role role;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("isCoupled")
    private Boolean isCoupled = false;
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
     * component state
     * <p>
     * 
     * 
     */
    @JsonProperty("componentState")
    private ComponentState componentState;
    /**
     * connection state
     * <p>
     * 
     * 
     */
    @JsonProperty("connectionState")
    private ConnectionState connectionState;
    /**
     * active state
     * <p>
     * 
     * 
     */
    @JsonProperty("clusterNodeState")
    private ClusterNodeState clusterNodeState;

    /**
     * non negative long
     * <p>
     * 
     * (Required)
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
     * (Required)
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

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
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
     * 
     * (Required)
     * 
     */
    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("clusterUrl")
    public String getClusterUrl() {
        return clusterUrl;
    }

    @JsonProperty("clusterUrl")
    public void setClusterUrl(String clusterUrl) {
        this.clusterUrl = clusterUrl;
    }

    /**
     * jobscheduler role
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("role")
    public Role getRole() {
        return role;
    }

    /**
     * jobscheduler role
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("role")
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("isCoupled")
    public Boolean getIsCoupled() {
        return isCoupled;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("isCoupled")
    public void setIsCoupled(Boolean isCoupled) {
        this.isCoupled = isCoupled;
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

    /**
     * component state
     * <p>
     * 
     * 
     */
    @JsonProperty("componentState")
    public ComponentState getComponentState() {
        return componentState;
    }

    /**
     * component state
     * <p>
     * 
     * 
     */
    @JsonProperty("componentState")
    public void setComponentState(ComponentState componentState) {
        this.componentState = componentState;
    }

    /**
     * connection state
     * <p>
     * 
     * 
     */
    @JsonProperty("connectionState")
    public ConnectionState getConnectionState() {
        return connectionState;
    }

    /**
     * connection state
     * <p>
     * 
     * 
     */
    @JsonProperty("connectionState")
    public void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    /**
     * active state
     * <p>
     * 
     * 
     */
    @JsonProperty("clusterNodeState")
    public ClusterNodeState getClusterNodeState() {
        return clusterNodeState;
    }

    /**
     * active state
     * <p>
     * 
     * 
     */
    @JsonProperty("clusterNodeState")
    public void setClusterNodeState(ClusterNodeState clusterNodeState) {
        this.clusterNodeState = clusterNodeState;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("surveyDate", surveyDate).append("jobschedulerId", jobschedulerId).append("title", title).append("host", host).append("url", url).append("clusterUrl", clusterUrl).append("role", role).append("isCoupled", isCoupled).append("startedAt", startedAt).append("version", version).append("os", os).append("timeZone", timeZone).append("componentState", componentState).append("connectionState", connectionState).append("clusterNodeState", clusterNodeState).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(role).append(surveyDate).append(os).append(connectionState).append(clusterUrl).append(startedAt).append(timeZone).append(title).append(version).append(url).append(componentState).append(isCoupled).append(host).append(clusterNodeState).append(id).append(jobschedulerId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Controller) == false) {
            return false;
        }
        Controller rhs = ((Controller) other);
        return new EqualsBuilder().append(role, rhs.role).append(surveyDate, rhs.surveyDate).append(os, rhs.os).append(connectionState, rhs.connectionState).append(clusterUrl, rhs.clusterUrl).append(startedAt, rhs.startedAt).append(timeZone, rhs.timeZone).append(title, rhs.title).append(version, rhs.version).append(url, rhs.url).append(componentState, rhs.componentState).append(isCoupled, rhs.isCoupled).append(host, rhs.host).append(clusterNodeState, rhs.clusterNodeState).append(id, rhs.id).append(jobschedulerId, rhs.jobschedulerId).isEquals();
    }

}
