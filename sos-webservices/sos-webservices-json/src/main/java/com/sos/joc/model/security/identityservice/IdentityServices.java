
package com.sos.joc.model.security.identityservice;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Identiy Services
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "identityServiceItems",
    "identityServiceTypes"
})
public class IdentityServices {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    @JsonProperty("identityServiceItems")
    private List<IdentityService> identityServiceItems = new ArrayList<IdentityService>();
    @JsonProperty("identityServiceTypes")
    private List<IdentityServiceTypes> identityServiceTypes = new ArrayList<IdentityServiceTypes>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public IdentityServices() {
    }

    /**
     * 
     * @param identityServiceItems
     * @param identityServiceTypes
     * @param deliveryDate
     */
    public IdentityServices(Date deliveryDate, List<IdentityService> identityServiceItems, List<IdentityServiceTypes> identityServiceTypes) {
        super();
        this.deliveryDate = deliveryDate;
        this.identityServiceItems = identityServiceItems;
        this.identityServiceTypes = identityServiceTypes;
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

    @JsonProperty("identityServiceItems")
    public List<IdentityService> getIdentityServiceItems() {
        return identityServiceItems;
    }

    @JsonProperty("identityServiceItems")
    public void setIdentityServiceItems(List<IdentityService> identityServiceItems) {
        this.identityServiceItems = identityServiceItems;
    }

    @JsonProperty("identityServiceTypes")
    public List<IdentityServiceTypes> getIdentityServiceTypes() {
        return identityServiceTypes;
    }

    @JsonProperty("identityServiceTypes")
    public void setIdentityServiceTypes(List<IdentityServiceTypes> identityServiceTypes) {
        this.identityServiceTypes = identityServiceTypes;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("identityServiceItems", identityServiceItems).append("identityServiceTypes", identityServiceTypes).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(identityServiceTypes).append(identityServiceItems).append(deliveryDate).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof IdentityServices) == false) {
            return false;
        }
        IdentityServices rhs = ((IdentityServices) other);
        return new EqualsBuilder().append(identityServiceTypes, rhs.identityServiceTypes).append(identityServiceItems, rhs.identityServiceItems).append(deliveryDate, rhs.deliveryDate).isEquals();
    }

}
