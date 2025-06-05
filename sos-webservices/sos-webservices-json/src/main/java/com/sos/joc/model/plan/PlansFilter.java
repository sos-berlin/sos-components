
package com.sos.joc.model.plan;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonAlias;
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
    "planSchemaIds",
    "noticeSpaceKeys",
    "noticeBoardPaths",
    "compact",
    "includeOrders",
    "limit"
})
public class PlansFilter
    extends PlansOpenCloseFilter
{

    @JsonProperty("planSchemaIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> planSchemaIds = new LinkedHashSet<String>();
    /**
     * Will be ignored for global schema because it has no plan keys
     * 
     */
    @JsonProperty("noticeSpaceKeys")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("Will be ignored for global schema because it has no plan keys")
    @JsonAlias({
        "planKeys"
    })
    private Set<String> noticeSpaceKeys = new LinkedHashSet<String>();
    @JsonProperty("noticeBoardPaths")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> noticeBoardPaths = new LinkedHashSet<String>();
    @JsonProperty("compact")
    private Boolean compact = false;
    @JsonProperty("includeOrders")
    private Boolean includeOrders = false;
    /**
     * -1=unlimited
     * 
     */
    @JsonProperty("limit")
    @JsonPropertyDescription("-1=unlimited")
    private Integer limit = 10000;

    @JsonProperty("planSchemaIds")
    public Set<String> getPlanSchemaIds() {
        return planSchemaIds;
    }

    @JsonProperty("planSchemaIds")
    public void setPlanSchemaIds(Set<String> planSchemaIds) {
        this.planSchemaIds = planSchemaIds;
    }

    /**
     * Will be ignored for global schema because it has no plan keys
     * 
     */
    @JsonProperty("noticeSpaceKeys")
    public Set<String> getNoticeSpaceKeys() {
        return noticeSpaceKeys;
    }

    /**
     * Will be ignored for global schema because it has no plan keys
     * 
     */
    @JsonProperty("noticeSpaceKeys")
    public void setNoticeSpaceKeys(Set<String> noticeSpaceKeys) {
        this.noticeSpaceKeys = noticeSpaceKeys;
    }

    @JsonProperty("noticeBoardPaths")
    public Set<String> getNoticeBoardPaths() {
        return noticeBoardPaths;
    }

    @JsonProperty("noticeBoardPaths")
    public void setNoticeBoardPaths(Set<String> noticeBoardPaths) {
        this.noticeBoardPaths = noticeBoardPaths;
    }

    @JsonProperty("compact")
    public Boolean getCompact() {
        return compact;
    }

    @JsonProperty("compact")
    public void setCompact(Boolean compact) {
        this.compact = compact;
    }

    @JsonProperty("includeOrders")
    public Boolean getIncludeOrders() {
        return includeOrders;
    }

    @JsonProperty("includeOrders")
    public void setIncludeOrders(Boolean includeOrders) {
        this.includeOrders = includeOrders;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("planSchemaIds", planSchemaIds).append("noticeSpaceKeys", noticeSpaceKeys).append("noticeBoardPaths", noticeBoardPaths).append("compact", compact).append("includeOrders", includeOrders).append("limit", limit).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(planSchemaIds).append(includeOrders).append(compact).append(noticeBoardPaths).append(limit).append(noticeSpaceKeys).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(planSchemaIds, rhs.planSchemaIds).append(includeOrders, rhs.includeOrders).append(compact, rhs.compact).append(noticeBoardPaths, rhs.noticeBoardPaths).append(limit, rhs.limit).append(noticeSpaceKeys, rhs.noticeSpaceKeys).isEquals();
    }

}
