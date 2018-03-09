
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
 * JobScheduler cluster member
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "version",
    "host",
    "port",
    "state",
    "startedAt",
    "precedence"
})
public class ClusterMember {

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
     * jobscheduler state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
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
    @JacksonXmlProperty(localName = "startedAt")
    private Date startedAt;
    /**
     * Only defined for passive cluster (0=primary, 1=secondary, ...)
     * 
     */
    @JsonProperty("precedence")
    @JsonPropertyDescription("Only defined for passive cluster (0=primary, 1=secondary, ...)")
    @JacksonXmlProperty(localName = "precedence")
    private Integer precedence;

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
     * jobscheduler state
     * <p>
     * 
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
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
     * Only defined for passive cluster (0=primary, 1=secondary, ...)
     * 
     */
    @JsonProperty("precedence")
    @JacksonXmlProperty(localName = "precedence")
    public Integer getPrecedence() {
        return precedence;
    }

    /**
     * Only defined for passive cluster (0=primary, 1=secondary, ...)
     * 
     */
    @JsonProperty("precedence")
    @JacksonXmlProperty(localName = "precedence")
    public void setPrecedence(Integer precedence) {
        this.precedence = precedence;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("version", version).append("host", host).append("port", port).append("state", state).append("startedAt", startedAt).append("precedence", precedence).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(port).append(host).append(startedAt).append(state).append(version).append(precedence).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ClusterMember) == false) {
            return false;
        }
        ClusterMember rhs = ((ClusterMember) other);
        return new EqualsBuilder().append(port, rhs.port).append(host, rhs.host).append(startedAt, rhs.startedAt).append(state, rhs.state).append(version, rhs.version).append(precedence, rhs.precedence).isEquals();
    }

}
