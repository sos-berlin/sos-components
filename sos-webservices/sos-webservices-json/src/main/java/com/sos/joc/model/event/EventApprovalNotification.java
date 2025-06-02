
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
    "numOfApprovedRequests",
    "numOfRejectedRequests",
    "requestor"
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
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfApprovedRequests")
    private Long numOfApprovedRequests;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfRejectedRequests")
    private Long numOfRejectedRequests;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("requestor")
    private String requestor;

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
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfApprovedRequests")
    public Long getNumOfApprovedRequests() {
        return numOfApprovedRequests;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfApprovedRequests")
    public void setNumOfApprovedRequests(Long numOfApprovedRequests) {
        this.numOfApprovedRequests = numOfApprovedRequests;
    }
    
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfRejectedRequests")
    public Long getNumOfRejectedRequests() {
        return numOfRejectedRequests;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfRejectedRequests")
    public void setNumOfRejectedRequests(Long numOfRejectedRequests) {
        this.numOfRejectedRequests = numOfRejectedRequests;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("eventId", eventId).append("eventType", eventType).append("approver", approver).append("numOfPendingApprovals", numOfPendingApprovals).append("numOfApprovedRequests", numOfApprovedRequests).append("numOfRejectedRequests", numOfRejectedRequests).append("requestor", requestor).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(approver).append(eventId).append(eventType).append(numOfPendingApprovals).append(numOfApprovedRequests).append(numOfApprovedRequests).append(requestor).toHashCode();
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
        return new EqualsBuilder().append(approver, rhs.approver).append(eventId, rhs.eventId).append(eventType, rhs.eventType).append(numOfPendingApprovals, rhs.numOfPendingApprovals).append(numOfApprovedRequests, rhs.numOfApprovedRequests).append(numOfRejectedRequests, rhs.numOfRejectedRequests).append(requestor, rhs.requestor).isEquals();
    }

}
