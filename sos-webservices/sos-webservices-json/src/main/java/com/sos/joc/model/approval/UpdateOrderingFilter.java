
package com.sos.joc.model.approval;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Update Ordering of Approvers Filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "accountNames"
})
public class UpdateOrderingFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountNames")
    private List<String> accountNames = new ArrayList<String>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountNames")
    public List<String> getAccountNames() {
        return accountNames;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountNames")
    public void setAccountNames(List<String> accountNames) {
        this.accountNames = accountNames;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("accountNames", accountNames).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(accountNames).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof UpdateOrderingFilter) == false) {
            return false;
        }
        UpdateOrderingFilter rhs = ((UpdateOrderingFilter) other);
        return new EqualsBuilder().append(accountNames, rhs.accountNames).isEquals();
    }

}
