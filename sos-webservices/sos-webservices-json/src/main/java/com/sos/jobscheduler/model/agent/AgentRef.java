
package com.sos.jobscheduler.model.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.jobscheduler.model.deploy.DeployType;
import com.sos.joc.model.common.IJSObject;
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
    "path",
    "versionId",
    "uri",
    "maxProcesses",
    "documentationId",
    "title"
})
public class AgentRef implements IJSObject
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
     * (Required)
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String path;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("versionId")
    private String versionId;
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
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("documentationId")
    private Long documentationId;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    private String title;

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
     * @param documentationId
     * @param title
     * @param uri
     */
    public AgentRef(String path, String versionId, String uri, Integer maxProcesses, Long documentationId, String title) {
        super();
        this.path = path;
        this.versionId = versionId;
        this.uri = uri;
        this.maxProcesses = maxProcesses;
        this.documentationId = documentationId;
        this.title = title;
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
     * (Required)
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
     * (Required)
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
     * 
     */
    @JsonProperty("versionId")
    public String getVersionId() {
        return versionId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("versionId")
    public void setVersionId(String versionId) {
        this.versionId = versionId;
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

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("documentationId")
    public Long getDocumentationId() {
        return documentationId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("documentationId")
    public void setDocumentationId(Long documentationId) {
        this.documentationId = documentationId;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("path", path).append("versionId", versionId).append("uri", uri).append("maxProcesses", maxProcesses).append("documentationId", documentationId).append("title", title).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(maxProcesses).append(path).append(versionId).append(documentationId).append(tYPE).append(title).append(uri).toHashCode();
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
        return new EqualsBuilder().append(maxProcesses, rhs.maxProcesses).append(path, rhs.path).append(versionId, rhs.versionId).append(documentationId, rhs.documentationId).append(tYPE, rhs.tYPE).append(title, rhs.title).append(uri, rhs.uri).isEquals();
    }

}
