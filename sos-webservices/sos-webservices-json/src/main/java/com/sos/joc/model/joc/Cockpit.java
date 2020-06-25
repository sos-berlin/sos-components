
package com.sos.joc.model.joc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.jobscheduler.ClusterNodeState;
import com.sos.joc.model.jobscheduler.ComponentState;
import com.sos.joc.model.jobscheduler.ConnectionState;
import com.sos.joc.model.jobscheduler.OperatingSystem;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * joc cockpit
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "title",
    "current",
    "host",
    "url",
    "startedAt",
    "version",
    "connectionState",
    "componentState",
    "clusterNodeState",
    "controllerConnectionStates",
    "os",
    "securityLevel",
    "lastHeartbeat"
})
public class Cockpit {

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    private Long id;
    @JsonProperty("title")
    private String title;
    /**
     * true if joc is that joc which sends this response
     * (Required)
     * 
     */
    @JsonProperty("current")
    @JsonPropertyDescription("true if joc is that joc which sends this response")
    private Boolean current;
    @JsonProperty("host")
    private String host;
    @JsonProperty("url")
    private String url;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("startedAt")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date startedAt;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    private String version;
    /**
     * connection state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("connectionState")
    private ConnectionState connectionState;
    /**
     * component state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("componentState")
    private ComponentState componentState;
    /**
     * active state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("clusterNodeState")
    private ClusterNodeState clusterNodeState;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerConnectionStates")
    private List<ControllerConnectionState> controllerConnectionStates = new ArrayList<ControllerConnectionState>();
    /**
     * jobscheduler platform
     * <p>
     * 
     * 
     */
    @JsonProperty("os")
    private OperatingSystem os;
    /**
     * Security Level of JOC Cockpit
     * <p>
     * 
     * 
     */
    @JsonProperty("securityLevel")
    private JocSecurityLevel securityLevel;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("lastHeartbeat")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date lastHeartbeat;

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

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * true if joc is that joc which sends this response
     * (Required)
     * 
     */
    @JsonProperty("current")
    public Boolean getCurrent() {
        return current;
    }

    /**
     * true if joc is that joc which sends this response
     * (Required)
     * 
     */
    @JsonProperty("current")
    public void setCurrent(Boolean current) {
        this.current = current;
    }

    @JsonProperty("host")
    public String getHost() {
        return host;
    }

    @JsonProperty("host")
    public void setHost(String host) {
        this.host = host;
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
     * connection state
     * <p>
     * 
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("connectionState")
    public void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    /**
     * component state
     * <p>
     * 
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("componentState")
    public void setComponentState(ComponentState componentState) {
        this.componentState = componentState;
    }

    /**
     * active state
     * <p>
     * 
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("clusterNodeState")
    public void setClusterNodeState(ClusterNodeState clusterNodeState) {
        this.clusterNodeState = clusterNodeState;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerConnectionStates")
    public List<ControllerConnectionState> getControllerConnectionStates() {
        return controllerConnectionStates;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerConnectionStates")
    public void setControllerConnectionStates(List<ControllerConnectionState> controllerConnectionStates) {
        this.controllerConnectionStates = controllerConnectionStates;
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

    /**
     * Security Level of JOC Cockpit
     * <p>
     * 
     * 
     */
    @JsonProperty("securityLevel")
    public JocSecurityLevel getSecurityLevel() {
        return securityLevel;
    }

    /**
     * Security Level of JOC Cockpit
     * <p>
     * 
     * 
     */
    @JsonProperty("securityLevel")
    public void setSecurityLevel(JocSecurityLevel securityLevel) {
        this.securityLevel = securityLevel;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("lastHeartbeat")
    public Date getLastHeartbeat() {
        return lastHeartbeat;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("lastHeartbeat")
    public void setLastHeartbeat(Date lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("title", title).append("current", current).append("host", host).append("url", url).append("startedAt", startedAt).append("version", version).append("connectionState", connectionState).append("componentState", componentState).append("clusterNodeState", clusterNodeState).append("controllerConnectionStates", controllerConnectionStates).append("os", os).append("securityLevel", securityLevel).append("lastHeartbeat", lastHeartbeat).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(lastHeartbeat).append(os).append(connectionState).append(startedAt).append(title).append(version).append(url).append(componentState).append(securityLevel).append(current).append(controllerConnectionStates).append(host).append(clusterNodeState).append(id).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Cockpit) == false) {
            return false;
        }
        Cockpit rhs = ((Cockpit) other);
        return new EqualsBuilder().append(lastHeartbeat, rhs.lastHeartbeat).append(os, rhs.os).append(connectionState, rhs.connectionState).append(startedAt, rhs.startedAt).append(title, rhs.title).append(version, rhs.version).append(url, rhs.url).append(componentState, rhs.componentState).append(securityLevel, rhs.securityLevel).append(current, rhs.current).append(controllerConnectionStates, rhs.controllerConnectionStates).append(host, rhs.host).append(clusterNodeState, rhs.clusterNodeState).append(id, rhs.id).isEquals();
    }

}
