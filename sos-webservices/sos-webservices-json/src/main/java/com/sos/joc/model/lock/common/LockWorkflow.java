
package com.sos.joc.model.lock.common;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * lock workflow
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "path",
    "versionId",
    "ordersHoldingLocks",
    "ordersWaitingForLocks"
})
public class LockWorkflow {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    private String path;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("versionId")
    private String versionId;
    @JsonProperty("ordersHoldingLocks")
    private List<LockOrder> ordersHoldingLocks = new ArrayList<LockOrder>();
    @JsonProperty("ordersWaitingForLocks")
    private List<LockOrder> ordersWaitingForLocks = new ArrayList<LockOrder>();

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
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
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("versionId")
    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    @JsonProperty("ordersHoldingLocks")
    public List<LockOrder> getOrdersHoldingLocks() {
        return ordersHoldingLocks;
    }

    @JsonProperty("ordersHoldingLocks")
    public void setOrdersHoldingLocks(List<LockOrder> ordersHoldingLocks) {
        this.ordersHoldingLocks = ordersHoldingLocks;
    }

    @JsonProperty("ordersWaitingForLocks")
    public List<LockOrder> getOrdersWaitingForLocks() {
        return ordersWaitingForLocks;
    }

    @JsonProperty("ordersWaitingForLocks")
    public void setOrdersWaitingForLocks(List<LockOrder> ordersWaitingForLocks) {
        this.ordersWaitingForLocks = ordersWaitingForLocks;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("path", path).append("versionId", versionId).append("ordersHoldingLocks", ordersHoldingLocks).append("ordersWaitingForLocks", ordersWaitingForLocks).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(versionId).append(ordersHoldingLocks).append(ordersWaitingForLocks).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LockWorkflow) == false) {
            return false;
        }
        LockWorkflow rhs = ((LockWorkflow) other);
        return new EqualsBuilder().append(path, rhs.path).append(versionId, rhs.versionId).append(ordersHoldingLocks, rhs.ordersHoldingLocks).append(ordersWaitingForLocks, rhs.ordersWaitingForLocks).isEquals();
    }

}
