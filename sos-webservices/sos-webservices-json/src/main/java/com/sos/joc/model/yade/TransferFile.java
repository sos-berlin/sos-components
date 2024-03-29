
package com.sos.joc.model.yade;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.Err;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


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
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("surveyDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date surveyDate;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    private Long id;
    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("transferId")
    private Long transferId;
    /**
     * state for each transferred file
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    private FileTransferState state;
    @JsonProperty("integrityHash")
    private String integrityHash;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("modificationDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date modificationDate;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("size")
    private Long size;
    /**
     * error
     * <p>
     * 
     * 
     */
    @JsonProperty("error")
    private Err error;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sourcePath")
    private String sourcePath;
    @JsonProperty("sourceName")
    private String sourceName;
    @JsonProperty("targetPath")
    private String targetPath;
    @JsonProperty("targetName")
    private String targetName;

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("surveyDate")
    public Date getSurveyDate() {
        return surveyDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("surveyDate")
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

    /**
     * state for each transferred file
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
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
    public void setState(FileTransferState state) {
        this.state = state;
    }

    @JsonProperty("integrityHash")
    public String getIntegrityHash() {
        return integrityHash;
    }

    @JsonProperty("integrityHash")
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
    public void setError(Err error) {
        this.error = error;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sourcePath")
    public String getSourcePath() {
        return sourcePath;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sourcePath")
    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    @JsonProperty("sourceName")
    public String getSourceName() {
        return sourceName;
    }

    @JsonProperty("sourceName")
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    @JsonProperty("targetPath")
    public String getTargetPath() {
        return targetPath;
    }

    @JsonProperty("targetPath")
    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    @JsonProperty("targetName")
    public String getTargetName() {
        return targetName;
    }

    @JsonProperty("targetName")
    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("surveyDate", surveyDate).append("id", id).append("transferId", transferId).append("state", state).append("integrityHash", integrityHash).append("modificationDate", modificationDate).append("size", size).append("error", error).append("sourcePath", sourcePath).append("sourceName", sourceName).append("targetPath", targetPath).append("targetName", targetName).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(targetName).append(surveyDate).append(targetPath).append(transferId).append(error).append(modificationDate).append(size).append(id).append(state).append(sourceName).append(integrityHash).append(sourcePath).toHashCode();
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
        return new EqualsBuilder().append(targetName, rhs.targetName).append(surveyDate, rhs.surveyDate).append(targetPath, rhs.targetPath).append(transferId, rhs.transferId).append(error, rhs.error).append(modificationDate, rhs.modificationDate).append(size, rhs.size).append(id, rhs.id).append(state, rhs.state).append(sourceName, rhs.sourceName).append(integrityHash, rhs.integrityHash).append(sourcePath, rhs.sourcePath).isEquals();
    }

}
