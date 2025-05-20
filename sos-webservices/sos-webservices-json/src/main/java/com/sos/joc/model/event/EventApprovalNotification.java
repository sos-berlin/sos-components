
package com.sos.joc.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.IEventObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * event from approval notification
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "eventId",
    "eventType",
    "approver",
    "numOfPendingApprovals",
    "requestor",
    "approverState"
})
public class EventApprovalNotification implements IEventObject
{

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("eventId")
    private Long eventId;
    /**
     * ApproverNotification, RequestorNotification
     * (Required)
     * 
     */
    @JsonProperty("eventType")
    @JsonPropertyDescription("ApproverNotification, RequestorNotification")
    private String eventType;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("approver")
    private String approver;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfPendingApprovals")
    private Long numOfPendingApprovals;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("requestor")
    private String requestor;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("approverState")
    private String approverState;

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("eventId")
    public Long getEventId() {
        return eventId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("eventId")
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    /**
     * ApproverNotification, RequestorNotification
     * (Required)
     * 
     */
    @JsonProperty("eventType")
    public String getEventType() {
        return eventType;
    }

    /**
     * ApproverNotification, RequestorNotification
     * (Required)
     * 
     */
    @JsonProperty("eventType")
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("approver")
    public String getApprover() {
        return approver;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("approver")
    public void setApprover(String approver) {
        this.approver = approver;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfPendingApprovals")
    public Long getNumOfPendingApprovals() {
        return numOfPendingApprovals;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfPendingApprovals")
    public void setNumOfPendingApprovals(Long numOfPendingApprovals) {
        this.numOfPendingApprovals = numOfPendingApprovals;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("requestor")
    public String getRequestor() {
        return requestor;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("requestor")
    public void setRequestor(String requestor) {
        this.requestor = requestor;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("approverState")
    public String getApproverState() {
        return approverState;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("approverState")
    public void setApproverState(String approverState) {
        this.approverState = approverState;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("eventId", eventId).append("eventType", eventType).append("approver", approver).append("numOfPendingApprovals", numOfPendingApprovals).append("requestor", requestor).append("approverState", approverState).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(approver).append(approverState).append(eventId).append(eventType).append(numOfPendingApprovals).append(requestor).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof EventApprovalNotification) == false) {
            return false;
        }
        EventApprovalNotification rhs = ((EventApprovalNotification) other);
        return new EqualsBuilder().append(approver, rhs.approver).append(approverState, rhs.approverState).append(eventId, rhs.eventId).append(eventType, rhs.eventType).append(numOfPendingApprovals, rhs.numOfPendingApprovals).append(requestor, rhs.requestor).isEquals();
    }

}
