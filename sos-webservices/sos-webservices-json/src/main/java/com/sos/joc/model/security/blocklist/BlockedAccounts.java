
package com.sos.joc.model.security.blocklist;

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
 * BlockedAccounts
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "blockedAccounts"
})
public class BlockedAccounts {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    @JsonProperty("blockedAccounts")
    private List<BlockedAccount> blockedAccounts = new ArrayList<BlockedAccount>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public BlockedAccounts() {
    }

    /**
     * 
     * @param blockedAccounts
     * @param deliveryDate
     */
    public BlockedAccounts(Date deliveryDate, List<BlockedAccount> blockedAccounts) {
        super();
        this.deliveryDate = deliveryDate;
        this.blockedAccounts = blockedAccounts;
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

    @JsonProperty("blockedAccounts")
    public List<BlockedAccount> getBlockedAccounts() {
        return blockedAccounts;
    }

    @JsonProperty("blockedAccounts")
    public void setBlockedAccounts(List<BlockedAccount> blockedAccounts) {
        this.blockedAccounts = blockedAccounts;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("blockedAccounts", blockedAccounts).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deliveryDate).append(blockedAccounts).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof BlockedAccounts) == false) {
            return false;
        }
        BlockedAccounts rhs = ((BlockedAccounts) other);
        return new EqualsBuilder().append(deliveryDate, rhs.deliveryDate).append(blockedAccounts, rhs.blockedAccounts).isEquals();
    }

}
