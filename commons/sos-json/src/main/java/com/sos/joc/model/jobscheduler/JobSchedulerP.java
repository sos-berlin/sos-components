
package com.sos.joc.model.jobscheduler;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * jobscheduler (permanent part)
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "surveyDate",
    "jobschedulerId",
    "version",
    "host",
    "port",
    "os",
    "timeZone",
    "url",
    "clusterType",
    "startedAt",
    "supervisor"
})
public class JobSchedulerP {

    /**
     * survey date of the inventory data; last time the inventory job has checked the live folder
     * <p>
     * Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    @JsonPropertyDescription("Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ")
    @JacksonXmlProperty(localName = "surveyDate")
    private Date surveyDate;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    private String jobschedulerId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    @JacksonXmlProperty(localName = "version")
    private String version;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("host")
    @JacksonXmlProperty(localName = "host")
    private String host;
    /**
     * port
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("port")
    @JacksonXmlProperty(localName = "port")
    private Integer port;
    /**
     * jobscheduler platform
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("os")
    @JacksonXmlProperty(localName = "os")
    private OperatingSystem os;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("timeZone")
    @JacksonXmlProperty(localName = "timeZone")
    private String timeZone;
    @JsonProperty("url")
    @JacksonXmlProperty(localName = "url")
    private String url;
    /**
     * jobscheduler cluster member type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("clusterType")
    @JacksonXmlProperty(localName = "clusterType")
    private ClusterMemberType clusterType;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("startedAt")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    @JacksonXmlProperty(localName = "startedAt")
    private Date startedAt;
    /**
     * hostPortParam
     * <p>
     * 
     * 
     */
    @JsonProperty("supervisor")
    @JacksonXmlProperty(localName = "supervisor")
    private HostPortParameter supervisor;

    /**
     * survey date of the inventory data; last time the inventory job has checked the live folder
     * <p>
     * Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    @JacksonXmlProperty(localName = "surveyDate")
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
    @JacksonXmlProperty(localName = "surveyDate")
    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    @JacksonXmlProperty(localName = "version")
    public String getVersion() {
        return version;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    @JacksonXmlProperty(localName = "version")
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("host")
    @JacksonXmlProperty(localName = "host")
    public String getHost() {
        return host;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("host")
    @JacksonXmlProperty(localName = "host")
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * port
     * <p>
     * 
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("port")
    @JacksonXmlProperty(localName = "port")
    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * jobscheduler platform
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("os")
    @JacksonXmlProperty(localName = "os")
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
    @JacksonXmlProperty(localName = "os")
    public void setOs(OperatingSystem os) {
        this.os = os;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("timeZone")
    @JacksonXmlProperty(localName = "timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("timeZone")
    @JacksonXmlProperty(localName = "timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
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
     * (Required)
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
     * (Required)
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
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("startedAt")
    @JacksonXmlProperty(localName = "startedAt")
    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    /**
     * hostPortParam
     * <p>
     * 
     * 
     */
    @JsonProperty("supervisor")
    @JacksonXmlProperty(localName = "supervisor")
    public HostPortParameter getSupervisor() {
        return supervisor;
    }

    /**
     * hostPortParam
     * <p>
     * 
     * 
     */
    @JsonProperty("supervisor")
    @JacksonXmlProperty(localName = "supervisor")
    public void setSupervisor(HostPortParameter supervisor) {
        this.supervisor = supervisor;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("surveyDate", surveyDate).append("jobschedulerId", jobschedulerId).append("version", version).append("host", host).append("port", port).append("os", os).append("timeZone", timeZone).append("url", url).append("clusterType", clusterType).append("startedAt", startedAt).append("supervisor", supervisor).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(clusterType).append(surveyDate).append(os).append(port).append(host).append(timeZone).append(startedAt).append(jobschedulerId).append(version).append(url).append(supervisor).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobSchedulerP) == false) {
            return false;
        }
        JobSchedulerP rhs = ((JobSchedulerP) other);
        return new EqualsBuilder().append(clusterType, rhs.clusterType).append(surveyDate, rhs.surveyDate).append(os, rhs.os).append(port, rhs.port).append(host, rhs.host).append(timeZone, rhs.timeZone).append(startedAt, rhs.startedAt).append(jobschedulerId, rhs.jobschedulerId).append(version, rhs.version).append(url, rhs.url).append(supervisor, rhs.supervisor).isEquals();
    }

}
