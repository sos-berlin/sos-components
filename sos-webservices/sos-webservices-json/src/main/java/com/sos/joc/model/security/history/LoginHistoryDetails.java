
package com.sos.joc.model.security.history;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * LoginHistoryDetails
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "loginHistoryItems"
})
public class LoginHistoryDetails {

    @JsonProperty("loginHistoryItems")
    private List<LoginHistoryDetailItem> loginHistoryItems = new ArrayList<LoginHistoryDetailItem>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public LoginHistoryDetails() {
    }

    /**
     * 
     * @param loginHistoryItems
     */
    public LoginHistoryDetails(List<LoginHistoryDetailItem> loginHistoryItems) {
        super();
        this.loginHistoryItems = loginHistoryItems;
    }

    @JsonProperty("loginHistoryItems")
    public List<LoginHistoryDetailItem> getLoginHistoryItems() {
        return loginHistoryItems;
    }

    @JsonProperty("loginHistoryItems")
    public void setLoginHistoryItems(List<LoginHistoryDetailItem> loginHistoryItems) {
        this.loginHistoryItems = loginHistoryItems;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("loginHistoryItems", loginHistoryItems).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(loginHistoryItems).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LoginHistoryDetails) == false) {
            return false;
        }
        LoginHistoryDetails rhs = ((LoginHistoryDetails) other);
        return new EqualsBuilder().append(loginHistoryItems, rhs.loginHistoryItems).isEquals();
    }

}
