
package com.sos.joc.model.yade;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * modify transfer
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "transferId",
    "fileIds"
})
public class ModifyTransfer {

    /**
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
    @JsonProperty("transferId")
    private Long transferId;
    @JsonProperty("fileIds")
    private List<Long> fileIds = new ArrayList<Long>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
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
    @JsonProperty("transferId")
    public Long getTransferId() {
        return transferId;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("transferId")
    public void setTransferId(Long transferId) {
        this.transferId = transferId;
    }

    @JsonProperty("fileIds")
    public List<Long> getFileIds() {
        return fileIds;
    }

    @JsonProperty("fileIds")
    public void setFileIds(List<Long> fileIds) {
        this.fileIds = fileIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("transferId", transferId).append("fileIds", fileIds).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(transferId).append(fileIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ModifyTransfer) == false) {
            return false;
        }
        ModifyTransfer rhs = ((ModifyTransfer) other);
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(transferId, rhs.transferId).append(fileIds, rhs.fileIds).isEquals();
    }

}
