
package com.sos.joc.model.security.fido2;

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
 * Accounts
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "identityServiceName",
    "fido2RegistrationItems"
})
public class Fido2Registrations {

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
    @JsonProperty("identityServiceName")
    private String identityServiceName;
    @JsonProperty("fido2RegistrationItems")
    private List<Fido2Registration> fido2RegistrationItems = new ArrayList<Fido2Registration>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Fido2Registrations() {
    }

    /**
     * 
     * @param identityServiceName
     * @param fido2RegistrationItems
     * @param deliveryDate
     */
    public Fido2Registrations(Date deliveryDate, String identityServiceName, List<Fido2Registration> fido2RegistrationItems) {
        super();
        this.deliveryDate = deliveryDate;
        this.identityServiceName = identityServiceName;
        this.fido2RegistrationItems = fido2RegistrationItems;
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
    @JsonProperty("identityServiceName")
    public String getIdentityServiceName() {
        return identityServiceName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceName")
    public void setIdentityServiceName(String identityServiceName) {
        this.identityServiceName = identityServiceName;
    }

    @JsonProperty("fido2RegistrationItems")
    public List<Fido2Registration> getFido2RegistrationItems() {
        return fido2RegistrationItems;
    }

    @JsonProperty("fido2RegistrationItems")
    public void setFido2RegistrationItems(List<Fido2Registration> fido2RegistrationItems) {
        this.fido2RegistrationItems = fido2RegistrationItems;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("identityServiceName", identityServiceName).append("fido2RegistrationItems", fido2RegistrationItems).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(identityServiceName).append(fido2RegistrationItems).append(deliveryDate).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Fido2Registrations) == false) {
            return false;
        }
        Fido2Registrations rhs = ((Fido2Registrations) other);
        return new EqualsBuilder().append(identityServiceName, rhs.identityServiceName).append(fido2RegistrationItems, rhs.fido2RegistrationItems).append(deliveryDate, rhs.deliveryDate).isEquals();
    }

}
