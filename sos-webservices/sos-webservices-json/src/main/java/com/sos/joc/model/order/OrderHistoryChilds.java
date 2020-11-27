
package com.sos.joc.model.order;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * order object in history collection
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "states",
    "hasTasks",
    "hasOrders",
    "children"
})
public class OrderHistoryChilds {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    @JsonProperty("states")
    private List<OrderHistoryStateItem> states = new ArrayList<OrderHistoryStateItem>();
    @JsonProperty("hasTasks")
    private Boolean hasTasks;
    @JsonProperty("hasOrders")
    private Boolean hasOrders;
    @JsonProperty("children")
    private List<OrderHistoryChildItem> children = new ArrayList<OrderHistoryChildItem>();

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    @JsonProperty("states")
    public List<OrderHistoryStateItem> getStates() {
        return states;
    }

    @JsonProperty("states")
    public void setStates(List<OrderHistoryStateItem> states) {
        this.states = states;
    }

    @JsonProperty("hasTasks")
    public Boolean getHasTasks() {
        return hasTasks;
    }

    @JsonProperty("hasTasks")
    public void setHasTasks(Boolean hasTasks) {
        this.hasTasks = hasTasks;
    }

    @JsonProperty("hasOrders")
    public Boolean getHasOrders() {
        return hasOrders;
    }

    @JsonProperty("hasOrders")
    public void setHasOrders(Boolean hasOrders) {
        this.hasOrders = hasOrders;
    }

    @JsonProperty("children")
    public List<OrderHistoryChildItem> getChildren() {
        return children;
    }

    @JsonProperty("children")
    public void setChildren(List<OrderHistoryChildItem> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("states", states).append("hasTasks", hasTasks).append("hasOrders", hasOrders).append("children", children).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deliveryDate).append(hasTasks).append(children).append(states).append(hasOrders).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderHistoryChilds) == false) {
            return false;
        }
        OrderHistoryChilds rhs = ((OrderHistoryChilds) other);
        return new EqualsBuilder().append(deliveryDate, rhs.deliveryDate).append(hasTasks, rhs.hasTasks).append(children, rhs.children).append(states, rhs.states).append(hasOrders, rhs.hasOrders).isEquals();
    }

}
