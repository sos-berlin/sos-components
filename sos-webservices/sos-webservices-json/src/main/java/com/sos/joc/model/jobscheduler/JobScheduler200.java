
package com.sos.joc.model.jobscheduler;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * jobscheduler with delivery date
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "controller"
})
public class JobScheduler200 {

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
     * JobScheduler Controller
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controller")
    private Controller controller;

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
     * JobScheduler Controller
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controller")
    public Controller getController() {
        return controller;
    }

    /**
     * JobScheduler Controller
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controller")
    public void setController(Controller controller) {
        this.controller = controller;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("controller", controller).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deliveryDate).append(controller).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobScheduler200) == false) {
            return false;
        }
        JobScheduler200 rhs = ((JobScheduler200) other);
        return new EqualsBuilder().append(deliveryDate, rhs.deliveryDate).append(controller, rhs.controller).isEquals();
    }

}
