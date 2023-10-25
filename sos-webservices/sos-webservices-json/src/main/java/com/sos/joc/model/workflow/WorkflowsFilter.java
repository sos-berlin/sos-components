
package com.sos.joc.model.workflow;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.controller.model.common.SyncStateText;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.workflow.search.InstructionStateText;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * workflowsFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "workflowIds",
    "compact",
    "folders",
    "tags",
    "states",
    "instructionStates",
    "regex",
    "agentNames"
})
public class WorkflowsFilter {

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    @JsonProperty("workflowIds")
    private List<WorkflowId> workflowIds = new ArrayList<WorkflowId>();
    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    @JsonPropertyDescription("controls if the object's data is compact or detailed")
    private Boolean compact = false;
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
    @JsonProperty("tags")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> tags = new LinkedHashSet<String>();
    @JsonProperty("states")
    private List<SyncStateText> states = new ArrayList<SyncStateText>();
    @JsonProperty("instructionStates")
    private List<InstructionStateText> instructionStates = new ArrayList<InstructionStateText>();
    /**
     * filter with regex
     * <p>
     * regular expression to filter Controller objects by matching the path
     * 
     */
    @JsonProperty("regex")
    @JsonPropertyDescription("regular expression to filter Controller objects by matching the path")
    private String regex;
    @JsonProperty("agentNames")
    private List<String> agentNames = new ArrayList<String>();

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

    @JsonProperty("workflowIds")
    public List<WorkflowId> getWorkflowIds() {
        return workflowIds;
    }

    @JsonProperty("workflowIds")
    public void setWorkflowIds(List<WorkflowId> workflowIds) {
        this.workflowIds = workflowIds;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    public Boolean getCompact() {
        return compact;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    public void setCompact(Boolean compact) {
        this.compact = compact;
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
    @JsonProperty("tags")
    public Set<String> getTags() {
        return tags;
    }

    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("tags")
    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    @JsonProperty("states")
    public List<SyncStateText> getStates() {
        return states;
    }

    @JsonProperty("states")
    public void setStates(List<SyncStateText> states) {
        this.states = states;
    }

    @JsonProperty("instructionStates")
    public List<InstructionStateText> getInstructionStates() {
        return instructionStates;
    }

    @JsonProperty("instructionStates")
    public void setInstructionStates(List<InstructionStateText> instructionStates) {
        this.instructionStates = instructionStates;
    }

    /**
     * filter with regex
     * <p>
     * regular expression to filter Controller objects by matching the path
     * 
     */
    @JsonProperty("regex")
    public String getRegex() {
        return regex;
    }

    /**
     * filter with regex
     * <p>
     * regular expression to filter Controller objects by matching the path
     * 
     */
    @JsonProperty("regex")
    public void setRegex(String regex) {
        this.regex = regex;
    }

    @JsonProperty("agentNames")
    public List<String> getAgentNames() {
        return agentNames;
    }

    @JsonProperty("agentNames")
    public void setAgentNames(List<String> agentNames) {
        this.agentNames = agentNames;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("workflowIds", workflowIds).append("compact", compact).append("folders", folders).append("tags", tags).append("states", states).append("instructionStates", instructionStates).append("regex", regex).append("agentNames", agentNames).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(workflowIds).append(regex).append(folders).append(controllerId).append(compact).append(agentNames).append(tags).append(states).append(instructionStates).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WorkflowsFilter) == false) {
            return false;
        }
        WorkflowsFilter rhs = ((WorkflowsFilter) other);
        return new EqualsBuilder().append(workflowIds, rhs.workflowIds).append(regex, rhs.regex).append(folders, rhs.folders).append(controllerId, rhs.controllerId).append(compact, rhs.compact).append(agentNames, rhs.agentNames).append(tags, rhs.tags).append(states, rhs.states).append(instructionStates, rhs.instructionStates).isEquals();
    }

}
