
package com.sos.joc.model.deploy;

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
 * JS Objects
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "jsObjects"
})
public class JSObjects {

    /**
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ")
    private Date deliveryDate;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jsObjects")
    private List<IJSObject> jsObjects = new ArrayList<IJSObject>();

    /**
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
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
    @JsonProperty("jsObjects")
    public List<IJSObject> getJsObjects() {
        return jsObjects;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jsObjects")
    public void setJsObjects(List<IJSObject> jsObjects) {
        this.jsObjects = jsObjects;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("jsObjects", jsObjects).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deliveryDate).append(jsObjects).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JSObjects) == false) {
            return false;
        }
        JSObjects rhs = ((JSObjects) other);
        return new EqualsBuilder().append(deliveryDate, rhs.deliveryDate).append(jsObjects, rhs.jsObjects).isEquals();
    }

}
