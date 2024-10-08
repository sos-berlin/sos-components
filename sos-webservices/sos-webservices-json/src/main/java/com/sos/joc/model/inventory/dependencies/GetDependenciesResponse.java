
package com.sos.joc.model.inventory.dependencies;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.dependencies.get.ResponseItem;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * dependencies
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "dependencies"
})
public class GetDependenciesResponse {

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
     * dependencies
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("dependencies")
    private ResponseItem dependencies;

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
     * dependencies
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("dependencies")
    public ResponseItem getDependencies() {
        return dependencies;
    }

    /**
     * dependencies
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("dependencies")
    public void setDependencies(ResponseItem dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("dependencies", dependencies).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deliveryDate).append(dependencies).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof GetDependenciesResponse) == false) {
            return false;
        }
        GetDependenciesResponse rhs = ((GetDependenciesResponse) other);
        return new EqualsBuilder().append(deliveryDate, rhs.deliveryDate).append(dependencies, rhs.dependencies).isEquals();
    }

}
