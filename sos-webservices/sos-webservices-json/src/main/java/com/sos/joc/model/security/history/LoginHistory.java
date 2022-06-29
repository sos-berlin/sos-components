
package com.sos.joc.model.security.history;

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
 * LoginHistory
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "loginHistoryItems"
})
public class LoginHistory {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    @JsonProperty("loginHistoryItems")
    private List<LoginHistoryItem> loginHistoryItems = new ArrayList<LoginHistoryItem>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public LoginHistory() {
    }

    /**
     * 
     * @param loginHistoryItems
     * @param deliveryDate
     */
    public LoginHistory(Date deliveryDate, List<LoginHistoryItem> loginHistoryItems) {
        super();
        this.deliveryDate = deliveryDate;
        this.loginHistoryItems = loginHistoryItems;
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

    @JsonProperty("loginHistoryItems")
    public List<LoginHistoryItem> getLoginHistoryItems() {
        return loginHistoryItems;
    }

    @JsonProperty("loginHistoryItems")
    public void setLoginHistoryItems(List<LoginHistoryItem> loginHistoryItems) {
        this.loginHistoryItems = loginHistoryItems;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("loginHistoryItems", loginHistoryItems).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deliveryDate).append(loginHistoryItems).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LoginHistory) == false) {
            return false;
        }
        LoginHistory rhs = ((LoginHistory) other);
        return new EqualsBuilder().append(deliveryDate, rhs.deliveryDate).append(loginHistoryItems, rhs.loginHistoryItems).isEquals();
    }

}
