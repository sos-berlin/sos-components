
package com.sos.jobscheduler.model.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.jobscheduler.model.deploy.DeployType;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.common.IDeployObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * agent
 * <p>
 * deploy object with fixed property 'TYPE':'AgentRef'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "name",
    "uri",
    "path",
    "maxProcesses",
    "title",
    "versionId"
})
public class AgentRef implements IConfigurationObject, IDeployObject
{

    /**
     * deployType
     * <p>
     * 
     * 
     */
    @JsonProperty("TYPE")
    private DeployType tYPE = DeployType.AGENTREF;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String path;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("uri")
    private String uri;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("maxProcesses")
    private Integer maxProcesses;
    @JsonProperty("title")
    private String title;
    @JsonProperty("versionId")
    private String versionId;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AgentRef() {
    }

    /**
     * 
     * @param maxProcesses
     * @param path
     * @param versionId
     * @param name
     * 
     * @param title
     * @param uri
     */
    public AgentRef(String path, String name, String uri, Integer maxProcesses, String title, String versionId) {
        super();
        this.path = path;
        this.name = name;
        this.uri = uri;
        this.maxProcesses = maxProcesses;
        this.title = title;
        this.versionId = versionId;
    }

    /**
     * deployType
     * <p>
     * 
     * 
     */
    @JsonProperty("TYPE")
    public DeployType getTYPE() {
        return tYPE;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("uri")
    public String getUri() {
        return uri;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("uri")
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("maxProcesses")
    public Integer getMaxProcesses() {
        return maxProcesses;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("maxProcesses")
    public void setMaxProcesses(Integer maxProcesses) {
        this.maxProcesses = maxProcesses;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("versionId")
    public String getVersionId() {
        return versionId;
    }

    @JsonProperty("versionId")
    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("path", path).append("name", name).append("uri", uri).append("maxProcesses", maxProcesses).append("title", title).append("versionId", versionId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(maxProcesses).append(path).append(versionId).append(name).append(tYPE).append(title).append(uri).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AgentRef) == false) {
            return false;
        }
        AgentRef rhs = ((AgentRef) other);
        return new EqualsBuilder().append(maxProcesses, rhs.maxProcesses).append(path, rhs.path).append(versionId, rhs.versionId).append(name, rhs.name).append(tYPE, rhs.tYPE).append(title, rhs.title).append(uri, rhs.uri).isEquals();
    }

}
