
package com.sos.joc.model.inventory.deploy;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ResponseDeployable
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "deployable"
})
public class ResponseDeployable {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    /**
     * ResponseDeployableTreeItem
     * <p>
     * 
     * 
     */
    @JsonProperty("deployable")
    private ResponseDeployableTreeItem deployable;

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
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
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    /**
     * ResponseDeployableTreeItem
     * <p>
     * 
     * 
     */
    @JsonProperty("deployable")
    public ResponseDeployableTreeItem getDeployable() {
        return deployable;
    }

    /**
     * ResponseDeployableTreeItem
     * <p>
     * 
     * 
     */
    @JsonProperty("deployable")
    public void setDeployable(ResponseDeployableTreeItem deployable) {
        this.deployable = deployable;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("deployable", deployable).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deliveryDate).append(deployable).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ResponseDeployable) == false) {
            return false;
        }
        ResponseDeployable rhs = ((ResponseDeployable) other);
        return new EqualsBuilder().append(deliveryDate, rhs.deliveryDate).append(deployable, rhs.deployable).isEquals();
    }

}
