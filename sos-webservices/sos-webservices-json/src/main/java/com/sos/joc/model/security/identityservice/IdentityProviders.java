
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
    "oidcServiceItems",
    "fido2ndFactorServiceItems",
    "fido2ServiceItems"
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
    @JsonProperty("oidcServiceItems")
    private List<OidcIdentityProvider> oidcServiceItems = new ArrayList<OidcIdentityProvider>();
    @JsonProperty("fido2ndFactorServiceItems")
    private List<Fido2IdentityProvider> fido2ndFactorServiceItems = new ArrayList<Fido2IdentityProvider>();
    @JsonProperty("fido2ServiceItems")
    private List<Fido2IdentityProvider> fido2ServiceItems = new ArrayList<Fido2IdentityProvider>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public IdentityProviders() {
    }

    /**
     * 
     * @param oidcServiceItems
     * @param fido2ServiceItems
     * @param deliveryDate
     * @param fido2ndFactorServiceItems
     */
    public IdentityProviders(Date deliveryDate, List<OidcIdentityProvider> oidcServiceItems, List<Fido2IdentityProvider> fido2ndFactorServiceItems, List<Fido2IdentityProvider> fido2ServiceItems) {
        super();
        this.deliveryDate = deliveryDate;
        this.oidcServiceItems = oidcServiceItems;
        this.fido2ndFactorServiceItems = fido2ndFactorServiceItems;
        this.fido2ServiceItems = fido2ServiceItems;
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

    @JsonProperty("oidcServiceItems")
    public List<OidcIdentityProvider> getOidcServiceItems() {
        return oidcServiceItems;
    }

    @JsonProperty("oidcServiceItems")
    public void setOidcServiceItems(List<OidcIdentityProvider> oidcServiceItems) {
        this.oidcServiceItems = oidcServiceItems;
    }

    @JsonProperty("fido2ndFactorServiceItems")
    public List<Fido2IdentityProvider> getFido2ndFactorServiceItems() {
        return fido2ndFactorServiceItems;
    }

    @JsonProperty("fido2ndFactorServiceItems")
    public void setFido2ndFactorServiceItems(List<Fido2IdentityProvider> fido2ndFactorServiceItems) {
        this.fido2ndFactorServiceItems = fido2ndFactorServiceItems;
    }

    @JsonProperty("fido2ServiceItems")
    public List<Fido2IdentityProvider> getFido2ServiceItems() {
        return fido2ServiceItems;
    }

    @JsonProperty("fido2ServiceItems")
    public void setFido2ServiceItems(List<Fido2IdentityProvider> fido2ServiceItems) {
        this.fido2ServiceItems = fido2ServiceItems;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("oidcServiceItems", oidcServiceItems).append("fido2ndFactorServiceItems", fido2ndFactorServiceItems).append("fido2ServiceItems", fido2ServiceItems).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(fido2ServiceItems).append(oidcServiceItems).append(deliveryDate).append(fido2ndFactorServiceItems).toHashCode();
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
        return new EqualsBuilder().append(fido2ServiceItems, rhs.fido2ServiceItems).append(oidcServiceItems, rhs.oidcServiceItems).append(deliveryDate, rhs.deliveryDate).append(fido2ndFactorServiceItems, rhs.fido2ndFactorServiceItems).isEquals();
    }

}
