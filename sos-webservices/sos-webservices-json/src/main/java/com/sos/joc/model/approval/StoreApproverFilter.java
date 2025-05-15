
package com.sos.joc.model.approval;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.security.foureyes.Approver;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Store Approver Filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "approver"
})
public class StoreApproverFilter {

    /**
     * Approver
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("approver")
    private Approver approver;

    /**
     * Approver
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("approver")
    public Approver getApprover() {
        return approver;
    }

    /**
     * Approver
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("approver")
    public void setApprover(Approver approver) {
        this.approver = approver;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("approver", approver).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(approver).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StoreApproverFilter) == false) {
            return false;
        }
        StoreApproverFilter rhs = ((StoreApproverFilter) other);
        return new EqualsBuilder().append(approver, rhs.approver).isEquals();
    }

}
