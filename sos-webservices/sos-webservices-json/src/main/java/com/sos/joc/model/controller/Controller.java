
package com.sos.joc.model.controller;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.JocSecurityLevel;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Controller
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "surveyDate",
    "controllerId",
    "title",
    "host",
    "url",
    "clusterUrl",
    "role",
    "isCoupled",
    "startedAt",
    "version",
    "javaVersion",
    "os",
    "securityLevel",
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
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("surveyDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date surveyDate;
    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
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
     * Controller role
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
    @JsonProperty("javaVersion")
    private String javaVersion;
    /**
     * Controller platform
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
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("surveyDate")
    public Date getSurveyDate() {
        return surveyDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("surveyDate")
    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
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
        if (url != null && !"/".equals(url) && url.endsWith("/")) {
            url = url.replaceFirst("/$", ""); 
        }
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
     * Controller role
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
     * Controller role
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

    @JsonProperty("javaVersion")
    public String getJavaVersion() {
        return javaVersion;
    }

    @JsonProperty("javaVersion")
    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    /**
     * Controller platform
     * <p>
     * 
     * 
     */
    @JsonProperty("os")
    public OperatingSystem getOs() {
        return os;
    }

    /**
     * Controller platform
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
        return new ToStringBuilder(this).append("id", id).append("surveyDate", surveyDate).append("controllerId", controllerId).append("title", title).append("host", host).append("url", url).append("clusterUrl", clusterUrl).append("role", role).append("isCoupled", isCoupled).append("startedAt", startedAt).append("version", version).append("javaVersion", javaVersion).append("os", os).append("securityLevel", securityLevel).append("componentState", componentState).append("connectionState", connectionState).append("clusterNodeState", clusterNodeState).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(role).append(surveyDate).append(controllerId).append(os).append(javaVersion).append(connectionState).append(clusterUrl).append(startedAt).append(title).append(version).append(url).append(securityLevel).append(componentState).append(isCoupled).append(host).append(clusterNodeState).append(id).toHashCode();
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
        return new EqualsBuilder().append(role, rhs.role).append(surveyDate, rhs.surveyDate).append(controllerId, rhs.controllerId).append(os, rhs.os).append(javaVersion, rhs.javaVersion).append(connectionState, rhs.connectionState).append(clusterUrl, rhs.clusterUrl).append(startedAt, rhs.startedAt).append(title, rhs.title).append(version, rhs.version).append(url, rhs.url).append(securityLevel, rhs.securityLevel).append(componentState, rhs.componentState).append(isCoupled, rhs.isCoupled).append(host, rhs.host).append(clusterNodeState, rhs.clusterNodeState).append(id, rhs.id).isEquals();
    }

}
