
package com.sos.joc.model.security.fido;

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
    "fidoRegistrationItems"
})
public class FidoRegistrations {

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
    @JsonProperty("fidoRegistrationItems")
    private List<FidoRegistration> fidoRegistrationItems = new ArrayList<FidoRegistration>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public FidoRegistrations() {
    }

    /**
     * 
     * @param identityServiceName
     * @param fidoRegistrationItems
     * @param deliveryDate
     */
    public FidoRegistrations(Date deliveryDate, String identityServiceName, List<FidoRegistration> fidoRegistrationItems) {
        super();
        this.deliveryDate = deliveryDate;
        this.identityServiceName = identityServiceName;
        this.fidoRegistrationItems = fidoRegistrationItems;
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

    @JsonProperty("fidoRegistrationItems")
    public List<FidoRegistration> getFidoRegistrationItems() {
        return fidoRegistrationItems;
    }

    @JsonProperty("fidoRegistrationItems")
    public void setFidoRegistrationItems(List<FidoRegistration> fidoRegistrationItems) {
        this.fidoRegistrationItems = fidoRegistrationItems;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("identityServiceName", identityServiceName).append("fidoRegistrationItems", fidoRegistrationItems).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(identityServiceName).append(deliveryDate).append(fidoRegistrationItems).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FidoRegistrations) == false) {
            return false;
        }
        FidoRegistrations rhs = ((FidoRegistrations) other);
        return new EqualsBuilder().append(identityServiceName, rhs.identityServiceName).append(deliveryDate, rhs.deliveryDate).append(fidoRegistrationItems, rhs.fidoRegistrationItems).isEquals();
    }

}
