
package com.sos.joc.model.deploy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sos.jobscheduler.model.deploy.DeployType;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * abstract super class for editing all JobScheduler Objects
 * <p>
 * oldPath is used for a move/rename, auditLog only for deploy
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "state",
    "objectType",
    "content",
    "auditLog"
})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "objectType", visible = true)
@JsonSubTypes({
	@JsonSubTypes.Type(value = com.sos.jobscheduler.model.workflow.Workflow.class, name = "Workflow"),
	@JsonSubTypes.Type(value = com.sos.jobscheduler.model.agent.AgentRef.class, name = "AgentRef")})
public class JSObjectEdit {

    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    @JsonProperty("state")
    private String state;
    @JsonProperty("objectType")
    private DeployType objectType;
    /**
     * interface for different json representations of a configuration item
     * 
     */
    @JsonProperty("content")
    @JsonPropertyDescription("interface for different json representations of a configuration item")
    private IJSObject content;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    @JsonProperty("state")
    public String getState() {
        return state;
    }

    @JsonProperty("state")
    public void setState(String state) {
        this.state = state;
    }

    @JsonProperty("objectType")
    public DeployType getObjectType() {
        return objectType;
    }

    @JsonProperty("objectType")
    public void setObjectType(DeployType objectType) {
        this.objectType = objectType;
    }

    /**
     * interface for different json representations of a configuration item
     * 
     */
    @JsonProperty("content")
    public IJSObject getContent() {
        return content;
    }

    /**
     * interface for different json representations of a configuration item
     * 
     */
    @JsonProperty("content")
    public void setContent(IJSObject content) {
        this.content = content;
    }

    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    public AuditParams getAuditLog() {
        return auditLog;
    }

    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    public void setAuditLog(AuditParams auditLog) {
        this.auditLog = auditLog;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("state", state)
        		.append("objectType", objectType).append("content", content).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(state).append(jobschedulerId).append(auditLog).append(content)
        		.append(objectType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JSObjectEdit) == false) {
            return false;
        }
        JSObjectEdit rhs = ((JSObjectEdit) other);
        return new EqualsBuilder().append(state, rhs.state).append(jobschedulerId, rhs.jobschedulerId).append(auditLog, rhs.auditLog)
        		.append(content, rhs.content).append(objectType, rhs.objectType).isEquals();
    }

}
