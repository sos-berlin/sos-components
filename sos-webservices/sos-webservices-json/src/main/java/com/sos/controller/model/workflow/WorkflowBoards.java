
package com.sos.controller.model.workflow;

import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
    "consumeNotices"
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
     * @param postNotices
     * @param consumeNotices
     * @param workflowTags
     */
    public WorkflowBoards(List<String> postNotices, List<String> expectNotices, List<String> consumeNotices, Set<String> workflowTags, String path, String versionId) {
        super(workflowTags, path, versionId);
        this.postNotices = postNotices;
        this.expectNotices = expectNotices;
        this.consumeNotices = consumeNotices;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("postNotices", postNotices).append("expectNotices", expectNotices).append("consumeNotices", consumeNotices).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(expectNotices).append(postNotices).append(consumeNotices).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(expectNotices, rhs.expectNotices).append(postNotices, rhs.postNotices).append(consumeNotices, rhs.consumeNotices).isEquals();
    }

}
