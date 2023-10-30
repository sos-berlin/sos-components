
package com.sos.joc.model.security.identityservice;

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
    "fidoServiceItems"
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
    private List<FidoIdentityProvider> fido2ndFactorServiceItems = new ArrayList<FidoIdentityProvider>();
    @JsonProperty("fidoServiceItems")
    private List<FidoIdentityProvider> fidoServiceItems = new ArrayList<FidoIdentityProvider>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public IdentityProviders() {
    }

    /**
     * 
     * @param oidcServiceItems
     * @param fidoServiceItems
     * @param deliveryDate
     * @param fido2ndFactorServiceItems
     */
    public IdentityProviders(Date deliveryDate, List<OidcIdentityProvider> oidcServiceItems, List<FidoIdentityProvider> fido2ndFactorServiceItems, List<FidoIdentityProvider> fidoServiceItems) {
        super();
        this.deliveryDate = deliveryDate;
        this.oidcServiceItems = oidcServiceItems;
        this.fido2ndFactorServiceItems = fido2ndFactorServiceItems;
        this.fidoServiceItems = fidoServiceItems;
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
    public List<FidoIdentityProvider> getFido2ndFactorServiceItems() {
        return fido2ndFactorServiceItems;
    }

    @JsonProperty("fido2ndFactorServiceItems")
    public void setFido2ndFactorServiceItems(List<FidoIdentityProvider> fido2ndFactorServiceItems) {
        this.fido2ndFactorServiceItems = fido2ndFactorServiceItems;
    }

    @JsonProperty("fidoServiceItems")
    public List<FidoIdentityProvider> getFidoServiceItems() {
        return fidoServiceItems;
    }

    @JsonProperty("fidoServiceItems")
    public void setFidoServiceItems(List<FidoIdentityProvider> fidoServiceItems) {
        this.fidoServiceItems = fidoServiceItems;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("oidcServiceItems", oidcServiceItems).append("fido2ndFactorServiceItems", fido2ndFactorServiceItems).append("fidoServiceItems", fidoServiceItems).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(oidcServiceItems).append(fidoServiceItems).append(deliveryDate).append(fido2ndFactorServiceItems).toHashCode();
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
        return new EqualsBuilder().append(oidcServiceItems, rhs.oidcServiceItems).append(fidoServiceItems, rhs.fidoServiceItems).append(deliveryDate, rhs.deliveryDate).append(fido2ndFactorServiceItems, rhs.fido2ndFactorServiceItems).isEquals();
    }

}
