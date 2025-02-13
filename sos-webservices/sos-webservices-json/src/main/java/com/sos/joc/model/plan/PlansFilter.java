
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
    "noticeBoardPaths",
    "onlyOpenPlans",
    "onlyClosedPlans",
    "compact",
    "limit"
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
     * Will be ignored for global schema because it has no plan keys
     * 
     */
    @JsonProperty("planKeys")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("Will be ignored for global schema because it has no plan keys")
    private Set<String> planKeys = new LinkedHashSet<String>();
    @JsonProperty("noticeBoardPaths")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> noticeBoardPaths = new LinkedHashSet<String>();
    @JsonProperty("onlyOpenPlans")
    private Boolean onlyOpenPlans = false;
    @JsonProperty("onlyClosedPlans")
    private Boolean onlyClosedPlans = false;
    @JsonProperty("compact")
    private Boolean compact = false;
    /**
     * -1=unlimited
     * 
     */
    @JsonProperty("limit")
    @JsonPropertyDescription("-1=unlimited")
    private Integer limit = 10000;

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
     * Will be ignored for global schema because it has no plan keys
     * 
     */
    @JsonProperty("planKeys")
    public Set<String> getPlanKeys() {
        return planKeys;
    }

    /**
     * Will be ignored for global schema because it has no plan keys
     * 
     */
    @JsonProperty("planKeys")
    public void setPlanKeys(Set<String> planKeys) {
        this.planKeys = planKeys;
    }

    @JsonProperty("noticeBoardPaths")
    public Set<String> getNoticeBoardPaths() {
        return noticeBoardPaths;
    }

    @JsonProperty("noticeBoardPaths")
    public void setNoticeBoardPaths(Set<String> noticeBoardPaths) {
        this.noticeBoardPaths = noticeBoardPaths;
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

    @JsonProperty("compact")
    public Boolean getCompact() {
        return compact;
    }

    @JsonProperty("compact")
    public void setCompact(Boolean compact) {
        this.compact = compact;
    }

    /**
     * -1=unlimited
     * 
     */
    @JsonProperty("limit")
    public Integer getLimit() {
        return limit;
    }

    /**
     * -1=unlimited
     * 
     */
    @JsonProperty("limit")
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("planSchemaIds", planSchemaIds).append("planKeys", planKeys).append("noticeBoardPaths", noticeBoardPaths).append("onlyOpenPlans", onlyOpenPlans).append("onlyClosedPlans", onlyClosedPlans).append("compact", compact).append("limit", limit).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(planSchemaIds).append(planKeys).append(controllerId).append(compact).append(noticeBoardPaths).append(limit).append(onlyOpenPlans).append(onlyClosedPlans).toHashCode();
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
        return new EqualsBuilder().append(planSchemaIds, rhs.planSchemaIds).append(planKeys, rhs.planKeys).append(controllerId, rhs.controllerId).append(compact, rhs.compact).append(noticeBoardPaths, rhs.noticeBoardPaths).append(limit, rhs.limit).append(onlyOpenPlans, rhs.onlyOpenPlans).append(onlyClosedPlans, rhs.onlyClosedPlans).isEquals();
    }

}
