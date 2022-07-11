
package com.sos.joc.model.profile;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ProfilesFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "complete",
    "accounts"
})
public class ProfilesFilter {

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    @JsonProperty("complete")
    private Boolean complete = false;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("accounts")
    private List<String> accounts = new ArrayList<String>();

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    @JsonProperty("complete")
    public Boolean getComplete() {
        return complete;
    }

    @JsonProperty("complete")
    public void setComplete(Boolean complete) {
        this.complete = complete;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("accounts")
    public List<String> getAccounts() {
        return accounts;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("accounts")
    public void setAccounts(List<String> accounts) {
        this.accounts = accounts;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("complete", complete).append("accounts", accounts).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(accounts).append(controllerId).append(complete).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ProfilesFilter) == false) {
            return false;
        }
        ProfilesFilter rhs = ((ProfilesFilter) other);
        return new EqualsBuilder().append(accounts, rhs.accounts).append(controllerId, rhs.controllerId).append(complete, rhs.complete).isEquals();
    }

}
