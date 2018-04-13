
package com.sos.joc.model.yade;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
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
    "jobschedulerId",
    "transferId",
    "fileIds"
})
public class ModifyTransfer {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    private String jobschedulerId;
    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("transferId")
    @JacksonXmlProperty(localName = "transferId")
    private Long transferId;
    @JsonProperty("fileIds")
    @JacksonXmlProperty(localName = "fileId")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "fileIds")
    private List<Long> fileIds = new ArrayList<Long>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("transferId")
    @JacksonXmlProperty(localName = "transferId")
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
    @JacksonXmlProperty(localName = "transferId")
    public void setTransferId(Long transferId) {
        this.transferId = transferId;
    }

    @JsonProperty("fileIds")
    @JacksonXmlProperty(localName = "fileId")
    public List<Long> getFileIds() {
        return fileIds;
    }

    @JsonProperty("fileIds")
    @JacksonXmlProperty(localName = "fileId")
    public void setFileIds(List<Long> fileIds) {
        this.fileIds = fileIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("transferId", transferId).append("fileIds", fileIds).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobschedulerId).append(transferId).append(fileIds).toHashCode();
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
        return new EqualsBuilder().append(jobschedulerId, rhs.jobschedulerId).append(transferId, rhs.transferId).append(fileIds, rhs.fileIds).isEquals();
    }

}
