
package com.sos.joc.model.yade;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
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
    "jobschedulerId",
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

    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    private String jobschedulerId;
    @JsonProperty("fileIds")
    @JacksonXmlProperty(localName = "fileId")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "fileIds")
    private List<Long> fileIds = new ArrayList<Long>();
    @JsonProperty("transferIds")
    @JacksonXmlProperty(localName = "transferId")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "transferIds")
    private List<Long> transferIds = new ArrayList<Long>();
    @JsonProperty("interventionTransferIds")
    @JacksonXmlProperty(localName = "interventionTransferId")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "interventionTransferIds")
    private List<Long> interventionTransferIds = new ArrayList<Long>();
    /**
     * compact parameter
     * <p>
     * controls if the object view is compact or detailed
     * 
     */
    @JsonProperty("compact")
    @JsonPropertyDescription("controls if the object view is compact or detailed")
    @JacksonXmlProperty(localName = "compact")
    private Boolean compact = false;
    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("regex")
    @JsonPropertyDescription("regular expression to filter JobScheduler objects by matching the path")
    @JacksonXmlProperty(localName = "regex")
    private String regex;
    @JsonProperty("states")
    @JacksonXmlProperty(localName = "state")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "states")
    private List<FileTransferStateText> states = new ArrayList<FileTransferStateText>();
    @JsonProperty("sourceFiles")
    @JacksonXmlProperty(localName = "sourceFile")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "sourceFiles")
    private List<String> sourceFiles = new ArrayList<String>();
    @JsonProperty("targetFiles")
    @JacksonXmlProperty(localName = "targetFile")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "targetFiles")
    private List<String> targetFiles = new ArrayList<String>();
    /**
     * only for db history urls to restrict the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    @JsonPropertyDescription("only for db history urls to restrict the number of responsed records; -1=unlimited")
    @JacksonXmlProperty(localName = "limit")
    private Integer limit = 10000;

    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
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

    @JsonProperty("transferIds")
    @JacksonXmlProperty(localName = "transferId")
    public List<Long> getTransferIds() {
        return transferIds;
    }

    @JsonProperty("transferIds")
    @JacksonXmlProperty(localName = "transferId")
    public void setTransferIds(List<Long> transferIds) {
        this.transferIds = transferIds;
    }

    @JsonProperty("interventionTransferIds")
    @JacksonXmlProperty(localName = "interventionTransferId")
    public List<Long> getInterventionTransferIds() {
        return interventionTransferIds;
    }

    @JsonProperty("interventionTransferIds")
    @JacksonXmlProperty(localName = "interventionTransferId")
    public void setInterventionTransferIds(List<Long> interventionTransferIds) {
        this.interventionTransferIds = interventionTransferIds;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object view is compact or detailed
     * 
     */
    @JsonProperty("compact")
    @JacksonXmlProperty(localName = "compact")
    public Boolean getCompact() {
        return compact;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object view is compact or detailed
     * 
     */
    @JsonProperty("compact")
    @JacksonXmlProperty(localName = "compact")
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
    @JacksonXmlProperty(localName = "regex")
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
    @JacksonXmlProperty(localName = "regex")
    public void setRegex(String regex) {
        this.regex = regex;
    }

    @JsonProperty("states")
    @JacksonXmlProperty(localName = "state")
    public List<FileTransferStateText> getStates() {
        return states;
    }

    @JsonProperty("states")
    @JacksonXmlProperty(localName = "state")
    public void setStates(List<FileTransferStateText> states) {
        this.states = states;
    }

    @JsonProperty("sourceFiles")
    @JacksonXmlProperty(localName = "sourceFile")
    public List<String> getSourceFiles() {
        return sourceFiles;
    }

    @JsonProperty("sourceFiles")
    @JacksonXmlProperty(localName = "sourceFile")
    public void setSourceFiles(List<String> sourceFiles) {
        this.sourceFiles = sourceFiles;
    }

    @JsonProperty("targetFiles")
    @JacksonXmlProperty(localName = "targetFile")
    public List<String> getTargetFiles() {
        return targetFiles;
    }

    @JsonProperty("targetFiles")
    @JacksonXmlProperty(localName = "targetFile")
    public void setTargetFiles(List<String> targetFiles) {
        this.targetFiles = targetFiles;
    }

    /**
     * only for db history urls to restrict the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    @JacksonXmlProperty(localName = "limit")
    public Integer getLimit() {
        return limit;
    }

    /**
     * only for db history urls to restrict the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    @JacksonXmlProperty(localName = "limit")
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("fileIds", fileIds).append("transferIds", transferIds).append("interventionTransferIds", interventionTransferIds).append("compact", compact).append("regex", regex).append("states", states).append("sourceFiles", sourceFiles).append("targetFiles", targetFiles).append("limit", limit).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(regex).append(sourceFiles).append(compact).append(fileIds).append(limit).append(interventionTransferIds).append(jobschedulerId).append(targetFiles).append(transferIds).append(states).toHashCode();
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
        return new EqualsBuilder().append(regex, rhs.regex).append(sourceFiles, rhs.sourceFiles).append(compact, rhs.compact).append(fileIds, rhs.fileIds).append(limit, rhs.limit).append(interventionTransferIds, rhs.interventionTransferIds).append(jobschedulerId, rhs.jobschedulerId).append(targetFiles, rhs.targetFiles).append(transferIds, rhs.transferIds).append(states, rhs.states).isEquals();
    }

}
