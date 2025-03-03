
package com.sos.joc.model.plan;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * PlansOpenCloseFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "onlyOpenPlans",
    "onlyClosedPlans"
})
public class PlansOpenCloseFilter {

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    @JsonProperty("onlyOpenPlans")
    private Boolean onlyOpenPlans = false;
    @JsonProperty("onlyClosedPlans")
    private Boolean onlyClosedPlans = false;

    /**
     * controllerId
     * <p>
     * 
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    @JsonProperty("onlyOpenPlans")
    public Boolean getOnlyOpenPlans() {
        return onlyOpenPlans;
    }

    @JsonProperty("onlyOpenPlans")
    public void setOnlyOpenPlans(Boolean onlyOpenPlans) {
        this.onlyOpenPlans = onlyOpenPlans;
    }

    @JsonProperty("onlyClosedPlans")
    public Boolean getOnlyClosedPlans() {
        return onlyClosedPlans;
    }

    @JsonProperty("onlyClosedPlans")
    public void setOnlyClosedPlans(Boolean onlyClosedPlans) {
        this.onlyClosedPlans = onlyClosedPlans;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("onlyOpenPlans", onlyOpenPlans).append("onlyClosedPlans", onlyClosedPlans).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(onlyOpenPlans).append(onlyClosedPlans).append(controllerId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PlansOpenCloseFilter) == false) {
            return false;
        }
        PlansOpenCloseFilter rhs = ((PlansOpenCloseFilter) other);
        return new EqualsBuilder().append(onlyOpenPlans, rhs.onlyOpenPlans).append(onlyClosedPlans, rhs.onlyClosedPlans).append(controllerId, rhs.controllerId).isEquals();
    }

}
