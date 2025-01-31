
package com.sos.joc.model.plan;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * PlanId
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "planKey",
    "planSchemaId"
})
public class PlanId {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("planKey")
    private String planKey;
    /**
     * order state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("planSchemaId")
    private PlanSchemaId planSchemaId;

    /**
     * No args constructor for use in serialization
     * 
     */
    public PlanId() {
    }

    /**
     * 
     * @param planSchemaId
     * @param planKey
     */
    public PlanId(String planKey, PlanSchemaId planSchemaId) {
        super();
        this.planKey = planKey;
        this.planSchemaId = planSchemaId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("planKey")
    public String getPlanKey() {
        return planKey;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("planKey")
    public void setPlanKey(String planKey) {
        this.planKey = planKey;
    }

    /**
     * order state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("planSchemaId")
    public PlanSchemaId getPlanSchemaId() {
        return planSchemaId;
    }

    /**
     * order state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("planSchemaId")
    public void setPlanSchemaId(PlanSchemaId planSchemaId) {
        this.planSchemaId = planSchemaId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("planKey", planKey).append("planSchemaId", planSchemaId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(planKey).append(planSchemaId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PlanId) == false) {
            return false;
        }
        PlanId rhs = ((PlanId) other);
        return new EqualsBuilder().append(planKey, rhs.planKey).append(planSchemaId, rhs.planSchemaId).isEquals();
    }

}
