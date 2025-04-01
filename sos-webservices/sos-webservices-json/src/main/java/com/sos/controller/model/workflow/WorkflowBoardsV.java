
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
 * WorkflowBoardsV
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "presentDueOrderIds",
    "futureDueOrderIds",
    "numOfExpectingOrders",
    "expectingOrderIds"
})
public class WorkflowBoardsV
    extends WorkflowBoards
{

    @JsonProperty("presentDueOrderIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> presentDueOrderIds = null;
    @JsonProperty("futureDueOrderIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> futureDueOrderIds = null;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfExpectingOrders")
    private Integer numOfExpectingOrders;
    @JsonProperty("expectingOrderIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> expectingOrderIds = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public WorkflowBoardsV() {
    }

    /**
     * 
     * @param expectNotices
     * @param postNotices
     * @param consumeNotices
     * @param expectingOrderIds
     * @param path
     * @param versionId
     * @param numOfExpectingOrders
     * @param futureDueOrderIds
     * @param workflowTags
     * @param presentDueOrderIds
     */
    public WorkflowBoardsV(Set<String> presentDueOrderIds, Set<String> futureDueOrderIds, Integer numOfExpectingOrders, Set<String> expectingOrderIds, List<String> postNotices, List<String> expectNotices, List<String> consumeNotices, Set<String> workflowTags, String path, String versionId) {
        super(postNotices, expectNotices, consumeNotices, workflowTags, path, versionId);
        this.presentDueOrderIds = presentDueOrderIds;
        this.futureDueOrderIds = futureDueOrderIds;
        this.numOfExpectingOrders = numOfExpectingOrders;
        this.expectingOrderIds = expectingOrderIds;
    }

    @JsonProperty("presentDueOrderIds")
    public Set<String> getPresentDueOrderIds() {
        return presentDueOrderIds;
    }

    @JsonProperty("presentDueOrderIds")
    public void setPresentDueOrderIds(Set<String> presentDueOrderIds) {
        this.presentDueOrderIds = presentDueOrderIds;
    }

    @JsonProperty("futureDueOrderIds")
    public Set<String> getFutureDueOrderIds() {
        return futureDueOrderIds;
    }

    @JsonProperty("futureDueOrderIds")
    public void setFutureDueOrderIds(Set<String> futureDueOrderIds) {
        this.futureDueOrderIds = futureDueOrderIds;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfExpectingOrders")
    public Integer getNumOfExpectingOrders() {
        return numOfExpectingOrders;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfExpectingOrders")
    public void setNumOfExpectingOrders(Integer numOfExpectingOrders) {
        this.numOfExpectingOrders = numOfExpectingOrders;
    }

    @JsonProperty("expectingOrderIds")
    public Set<String> getExpectingOrderIds() {
        return expectingOrderIds;
    }

    @JsonProperty("expectingOrderIds")
    public void setExpectingOrderIds(Set<String> expectingOrderIds) {
        this.expectingOrderIds = expectingOrderIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("presentDueOrderIds", presentDueOrderIds).append("futureDueOrderIds", futureDueOrderIds).append("numOfExpectingOrders", numOfExpectingOrders).append("expectingOrderIds", expectingOrderIds).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(numOfExpectingOrders).append(futureDueOrderIds).append(expectingOrderIds).append(presentDueOrderIds).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(numOfExpectingOrders, rhs.numOfExpectingOrders).append(futureDueOrderIds, rhs.futureDueOrderIds).append(expectingOrderIds, rhs.expectingOrderIds).append(presentDueOrderIds, rhs.presentDueOrderIds).isEquals();
    }

}
