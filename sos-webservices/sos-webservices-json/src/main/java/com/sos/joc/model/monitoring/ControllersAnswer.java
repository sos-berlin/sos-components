
package com.sos.joc.model.monitoring;

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
 * monitoring collection of notifications
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "controllers"
})
public class ControllersAnswer {

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
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllers")
    private List<ControllerItem> controllers = new ArrayList<ControllerItem>();

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

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllers")
    public List<ControllerItem> getControllers() {
        return controllers;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllers")
    public void setControllers(List<ControllerItem> controllers) {
        this.controllers = controllers;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("controllers", controllers).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deliveryDate).append(controllers).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ControllersAnswer) == false) {
            return false;
        }
        ControllersAnswer rhs = ((ControllersAnswer) other);
        return new EqualsBuilder().append(deliveryDate, rhs.deliveryDate).append(controllers, rhs.controllers).isEquals();
    }

}
