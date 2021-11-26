
package com.sos.joc.model.dailyplan.history.items;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * date object in daily plan history collection
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "countTotal",
    "countSubmitted"
})
public class ControllerItem {

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("countTotal")
    private Long countTotal;
    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("countSubmitted")
    private Long countSubmitted;

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("countTotal")
    public Long getCountTotal() {
        return countTotal;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("countTotal")
    public void setCountTotal(Long countTotal) {
        this.countTotal = countTotal;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("countSubmitted")
    public Long getCountSubmitted() {
        return countSubmitted;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("countSubmitted")
    public void setCountSubmitted(Long countSubmitted) {
        this.countSubmitted = countSubmitted;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("countTotal", countTotal).append("countSubmitted", countSubmitted).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(countTotal).append(controllerId).append(countSubmitted).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ControllerItem) == false) {
            return false;
        }
        ControllerItem rhs = ((ControllerItem) other);
        return new EqualsBuilder().append(countTotal, rhs.countTotal).append(controllerId, rhs.controllerId).append(countSubmitted, rhs.countSubmitted).isEquals();
    }

}
