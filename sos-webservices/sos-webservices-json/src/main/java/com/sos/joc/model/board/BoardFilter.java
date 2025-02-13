
package com.sos.joc.model.board;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.joc.model.plan.PlanSchemaId;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * notice board request filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "planSchemaIds",
    "planKeys",
    "noticeBoardPath",
    "compact",
    "limit"
})
public class BoardFilter {

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
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("noticeBoardPath")
    private String noticeBoardPath;
    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    @JsonPropertyDescription("controls if the object's data is compact or detailed")
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

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("noticeBoardPath")
    public String getNoticeBoardPath() {
        return noticeBoardPath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("noticeBoardPath")
    public void setNoticeBoardPath(String noticeBoardPath) {
        this.noticeBoardPath = noticeBoardPath;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    public Boolean getCompact() {
        return compact;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("planSchemaIds", planSchemaIds).append("planKeys", planKeys).append("noticeBoardPath", noticeBoardPath).append("compact", compact).append("limit", limit).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(planSchemaIds).append(planKeys).append(controllerId).append(compact).append(noticeBoardPath).append(limit).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof BoardFilter) == false) {
            return false;
        }
        BoardFilter rhs = ((BoardFilter) other);
        return new EqualsBuilder().append(planSchemaIds, rhs.planSchemaIds).append(planKeys, rhs.planKeys).append(controllerId, rhs.controllerId).append(compact, rhs.compact).append(noticeBoardPath, rhs.noticeBoardPath).append(limit, rhs.limit).isEquals();
    }

}
