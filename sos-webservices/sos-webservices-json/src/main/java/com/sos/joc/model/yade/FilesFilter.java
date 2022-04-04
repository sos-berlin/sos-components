
package com.sos.joc.model.yade;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * file transfer files filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "transferIds",
    "states",
    "sourceFiles",
    "targetFiles",
    "sourceFile",
    "targetFile",
    "integrityHash",
    "limit"
})
public class FilesFilter {

    @JsonProperty("transferIds")
    private List<Long> transferIds = new ArrayList<Long>();
    @JsonProperty("states")
    private List<FileTransferStateText> states = new ArrayList<FileTransferStateText>();
    @JsonProperty("sourceFiles")
    private List<String> sourceFiles = new ArrayList<String>();
    @JsonProperty("targetFiles")
    private List<String> targetFiles = new ArrayList<String>();
    /**
     * glob pattern
     * <p>
     * pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character
     * 
     */
    @JsonProperty("sourceFile")
    @JsonPropertyDescription("pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character")
    private String sourceFile;
    /**
     * glob pattern
     * <p>
     * pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character
     * 
     */
    @JsonProperty("targetFile")
    @JsonPropertyDescription("pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character")
    private String targetFile;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("integrityHash")
    private String integrityHash;
    /**
     * only for db history urls to restrict the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    @JsonPropertyDescription("only for db history urls to restrict the number of responsed records; -1=unlimited")
    private Integer limit = 10000;

    @JsonProperty("transferIds")
    public List<Long> getTransferIds() {
        return transferIds;
    }

    @JsonProperty("transferIds")
    public void setTransferIds(List<Long> transferIds) {
        this.transferIds = transferIds;
    }

    @JsonProperty("states")
    public List<FileTransferStateText> getStates() {
        return states;
    }

    @JsonProperty("states")
    public void setStates(List<FileTransferStateText> states) {
        this.states = states;
    }

    @JsonProperty("sourceFiles")
    public List<String> getSourceFiles() {
        return sourceFiles;
    }

    @JsonProperty("sourceFiles")
    public void setSourceFiles(List<String> sourceFiles) {
        this.sourceFiles = sourceFiles;
    }

    @JsonProperty("targetFiles")
    public List<String> getTargetFiles() {
        return targetFiles;
    }

    @JsonProperty("targetFiles")
    public void setTargetFiles(List<String> targetFiles) {
        this.targetFiles = targetFiles;
    }

    /**
     * glob pattern
     * <p>
     * pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character
     * 
     */
    @JsonProperty("sourceFile")
    public String getSourceFile() {
        return sourceFile;
    }

    /**
     * glob pattern
     * <p>
     * pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character
     * 
     */
    @JsonProperty("sourceFile")
    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    /**
     * glob pattern
     * <p>
     * pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character
     * 
     */
    @JsonProperty("targetFile")
    public String getTargetFile() {
        return targetFile;
    }

    /**
     * glob pattern
     * <p>
     * pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character
     * 
     */
    @JsonProperty("targetFile")
    public void setTargetFile(String targetFile) {
        this.targetFile = targetFile;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("integrityHash")
    public String getIntegrityHash() {
        return integrityHash;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("integrityHash")
    public void setIntegrityHash(String integrityHash) {
        this.integrityHash = integrityHash;
    }

    /**
     * only for db history urls to restrict the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    public Integer getLimit() {
        return limit;
    }

    /**
     * only for db history urls to restrict the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("transferIds", transferIds).append("states", states).append("sourceFiles", sourceFiles).append("targetFiles", targetFiles).append("sourceFile", sourceFile).append("targetFile", targetFile).append("integrityHash", integrityHash).append("limit", limit).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(sourceFiles).append(targetFile).append(limit).append(targetFiles).append(integrityHash).append(sourceFile).append(transferIds).append(states).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FilesFilter) == false) {
            return false;
        }
        FilesFilter rhs = ((FilesFilter) other);
        return new EqualsBuilder().append(sourceFiles, rhs.sourceFiles).append(targetFile, rhs.targetFile).append(limit, rhs.limit).append(targetFiles, rhs.targetFiles).append(integrityHash, rhs.integrityHash).append(sourceFile, rhs.sourceFile).append(transferIds, rhs.transferIds).append(states, rhs.states).isEquals();
    }

}
