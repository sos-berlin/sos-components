
package com.sos.joc.model.security.foureyes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * ApprovalRequests
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "requests",
    "approvers"
})
public class ApprovalRequests {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("requests")
    private List<ApprovalRequest> requests = new ArrayList<ApprovalRequest>();
    @JsonProperty("approvers")
    private Collection<Approver> approvers = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ApprovalRequests() {
    }

    /**
     * 
     * @param approvers
     * @param requests
     * @param deliveryDate
     */
    public ApprovalRequests(Date deliveryDate, List<ApprovalRequest> requests, List<Approver> approvers) {
        super();
        this.deliveryDate = deliveryDate;
        this.requests = requests;
        this.approvers = approvers;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("requests")
    public List<ApprovalRequest> getRequests() {
        return requests;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("requests")
    public void setRequests(List<ApprovalRequest> requests) {
        this.requests = requests;
    }

    @JsonProperty("approvers")
    public List<Approver> getApprovers() {
        return approvers;
    }

    @JsonProperty("approvers")
    public void setApprovers(Collection<Approver> approvers) {
        this.approvers = approvers;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("requests", requests).append("approvers", approvers).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(approvers).append(requests).append(deliveryDate).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ApprovalRequests) == false) {
            return false;
        }
        ApprovalRequests rhs = ((ApprovalRequests) other);
        return new EqualsBuilder().append(approvers, rhs.approvers).append(requests, rhs.requests).append(deliveryDate, rhs.deliveryDate).isEquals();
    }

}
