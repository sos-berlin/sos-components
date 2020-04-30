package com.sos.joc.model.publish;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
    "editAccount",
    "publishAccount",
    "path",
    "objectType",
    "content",
    "signedContent",
    "version",
    "parentVersion",
    "comment",
    "modified"
})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "objectType", visible = true)
@JsonSubTypes({ 
	@JsonSubTypes.Type(value = com.sos.jobscheduler.model.workflow.WorkflowEdit.class, name = "Workflow"),
	@JsonSubTypes.Type(value = com.sos.jobscheduler.model.agent.AgentRefEdit.class, name = "AgentRef")})
public class JSObject {

    /**
     * non negative long
     */
    @JsonProperty("id")
    private Long id;
    @JsonProperty("editAccount")
    private String editAccount;
    @JsonProperty("publishAccount")
    private String publishAccount;
    @JsonProperty("path")
    private String path;
    @JsonProperty("objectType")
    private DeployType objectType;
    @JsonProperty("content")
    private IJSObject content;
    @JsonProperty("signedContent")
    private String signedContent;
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
     */
    @JsonProperty("modified")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date modified;

    /**
     * non negative long
     */
    @JsonProperty("id")
    public Long getId() {
        return id;
    }
    /**
     * non negative long
     */
    @JsonProperty("id")
    public void setId(Long id) {
        this.id = id;
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

    @JsonProperty("objectType")
    public DeployType getObjectType() {
        return objectType;
    }
    @JsonProperty("objectType")
    public void setObjectType(DeployType objectType) {
        this.objectType = objectType;
    }

    @JsonProperty("content")
    public IJSObject getContent() {
        return content;
    }
    @JsonProperty("content")
    public void setContent(IJSObject content) {
        this.content = content;
    }

    @JsonProperty("signedContent")
    public String getSignedContent() {
        return signedContent;
    }
    @JsonProperty("signedContent")
    public void setSignedContent(String signedContent) {
        this.signedContent = signedContent;
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
     */
    @JsonProperty("modified")
    public Date getModified() {
        return modified;
    }
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     */
    @JsonProperty("modified")
    public void setModified(Date modified) {
        this.modified = modified;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("id", id).append("editAccount", editAccount)
                .append("publishAccount", publishAccount).append("path", path).append("objectType", objectType).append("content", content)
                .append("signedContent", signedContent).append("version", version).append("parentVersion", parentVersion).append("comment", comment)
                .append("modified", modified).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(id).append(editAccount).append(publishAccount).append(path).append(objectType)
                .append(content).append(signedContent).append(version).append(parentVersion).append(comment).append(modified).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(id, rhs.id).append(editAccount, rhs.editAccount)
                .append(publishAccount, rhs.publishAccount).append(path, rhs.path).append(objectType, rhs.objectType).append(content, rhs.content)
                .append(signedContent, rhs.signedContent).append(version, rhs.version).append(parentVersion, rhs.parentVersion)
                .append(comment, rhs.comment).append(modified, rhs.modified).isEquals();
    }

}
