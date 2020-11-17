
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
 * yade filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "fileIds",
    "transferIds",
    "interventionTransferIds",
    "compact",
    "regex",
    "states",
    "sourceFiles",
    "targetFiles",
    "limit"
})
public class FilesFilter {

    @JsonProperty("controllerId")
    private String controllerId;
    @JsonProperty("fileIds")
    private List<Long> fileIds = new ArrayList<Long>();
    @JsonProperty("transferIds")
    private List<Long> transferIds = new ArrayList<Long>();
    @JsonProperty("interventionTransferIds")
    private List<Long> interventionTransferIds = new ArrayList<Long>();
    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    @JsonPropertyDescription("controls if the object's data is compact or detailed")
    private Boolean compact = false;
    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("regex")
    @JsonPropertyDescription("regular expression to filter JobScheduler objects by matching the path")
    private String regex;
    @JsonProperty("states")
    private List<FileTransferStateText> states = new ArrayList<FileTransferStateText>();
    @JsonProperty("sourceFiles")
    private List<String> sourceFiles = new ArrayList<String>();
    @JsonProperty("targetFiles")
    private List<String> targetFiles = new ArrayList<String>();
    /**
     * only for db history urls to restrict the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    @JsonPropertyDescription("only for db history urls to restrict the number of responsed records; -1=unlimited")
    private Integer limit = 10000;

    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    @JsonProperty("fileIds")
    public List<Long> getFileIds() {
        return fileIds;
    }

    @JsonProperty("fileIds")
    public void setFileIds(List<Long> fileIds) {
        this.fileIds = fileIds;
    }

    @JsonProperty("transferIds")
    public List<Long> getTransferIds() {
        return transferIds;
    }

    @JsonProperty("transferIds")
    public void setTransferIds(List<Long> transferIds) {
        this.transferIds = transferIds;
    }

    @JsonProperty("interventionTransferIds")
    public List<Long> getInterventionTransferIds() {
        return interventionTransferIds;
    }

    @JsonProperty("interventionTransferIds")
    public void setInterventionTransferIds(List<Long> interventionTransferIds) {
        this.interventionTransferIds = interventionTransferIds;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    public Boolean getCompact() {
        return compact;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    public void setCompact(Boolean compact) {
        this.compact = compact;
    }

    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("regex")
    public String getRegex() {
        return regex;
    }

    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("regex")
    public void setRegex(String regex) {
        this.regex = regex;
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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("fileIds", fileIds).append("transferIds", transferIds).append("interventionTransferIds", interventionTransferIds).append("compact", compact).append("regex", regex).append("states", states).append("sourceFiles", sourceFiles).append("targetFiles", targetFiles).append("limit", limit).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(regex).append(sourceFiles).append(controllerId).append(compact).append(fileIds).append(limit).append(interventionTransferIds).append(targetFiles).append(transferIds).append(states).toHashCode();
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
        return new EqualsBuilder().append(regex, rhs.regex).append(sourceFiles, rhs.sourceFiles).append(controllerId, rhs.controllerId).append(compact, rhs.compact).append(fileIds, rhs.fileIds).append(limit, rhs.limit).append(interventionTransferIds, rhs.interventionTransferIds).append(targetFiles, rhs.targetFiles).append(transferIds, rhs.transferIds).append(states, rhs.states).isEquals();
    }

}
