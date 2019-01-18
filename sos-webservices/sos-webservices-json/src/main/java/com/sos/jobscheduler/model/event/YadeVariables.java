
package com.sos.jobscheduler.model.event;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "transferId",
    "fileId"
})
public class YadeVariables {

    @JsonProperty("transferId")
    private String transferId;
    /**
     * for YADEFileStateChanged
     * 
     */
    @JsonProperty("fileId")
    private String fileId;

    /**
     * 
     * @return
     *     The transferId
     */
    @JsonProperty("transferId")
    public String getTransferId() {
        return transferId;
    }

    /**
     * 
     * @param transferId
     *     The transferId
     */
    @JsonProperty("transferId")
    public void setTransferId(String transferId) {
        this.transferId = transferId;
    }

    /**
     * for YADEFileStateChanged
     * 
     * @return
     *     The fileId
     */
    @JsonProperty("fileId")
    public String getFileId() {
        return fileId;
    }

    /**
     * for YADEFileStateChanged
     * 
     * @param fileId
     *     The fileId
     */
    @JsonProperty("fileId")
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(transferId).append(fileId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof YadeVariables) == false) {
            return false;
        }
        YadeVariables rhs = ((YadeVariables) other);
        return new EqualsBuilder().append(transferId, rhs.transferId).append(fileId, rhs.fileId).isEquals();
    }

}
