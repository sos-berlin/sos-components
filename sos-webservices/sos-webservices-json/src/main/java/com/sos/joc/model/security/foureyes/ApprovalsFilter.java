
package com.sos.joc.model.security.foureyes;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * ApprovalsFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "requestors",
    "approvers",
    "requestorStates",
    "approverStates"
})
public class ApprovalsFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("requestors")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> requestors = new LinkedHashSet<String>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("approvers")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> approvers = new LinkedHashSet<String>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("requestorStates")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<RequestorState> requestorStates = new LinkedHashSet<RequestorState>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("approverStates")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ApproverState> approverStates = new LinkedHashSet<ApproverState>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public ApprovalsFilter() {
    }

    /**
     * 
     * @param requestorStates
     * @param requestors
     * @param approverStates
     * @param approvers
     */
    public ApprovalsFilter(Set<String> requestors, Set<String> approvers, Set<RequestorState> requestorStates, Set<ApproverState> approverStates) {
        super();
        this.requestors = requestors;
        this.approvers = approvers;
        this.requestorStates = requestorStates;
        this.approverStates = approverStates;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("requestors")
    public Set<String> getRequestors() {
        return requestors;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("requestors")
    public void setRequestors(Set<String> requestors) {
        this.requestors = requestors;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("approvers")
    public Set<String> getApprovers() {
        return approvers;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("approvers")
    public void setApprovers(Set<String> approvers) {
        this.approvers = approvers;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("requestorStates")
    public Set<RequestorState> getRequestorStates() {
        return requestorStates;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("requestorStates")
    public void setRequestorStates(Set<RequestorState> requestorStates) {
        this.requestorStates = requestorStates;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("approverStates")
    public Set<ApproverState> getApproverStates() {
        return approverStates;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("approverStates")
    public void setApproverStates(Set<ApproverState> approverStates) {
        this.approverStates = approverStates;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("requestors", requestors).append("approvers", approvers).append("requestorStates", requestorStates).append("approverStates", approverStates).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(approverStates).append(approvers).append(requestorStates).append(requestors).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ApprovalsFilter) == false) {
            return false;
        }
        ApprovalsFilter rhs = ((ApprovalsFilter) other);
        return new EqualsBuilder().append(approverStates, rhs.approverStates).append(approvers, rhs.approvers).append(requestorStates, rhs.requestorStates).append(requestors, rhs.requestors).isEquals();
    }

}
