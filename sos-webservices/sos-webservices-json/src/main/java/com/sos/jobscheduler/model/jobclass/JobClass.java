
package com.sos.jobscheduler.model.jobclass;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.jobscheduler.model.deploy.DeployObject;
import com.sos.joc.model.common.IJSObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * jobClass
 * <p>
 * deploy object with fixed property 'TYPE':'jobClass'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "path",
    "versionId",
    "maxProcesses",
    "priority",
    "documentationId",
    "title"
})
public class JobClass
    extends DeployObject
    implements IJSObject
{

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
     * (Required)
     * 
     */
    @JsonProperty("versionId")
    private String versionId;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("maxProcesses")
    private Integer maxProcesses;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("priority")
    private String priority;
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
    public JobClass() {
    }

    /**
     * 
     * @param maxProcesses
     * @param path
     * @param versionId
     * @param priority
     */
    public JobClass(String path, String versionId, Integer maxProcesses, String priority) {
        super();
        this.path = path;
        this.versionId = versionId;
        this.maxProcesses = maxProcesses;
        this.priority = priority;
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
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("versionId")
    public void setVersionId(String versionId) {
        this.versionId = versionId;
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
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("priority")
    public String getPriority() {
        return priority;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("priority")
    public void setPriority(String priority) {
        this.priority = priority;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("path", path).append("versionId", versionId).append("maxProcesses", maxProcesses).append("priority", priority).append("documentationId", documentationId).append("title", title).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(maxProcesses).append(path).append(versionId).append(documentationId).append(priority).append(title).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobClass) == false) {
            return false;
        }
        JobClass rhs = ((JobClass) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(maxProcesses, rhs.maxProcesses).append(path, rhs.path).append(versionId, rhs.versionId).append(documentationId, rhs.documentationId).append(priority, rhs.priority).append(title, rhs.title).isEquals();
    }

}
