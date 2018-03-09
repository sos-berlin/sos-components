
package com.sos.joc.model.yade;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sos.joc.model.common.Err;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * yade file
 * <p>
 * compact=true -> required fields + possibly targetPath
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "surveyDate",
    "id",
    "transferId",
    "interventionTransferId",
    "state",
    "integrityHash",
    "modificationDate",
    "size",
    "error",
    "sourcePath",
    "sourceName",
    "targetPath",
    "targetName"
})
public class TransferFile {

    /**
     * survey date of the inventory data; last time the inventory job has checked the live folder
     * <p>
     * Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("surveyDate")
    @JsonPropertyDescription("Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ")
    @JacksonXmlProperty(localName = "surveyDate")
    private Date surveyDate;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    @JacksonXmlProperty(localName = "id")
    private Long id;
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
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("interventionTransferId")
    @JacksonXmlProperty(localName = "interventionTransferId")
    private Long interventionTransferId;
    /**
     * state for each transferred file
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
    private FileTransferState state;
    @JsonProperty("integrityHash")
    @JacksonXmlProperty(localName = "integrityHash")
    private String integrityHash;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("modificationDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    @JacksonXmlProperty(localName = "modificationDate")
    private Date modificationDate;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("size")
    @JacksonXmlProperty(localName = "size")
    private Long size;
    /**
     * error
     * <p>
     * 
     * 
     */
    @JsonProperty("error")
    @JacksonXmlProperty(localName = "error")
    private Err error;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sourcePath")
    @JacksonXmlProperty(localName = "sourcePath")
    private String sourcePath;
    @JsonProperty("sourceName")
    @JacksonXmlProperty(localName = "sourceName")
    private String sourceName;
    @JsonProperty("targetPath")
    @JacksonXmlProperty(localName = "targetPath")
    private String targetPath;
    @JsonProperty("targetName")
    @JacksonXmlProperty(localName = "targetName")
    private String targetName;

    /**
     * survey date of the inventory data; last time the inventory job has checked the live folder
     * <p>
     * Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("surveyDate")
    @JacksonXmlProperty(localName = "surveyDate")
    public Date getSurveyDate() {
        return surveyDate;
    }

    /**
     * survey date of the inventory data; last time the inventory job has checked the live folder
     * <p>
     * Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("surveyDate")
    @JacksonXmlProperty(localName = "surveyDate")
    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    @JacksonXmlProperty(localName = "id")
    public Long getId() {
        return id;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    @JacksonXmlProperty(localName = "id")
    public void setId(Long id) {
        this.id = id;
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

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("interventionTransferId")
    @JacksonXmlProperty(localName = "interventionTransferId")
    public Long getInterventionTransferId() {
        return interventionTransferId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("interventionTransferId")
    @JacksonXmlProperty(localName = "interventionTransferId")
    public void setInterventionTransferId(Long interventionTransferId) {
        this.interventionTransferId = interventionTransferId;
    }

    /**
     * state for each transferred file
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
    public FileTransferState getState() {
        return state;
    }

    /**
     * state for each transferred file
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
    public void setState(FileTransferState state) {
        this.state = state;
    }

    @JsonProperty("integrityHash")
    @JacksonXmlProperty(localName = "integrityHash")
    public String getIntegrityHash() {
        return integrityHash;
    }

    @JsonProperty("integrityHash")
    @JacksonXmlProperty(localName = "integrityHash")
    public void setIntegrityHash(String integrityHash) {
        this.integrityHash = integrityHash;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("modificationDate")
    @JacksonXmlProperty(localName = "modificationDate")
    public Date getModificationDate() {
        return modificationDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("modificationDate")
    @JacksonXmlProperty(localName = "modificationDate")
    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("size")
    @JacksonXmlProperty(localName = "size")
    public Long getSize() {
        return size;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("size")
    @JacksonXmlProperty(localName = "size")
    public void setSize(Long size) {
        this.size = size;
    }

    /**
     * error
     * <p>
     * 
     * 
     */
    @JsonProperty("error")
    @JacksonXmlProperty(localName = "error")
    public Err getError() {
        return error;
    }

    /**
     * error
     * <p>
     * 
     * 
     */
    @JsonProperty("error")
    @JacksonXmlProperty(localName = "error")
    public void setError(Err error) {
        this.error = error;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sourcePath")
    @JacksonXmlProperty(localName = "sourcePath")
    public String getSourcePath() {
        return sourcePath;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sourcePath")
    @JacksonXmlProperty(localName = "sourcePath")
    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    @JsonProperty("sourceName")
    @JacksonXmlProperty(localName = "sourceName")
    public String getSourceName() {
        return sourceName;
    }

    @JsonProperty("sourceName")
    @JacksonXmlProperty(localName = "sourceName")
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    @JsonProperty("targetPath")
    @JacksonXmlProperty(localName = "targetPath")
    public String getTargetPath() {
        return targetPath;
    }

    @JsonProperty("targetPath")
    @JacksonXmlProperty(localName = "targetPath")
    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    @JsonProperty("targetName")
    @JacksonXmlProperty(localName = "targetName")
    public String getTargetName() {
        return targetName;
    }

    @JsonProperty("targetName")
    @JacksonXmlProperty(localName = "targetName")
    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("surveyDate", surveyDate).append("id", id).append("transferId", transferId).append("interventionTransferId", interventionTransferId).append("state", state).append("integrityHash", integrityHash).append("modificationDate", modificationDate).append("size", size).append("error", error).append("sourcePath", sourcePath).append("sourceName", sourceName).append("targetPath", targetPath).append("targetName", targetName).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(interventionTransferId).append(targetName).append(surveyDate).append(targetPath).append(transferId).append(error).append(modificationDate).append(size).append(id).append(state).append(sourceName).append(integrityHash).append(sourcePath).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TransferFile) == false) {
            return false;
        }
        TransferFile rhs = ((TransferFile) other);
        return new EqualsBuilder().append(interventionTransferId, rhs.interventionTransferId).append(targetName, rhs.targetName).append(surveyDate, rhs.surveyDate).append(targetPath, rhs.targetPath).append(transferId, rhs.transferId).append(error, rhs.error).append(modificationDate, rhs.modificationDate).append(size, rhs.size).append(id, rhs.id).append(state, rhs.state).append(sourceName, rhs.sourceName).append(integrityHash, rhs.integrityHash).append(sourcePath, rhs.sourcePath).isEquals();
    }

}
