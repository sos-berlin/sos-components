
package com.sos.jobscheduler.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "transferId",
    "fileId"
})
public class YadeVariables {

    @JsonProperty("transferId")
    @JacksonXmlProperty(localName = "transferId")
    private String transferId;
    /**
     * for YADEFileStateChanged
     * 
     */
    @JsonProperty("fileId")
    @JsonPropertyDescription("for YADEFileStateChanged")
    @JacksonXmlProperty(localName = "fileId")
    private String fileId;

    @JsonProperty("transferId")
    @JacksonXmlProperty(localName = "transferId")
    public String getTransferId() {
        return transferId;
    }

    @JsonProperty("transferId")
    @JacksonXmlProperty(localName = "transferId")
    public void setTransferId(String transferId) {
        this.transferId = transferId;
    }

    /**
     * for YADEFileStateChanged
     * 
     */
    @JsonProperty("fileId")
    @JacksonXmlProperty(localName = "fileId")
    public String getFileId() {
        return fileId;
    }

    /**
     * for YADEFileStateChanged
     * 
     */
    @JsonProperty("fileId")
    @JacksonXmlProperty(localName = "fileId")
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("transferId", transferId).append("fileId", fileId).toString();
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
