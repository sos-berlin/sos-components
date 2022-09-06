
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
 * Identiy Providers
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "identityServiceItems"
})
public class IdentityProviders {

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
    private List<IdentityProvider> identityServiceItems = new ArrayList<IdentityProvider>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public IdentityProviders() {
    }

    /**
     * 
     * @param identityServiceItems
     * @param deliveryDate
     */
    public IdentityProviders(Date deliveryDate, List<IdentityProvider> identityServiceItems) {
        super();
        this.deliveryDate = deliveryDate;
        this.identityServiceItems = identityServiceItems;
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
    public List<IdentityProvider> getIdentityServiceItems() {
        return identityServiceItems;
    }

    @JsonProperty("identityServiceItems")
    public void setIdentityServiceItems(List<IdentityProvider> identityServiceItems) {
        this.identityServiceItems = identityServiceItems;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("identityServiceItems", identityServiceItems).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deliveryDate).append(identityServiceItems).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof IdentityProviders) == false) {
            return false;
        }
        IdentityProviders rhs = ((IdentityProviders) other);
        return new EqualsBuilder().append(deliveryDate, rhs.deliveryDate).append(identityServiceItems, rhs.identityServiceItems).isEquals();
    }

}
