
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
 * WorkflowBoards
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "postNotices",
    "expectNotices",
    "consumeNotices",
    "expectNoticesExpressions",
    "consumeNoticesExpressions"
})
public class WorkflowBoards
    extends WorkflowIdAndTags
{

    @JsonProperty("postNotices")
    private List<String> postNotices = null;
    @JsonProperty("expectNotices")
    private List<String> expectNotices = null;
    @JsonProperty("consumeNotices")
    private List<String> consumeNotices = null;
    @JsonProperty("expectNoticesExpressions")
    private WorkflowBoardExpressions expectNoticesExpressions;
    @JsonProperty("consumeNoticesExpressions")
    private WorkflowBoardExpressions consumeNoticesExpressions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public WorkflowBoards() {
    }

    /**
     * 
     * @param expectNotices
     * @param path
     * @param versionId
     * @param consumeNoticesExpressions
     * @param postNotices
     * @param consumeNotices
     * @param workflowTags
     * @param expectNoticesExpressions
     */
    public WorkflowBoards(List<String> postNotices, List<String> expectNotices, List<String> consumeNotices, WorkflowBoardExpressions expectNoticesExpressions, WorkflowBoardExpressions consumeNoticesExpressions, Set<String> workflowTags, String path, String versionId) {
        super(workflowTags, path, versionId);
        this.postNotices = postNotices;
        this.expectNotices = expectNotices;
        this.consumeNotices = consumeNotices;
        this.expectNoticesExpressions = expectNoticesExpressions;
        this.consumeNoticesExpressions = consumeNoticesExpressions;
    }

    @JsonProperty("postNotices")
    public List<String> getPostNotices() {
        return postNotices;
    }

    @JsonProperty("postNotices")
    public void setPostNotices(List<String> postNotices) {
        this.postNotices = postNotices;
    }

    @JsonProperty("expectNotices")
    public List<String> getExpectNotices() {
        return expectNotices;
    }

    @JsonProperty("expectNotices")
    public void setExpectNotices(List<String> expectNotices) {
        this.expectNotices = expectNotices;
    }

    @JsonProperty("consumeNotices")
    public List<String> getConsumeNotices() {
        return consumeNotices;
    }

    @JsonProperty("consumeNotices")
    public void setConsumeNotices(List<String> consumeNotices) {
        this.consumeNotices = consumeNotices;
    }

    @JsonProperty("expectNoticesExpressions")
    public WorkflowBoardExpressions getExpectNoticesExpressions() {
        return expectNoticesExpressions;
    }

    @JsonProperty("expectNoticesExpressions")
    public void setExpectNoticesExpressions(WorkflowBoardExpressions expectNoticesExpressions) {
        this.expectNoticesExpressions = expectNoticesExpressions;
    }

    @JsonProperty("consumeNoticesExpressions")
    public WorkflowBoardExpressions getConsumeNoticesExpressions() {
        return consumeNoticesExpressions;
    }

    @JsonProperty("consumeNoticesExpressions")
    public void setConsumeNoticesExpressions(WorkflowBoardExpressions consumeNoticesExpressions) {
        this.consumeNoticesExpressions = consumeNoticesExpressions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("postNotices", postNotices).append("expectNotices", expectNotices).append("consumeNotices", consumeNotices).append("expectNoticesExpressions", expectNoticesExpressions).append("consumeNoticesExpressions", consumeNoticesExpressions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(expectNotices).append(postNotices).append(consumeNotices).append(expectNoticesExpressions).append(consumeNoticesExpressions).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WorkflowBoards) == false) {
            return false;
        }
        WorkflowBoards rhs = ((WorkflowBoards) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(expectNotices, rhs.expectNotices).append(postNotices, rhs.postNotices).append(consumeNotices, rhs.consumeNotices).append(expectNoticesExpressions, rhs.expectNoticesExpressions).append(consumeNoticesExpressions, rhs.consumeNoticesExpressions).isEquals();
    }

}
