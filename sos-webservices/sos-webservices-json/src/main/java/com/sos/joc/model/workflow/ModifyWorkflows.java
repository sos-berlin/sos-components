
package com.sos.joc.model.workflow;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.common.Folder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * ModifyWorkflows (suspend, resume)
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "workflowPaths",
    "folders",
    "workflowTags",
    "all",
    "auditLog"
})
public class ModifyWorkflows {

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    @JsonProperty("workflowPaths")
    private List<String> workflowPaths = new ArrayList<String>();
    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    private List<Folder> folders = new ArrayList<Folder>();
    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowTags")
    @JsonAlias({
        "tags"
    })
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> workflowTags = new LinkedHashSet<String>();
    @JsonProperty("all")
    private Boolean all = false;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

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

    @JsonProperty("workflowPaths")
    public List<String> getWorkflowPaths() {
        return workflowPaths;
    }

    @JsonProperty("workflowPaths")
    public void setWorkflowPaths(List<String> workflowPaths) {
        this.workflowPaths = workflowPaths;
    }

    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    public List<Folder> getFolders() {
        return folders;
    }

    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowTags")
    public Set<String> getWorkflowTags() {
        return workflowTags;
    }

    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowTags")
    public void setWorkflowTags(Set<String> workflowTags) {
        this.workflowTags = workflowTags;
    }

    @JsonProperty("all")
    public Boolean getAll() {
        return all;
    }

    @JsonProperty("all")
    public void setAll(Boolean all) {
        this.all = all;
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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("workflowPaths", workflowPaths).append("folders", folders).append("workflowTags", workflowTags).append("all", all).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(all).append(folders).append(controllerId).append(auditLog).append(workflowPaths).append(workflowTags).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ModifyWorkflows) == false) {
            return false;
        }
        ModifyWorkflows rhs = ((ModifyWorkflows) other);
        return new EqualsBuilder().append(all, rhs.all).append(folders, rhs.folders).append(controllerId, rhs.controllerId).append(auditLog, rhs.auditLog).append(workflowPaths, rhs.workflowPaths).append(workflowTags, rhs.workflowTags).isEquals();
    }

}
