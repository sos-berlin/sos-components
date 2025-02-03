
package com.sos.joc.model.plan;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * PlansFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "planSchemaIds",
    "planKeys",
    "onlyOpenPlans",
    "onlyClosedPlans"
})
public class PlansFilter {

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    @JsonProperty("planSchemaIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<PlanSchemaId> planSchemaIds = new LinkedHashSet<PlanSchemaId>();
    /**
     * Will be ignored for global schema bacause it has no plan keys
     * 
     */
    @JsonProperty("planKeys")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("Will be ignored for global schema bacause it has no plan keys")
    private Set<String> planKeys = new LinkedHashSet<String>();
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

    @JsonProperty("planSchemaIds")
    public Set<PlanSchemaId> getPlanSchemaIds() {
        return planSchemaIds;
    }

    @JsonProperty("planSchemaIds")
    public void setPlanSchemaIds(Set<PlanSchemaId> planSchemaIds) {
        this.planSchemaIds = planSchemaIds;
    }

    /**
     * Will be ignored for global schema bacause it has no plan keys
     * 
     */
    @JsonProperty("planKeys")
    public Set<String> getPlanKeys() {
        return planKeys;
    }

    /**
     * Will be ignored for global schema bacause it has no plan keys
     * 
     */
    @JsonProperty("planKeys")
    public void setPlanKeys(Set<String> planKeys) {
        this.planKeys = planKeys;
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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("planSchemaIds", planSchemaIds).append("planKeys", planKeys).append("onlyOpenPlans", onlyOpenPlans).append("onlyClosedPlans", onlyClosedPlans).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(planSchemaIds).append(planKeys).append(onlyOpenPlans).append(onlyClosedPlans).append(controllerId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PlansFilter) == false) {
            return false;
        }
        PlansFilter rhs = ((PlansFilter) other);
        return new EqualsBuilder().append(planSchemaIds, rhs.planSchemaIds).append(planKeys, rhs.planKeys).append(onlyOpenPlans, rhs.onlyOpenPlans).append(onlyClosedPlans, rhs.onlyClosedPlans).append(controllerId, rhs.controllerId).isEquals();
    }

}
