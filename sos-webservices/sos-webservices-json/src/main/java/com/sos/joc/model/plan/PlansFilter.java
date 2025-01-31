
package com.sos.joc.model.plan;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    "planSchemaIds"
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("planSchemaIds", planSchemaIds).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(planSchemaIds).toHashCode();
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
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(planSchemaIds, rhs.planSchemaIds).isEquals();
    }

}
