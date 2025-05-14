
package com.sos.joc.model.security.foureyes;

import java.util.ArrayList;
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
 * FourEyesRequests
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "requests"
})
public class FourEyesRequests {

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
    private List<FourEyesRequest> requests = new ArrayList<FourEyesRequest>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public FourEyesRequests() {
    }

    /**
     * 
     * @param requests
     * @param deliveryDate
     */
    public FourEyesRequests(Date deliveryDate, List<FourEyesRequest> requests) {
        super();
        this.deliveryDate = deliveryDate;
        this.requests = requests;
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
    public List<FourEyesRequest> getRequests() {
        return requests;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("requests")
    public void setRequests(List<FourEyesRequest> requests) {
        this.requests = requests;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("requests", requests).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deliveryDate).append(requests).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FourEyesRequests) == false) {
            return false;
        }
        FourEyesRequests rhs = ((FourEyesRequests) other);
        return new EqualsBuilder().append(deliveryDate, rhs.deliveryDate).append(requests, rhs.requests).isEquals();
    }

}
