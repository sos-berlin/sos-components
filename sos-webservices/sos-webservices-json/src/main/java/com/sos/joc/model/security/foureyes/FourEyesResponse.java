
package com.sos.joc.model.security.foureyes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.CategoryType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * FourEyesResponse
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "message",
    "approvers"
})
public class FourEyesResponse
    extends ApprovalBase
{

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("message")
    private String message;
    @JsonProperty("approvers")
    private List<Approver> approvers = new ArrayList<Approver>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public FourEyesResponse() {
    }

    /**
     * 
     * @param requestBody
     * @param requestUrl
     * @param approvers
     * @param deliveryDate
     * @param message
     * @param category
     * @param requestor
     */
    public FourEyesResponse(Date deliveryDate, String message, List<Approver> approvers, String requestor, String requestUrl, RequestBody requestBody, CategoryType category) {
        super(requestor, requestUrl, requestBody, category);
        this.deliveryDate = deliveryDate;
        this.message = message;
        this.approvers = approvers;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
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
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty("approvers")
    public List<Approver> getApprovers() {
        return approvers;
    }

    @JsonProperty("approvers")
    public void setApprovers(List<Approver> approvers) {
        this.approvers = approvers;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("deliveryDate", deliveryDate).append("message", message).append("approvers", approvers).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(approvers).append(deliveryDate).append(message).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FourEyesResponse) == false) {
            return false;
        }
        FourEyesResponse rhs = ((FourEyesResponse) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(approvers, rhs.approvers).append(deliveryDate, rhs.deliveryDate).append(message, rhs.message).isEquals();
    }

}
