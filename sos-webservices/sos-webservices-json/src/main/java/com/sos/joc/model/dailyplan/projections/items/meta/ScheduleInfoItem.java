
package com.sos.joc.model.dailyplan.projections.items.meta;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * daily plan projection
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "orders"
})
public class ScheduleInfoItem {

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("orders")
    private Long orders;
    @JsonIgnore
    private Map<String, WorkflowItem> additionalProperties = new HashMap<String, WorkflowItem>();

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("orders")
    public Long getOrders() {
        return orders;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("orders")
    public void setOrders(Long orders) {
        this.orders = orders;
    }

    @JsonAnyGetter
    public Map<String, WorkflowItem> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, WorkflowItem value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("orders", orders).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(orders).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ScheduleInfoItem) == false) {
            return false;
        }
        ScheduleInfoItem rhs = ((ScheduleInfoItem) other);
        return new EqualsBuilder().append(orders, rhs.orders).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
