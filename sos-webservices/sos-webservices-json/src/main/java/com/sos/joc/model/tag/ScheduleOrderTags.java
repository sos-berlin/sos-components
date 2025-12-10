
package com.sos.joc.model.tag;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.schedule.OrderParameterisation;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "orderParameterisations"
})
public class ScheduleOrderTags {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("name")
    private String name;
    @JsonProperty("orderParameterisations")
    @JsonAlias({
        "variableSets"
    })
    private List<OrderParameterisation> orderParameterisations = new ArrayList<OrderParameterisation>();

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("orderParameterisations")
    public List<OrderParameterisation> getOrderParameterisations() {
        return orderParameterisations;
    }

    @JsonProperty("orderParameterisations")
    public void setOrderParameterisations(List<OrderParameterisation> orderParameterisations) {
        this.orderParameterisations = orderParameterisations;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("orderParameterisations", orderParameterisations).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(orderParameterisations).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ScheduleOrderTags) == false) {
            return false;
        }
        ScheduleOrderTags rhs = ((ScheduleOrderTags) other);
        return new EqualsBuilder().append(name, rhs.name).append(orderParameterisations, rhs.orderParameterisations).isEquals();
    }

}
