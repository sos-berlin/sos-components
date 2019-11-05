
package com.sos.joc.model.deploy;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sos.jobscheduler.model.deploy.DeployObject;
import com.sos.jobscheduler.model.deploy.DeployType;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JS Object configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "jobschedulerId",
    "editAccount",
    "publishAccount",
    "path",
    "TYPE",
    "content",
    "uri",
    "state",
    "valid",
    "version",
    "parentVersion",
    "comment",
    "modified"
})
@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME, 
		include = JsonTypeInfo.As.PROPERTY, 
		property = "TYPE",
		visible = true)
@JsonSubTypes({ 
	@JsonSubTypes.Type(value = com.sos.jobscheduler.model.workflow.Workflow.class, name = "Workflow"),
	@JsonSubTypes.Type(value = com.sos.jobscheduler.model.agent.AgentRef.class, name = "AgentRef")})
public class JSObject 
	extends DeployObject {

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    private Long id;
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    @JsonProperty("editAccount")
    private String editAccount;
    @JsonProperty("publishAccount")
    private String publishAccount;
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String path;
    @JsonProperty("TYPE")
    private DeployType tYPE;
    @JsonProperty("content")
    private IJSObject content;
    @JsonProperty("uri")
    private String uri;
     @JsonProperty("state")
    private String state;
    @JsonProperty("valid")
    private Boolean valid;
    @JsonProperty("version")
    private String version;
    @JsonProperty("parentVersion")
    private String parentVersion;
    @JsonProperty("comment")
    private String comment;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("modified")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date modified;

    /**
     * non negative long
     * <p>
     * 
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
     * 
     */
    @JsonProperty("id")
    public void setId(Long id) {
        this.id = id;
    }

    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    @JsonProperty("editAccount")
    public String getEditAccount() {
        return editAccount;
    }

    @JsonProperty("editAccount")
    public void setEditAccount(String editAccount) {
        this.editAccount = editAccount;
    }

    @JsonProperty("publishAccount")
    public String getPublishAccount() {
        return publishAccount;
    }

    @JsonProperty("publishAccount")
    public void setPublishAccount(String publishAccount) {
        this.publishAccount = publishAccount;
    }

    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    @JsonProperty("TYPE")
    public DeployType getTYPE() {
        return tYPE;
    }

    @JsonProperty("TYPE")
    public void setTYPE(DeployType tYPE) {
        this.tYPE = tYPE;
    }

    @JsonProperty("content")
    public IJSObject getContent() {
        return content;
    }

    @JsonProperty("content")
    public void setContent(IJSObject content) {
        this.content = content;
    }

    @JsonProperty("uri")
    public String getUri() {
        return uri;
    }

    @JsonProperty("uri")
    public void setUri(String uri) {
        this.uri = uri;
    }

    @JsonProperty("state")
    public String getState() {
        return state;
    }

    @JsonProperty("state")
    public void setState(String state) {
        this.state = state;
    }

    @JsonProperty("valid")
    public Boolean getValid() {
        return valid;
    }

    @JsonProperty("valid")
    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("parentVersion")
    public String getParentVersion() {
        return parentVersion;
    }

    @JsonProperty("parentVersion")
    public void setParentVersion(String parentVersion) {
        this.parentVersion = parentVersion;
    }

    @JsonProperty("comment")
    public String getComment() {
        return comment;
    }

    @JsonProperty("comment")
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("modified")
    public Date getModified() {
        return modified;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("modified")
    public void setModified(Date modified) {
        this.modified = modified;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("id", id).append("jobschedulerId", jobschedulerId).append("editAccount", editAccount).append("publishAccount", publishAccount).append("path", path).append("tYPE", tYPE).append("content", content).append("uri", uri).append("state", state).append("valid", valid).append("version", version).append("parentVersion", parentVersion).append("comment", comment).append("modified", modified).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(tYPE).append(uri).append(version).append(content).append(valid).append(path).append(editAccount).append(publishAccount).append(modified).append(comment).append(id).append(state).append(jobschedulerId).append(parentVersion).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JSObject) == false) {
            return false;
        }
        JSObject rhs = ((JSObject) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(tYPE, rhs.tYPE).append(uri, rhs.uri).append(version, rhs.version).append(content, rhs.content).append(valid, rhs.valid).append(path, rhs.path).append(editAccount, rhs.editAccount).append(publishAccount, rhs.publishAccount).append(modified, rhs.modified).append(comment, rhs.comment).append(id, rhs.id).append(state, rhs.state).append(jobschedulerId, rhs.jobschedulerId).append(parentVersion, rhs.parentVersion).isEquals();
    }

}
