
package com.sos.joc.model.workflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.order.OrdersSummary;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "path",
    "versionId",
    "numOfOrders"
})
public class WorkflowOrderCount {

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of an object.")
    private String path;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("versionId")
    private String versionId;
    /**
     * order summary
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfOrders")
    private OrdersSummary numOfOrders;

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("versionId")
    public String getVersionId() {
        return versionId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("versionId")
    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    /**
     * order summary
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfOrders")
    public OrdersSummary getNumOfOrders() {
        return numOfOrders;
    }

    /**
     * order summary
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfOrders")
    public void setNumOfOrders(OrdersSummary numOfOrders) {
        this.numOfOrders = numOfOrders;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("path", path).append("versionId", versionId).append("numOfOrders", numOfOrders).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(versionId).append(numOfOrders).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WorkflowOrderCount) == false) {
            return false;
        }
        WorkflowOrderCount rhs = ((WorkflowOrderCount) other);
        return new EqualsBuilder().append(path, rhs.path).append(versionId, rhs.versionId).append(numOfOrders, rhs.numOfOrders).isEquals();
    }

}
