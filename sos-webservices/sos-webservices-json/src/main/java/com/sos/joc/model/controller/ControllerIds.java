
package com.sos.joc.model.controller;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * controllerIds
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "controllerIds",
    "selected"
})
public class ControllerIds {

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
    @JsonProperty("controllerIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> controllerIds = new LinkedHashSet<String>();
    /**
     * The Id from the 'controllerIds' collection which is specified in the selected field will be used for all further calls
     * (Required)
     * 
     */
    @JsonProperty("selected")
    @JsonPropertyDescription("The Id from the 'controllerIds' collection which is specified in the selected field will be used for all further calls")
    private String selected;

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
    @JsonProperty("controllerIds")
    public Set<String> getControllerIds() {
        return controllerIds;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerIds")
    public void setControllerIds(Set<String> controllerIds) {
        this.controllerIds = controllerIds;
    }

    /**
     * The Id from the 'controllerIds' collection which is specified in the selected field will be used for all further calls
     * (Required)
     * 
     */
    @JsonProperty("selected")
    public String getSelected() {
        return selected;
    }

    /**
     * The Id from the 'controllerIds' collection which is specified in the selected field will be used for all further calls
     * (Required)
     * 
     */
    @JsonProperty("selected")
    public void setSelected(String selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("controllerIds", controllerIds).append("selected", selected).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deliveryDate).append(controllerIds).append(selected).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ControllerIds) == false) {
            return false;
        }
        ControllerIds rhs = ((ControllerIds) other);
        return new EqualsBuilder().append(deliveryDate, rhs.deliveryDate).append(controllerIds, rhs.controllerIds).append(selected, rhs.selected).isEquals();
    }

}
