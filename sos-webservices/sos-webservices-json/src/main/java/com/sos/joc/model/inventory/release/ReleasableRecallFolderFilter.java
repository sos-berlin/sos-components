
package com.sos.joc.model.inventory.release;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.common.RequestFolder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * filter to recall already released configurations from a folder
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "keepOrders",
    "transactionId"
})
public class ReleasableRecallFolderFilter
    extends RequestFolder
{

    @JsonProperty("keepOrders")
    private Boolean keepOrders = false;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("transactionId")
    private String transactionId;

    @JsonProperty("keepOrders")
    public Boolean getKeepOrders() {
        return keepOrders;
    }

    @JsonProperty("keepOrders")
    public void setKeepOrders(Boolean keepOrders) {
        this.keepOrders = keepOrders;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("transactionId")
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("transactionId")
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("keepOrders", keepOrders).append("transactionId", transactionId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(keepOrders).append(transactionId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReleasableRecallFolderFilter) == false) {
            return false;
        }
        ReleasableRecallFolderFilter rhs = ((ReleasableRecallFolderFilter) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(keepOrders, rhs.keepOrders).append(transactionId, rhs.transactionId).isEquals();
    }

}
