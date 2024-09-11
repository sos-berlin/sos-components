
package com.sos.joc.model.cluster;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.joc.model.cluster.common.state.JocClusterState;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * JOC cluster response
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "type",
    "state"
})
public class ClusterResponse {

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
     * JOC cluster services
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    private ClusterServices type;
    /**
     * JOC cluster states
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    private JocClusterState state;

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
     * JOC cluster services
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    public ClusterServices getType() {
        return type;
    }

    /**
     * JOC cluster services
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    public void setType(ClusterServices type) {
        this.type = type;
    }

    /**
     * JOC cluster states
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    public JocClusterState getState() {
        return state;
    }

    /**
     * JOC cluster states
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    public void setState(JocClusterState state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("type", type).append("state", state).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(state).append(deliveryDate).append(type).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ClusterResponse) == false) {
            return false;
        }
        ClusterResponse rhs = ((ClusterResponse) other);
        return new EqualsBuilder().append(state, rhs.state).append(deliveryDate, rhs.deliveryDate).append(type, rhs.type).isEquals();
    }

}
