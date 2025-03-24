
package com.sos.controller.model.workflow;

import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * WorkflowBoardsV
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "criticalOrderIds"
})
public class WorkflowBoardsV
    extends WorkflowBoards
{

    @JsonProperty("criticalOrderIds")
    private Set<String> criticalOrderIds = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public WorkflowBoardsV() {
    }

    /**
     * 
     * @param expectNotices
     * @param path
     * @param versionId
     * @param criticalOrderIds
     * @param postNotices
     * @param consumeNotices
     * @param workflowTags
     */
    public WorkflowBoardsV(Set<String> criticalOrderIds, List<String> postNotices, List<String> expectNotices, List<String> consumeNotices, Set<String> workflowTags, String path, String versionId) {
        super(postNotices, expectNotices, consumeNotices, workflowTags, path, versionId);
        this.criticalOrderIds = criticalOrderIds;
    }

    @JsonProperty("criticalOrderIds")
    public Set<String> getCriticalOrderIds() {
        return criticalOrderIds;
    }

    @JsonProperty("criticalOrderIds")
    public void setCriticalOrderIds(Set<String> criticalOrderIds) {
        this.criticalOrderIds = criticalOrderIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("criticalOrderIds", criticalOrderIds).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(criticalOrderIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WorkflowBoardsV) == false) {
            return false;
        }
        WorkflowBoardsV rhs = ((WorkflowBoardsV) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(criticalOrderIds, rhs.criticalOrderIds).isEquals();
    }

}
