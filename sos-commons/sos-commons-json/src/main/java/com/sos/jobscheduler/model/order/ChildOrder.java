
package com.sos.jobscheduler.model.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sos.jobscheduler.model.common.VariablesDiff;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * childOrder
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "branchId",
    "orderId",
    "variablesDiff"
})
public class ChildOrder {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("branchId")
    @JacksonXmlProperty(localName = "branchId")
    private String branchId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    @JacksonXmlProperty(localName = "orderId")
    private String orderId;
    /**
     * changes of key-value pairs
     * <p>
     * 
     * 
     */
    @JsonProperty("variablesDiff")
    @JacksonXmlProperty(localName = "variablesDiff")
    private VariablesDiff variablesDiff;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("branchId")
    @JacksonXmlProperty(localName = "branchId")
    public String getBranchId() {
        return branchId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("branchId")
    @JacksonXmlProperty(localName = "branchId")
    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    @JacksonXmlProperty(localName = "orderId")
    public String getOrderId() {
        return orderId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    @JacksonXmlProperty(localName = "orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * changes of key-value pairs
     * <p>
     * 
     * 
     */
    @JsonProperty("variablesDiff")
    @JacksonXmlProperty(localName = "variablesDiff")
    public VariablesDiff getVariablesDiff() {
        return variablesDiff;
    }

    /**
     * changes of key-value pairs
     * <p>
     * 
     * 
     */
    @JsonProperty("variablesDiff")
    @JacksonXmlProperty(localName = "variablesDiff")
    public void setVariablesDiff(VariablesDiff variablesDiff) {
        this.variablesDiff = variablesDiff;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("branchId", branchId).append("orderId", orderId).append("variablesDiff", variablesDiff).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(variablesDiff).append(branchId).append(orderId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ChildOrder) == false) {
            return false;
        }
        ChildOrder rhs = ((ChildOrder) other);
        return new EqualsBuilder().append(variablesDiff, rhs.variablesDiff).append(branchId, rhs.branchId).append(orderId, rhs.orderId).isEquals();
    }

}
