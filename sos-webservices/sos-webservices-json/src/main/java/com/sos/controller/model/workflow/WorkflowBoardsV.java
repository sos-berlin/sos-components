
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
    "dueOrderIds"
})
public class WorkflowBoardsV
    extends WorkflowBoards
{

    @JsonProperty("dueOrderIds")
    private Set<String> dueOrderIds = null;

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
     * @param dueOrderIds
     * @param postNotices
     * @param consumeNotices
     * @param workflowTags
     */
    public WorkflowBoardsV(Set<String> dueOrderIds, List<String> postNotices, List<String> expectNotices, List<String> consumeNotices, Set<String> workflowTags, String path, String versionId) {
        super(postNotices, expectNotices, consumeNotices, workflowTags, path, versionId);
        this.dueOrderIds = dueOrderIds;
    }

    @JsonProperty("dueOrderIds")
    public Set<String> getDueOrderIds() {
        return dueOrderIds;
    }

    @JsonProperty("dueOrderIds")
    public void setDueOrderIds(Set<String> criticalOrderIds) {
        this.dueOrderIds = criticalOrderIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("dueOrderIds", dueOrderIds).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(dueOrderIds).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(dueOrderIds, rhs.dueOrderIds).isEquals();
    }

}
