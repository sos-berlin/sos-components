
package com.sos.joc.model.reporting;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.joc.model.job.JobsFilter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * reporting of ORDER_STEPS table
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "columns",
    "limit"
})
public class OrderSteps
    extends JobsFilter
{

    @JsonProperty("columns")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<OrderStepsColumns> columns = new LinkedHashSet<OrderStepsColumns>();
    /**
     * only for db history urls to restrict the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    @JsonPropertyDescription("only for db history urls to restrict the number of responsed records; -1=unlimited")
    private Integer limit = -1;

    @JsonProperty("columns")
    public Set<OrderStepsColumns> getColumns() {
        return columns;
    }

    @JsonProperty("columns")
    public void setColumns(Set<OrderStepsColumns> columns) {
        this.columns = columns;
    }

    /**
     * only for db history urls to restrict the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    public Integer getLimit() {
        return limit;
    }

    /**
     * only for db history urls to restrict the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("columns", columns).append("limit", limit).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(limit).append(columns).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderSteps) == false) {
            return false;
        }
        OrderSteps rhs = ((OrderSteps) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(limit, rhs.limit).append(columns, rhs.columns).isEquals();
    }

}
