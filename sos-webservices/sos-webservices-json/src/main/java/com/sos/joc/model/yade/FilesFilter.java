
package com.sos.joc.model.yade;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@Generated("org.jsonschema2pojo")
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
    private String jobschedulerId;
    @JsonProperty("fileIds")
    private List<Long> fileIds = new ArrayList<Long>();
    @JsonProperty("transferIds")
    private List<Long> transferIds = new ArrayList<Long>();
    @JsonProperty("interventionTransferIds")
    private List<Long> interventionTransferIds = new ArrayList<Long>();
    /**
     * compact parameter
     * <p>
     * controls if the object view is compact or detailed
     * 
     */
    @JsonProperty("compact")
    private Boolean compact = false;
    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("regex")
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
    private Integer limit = 10000;

    /**
     * 
     * @return
     *     The jobschedulerId
     */
    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * 
     * @param jobschedulerId
     *     The jobschedulerId
     */
    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    /**
     * 
     * @return
     *     The fileIds
     */
    @JsonProperty("fileIds")
    public List<Long> getFileIds() {
        return fileIds;
    }

    /**
     * 
     * @param fileIds
     *     The fileIds
     */
    @JsonProperty("fileIds")
    public void setFileIds(List<Long> fileIds) {
        this.fileIds = fileIds;
    }

    /**
     * 
     * @return
     *     The transferIds
     */
    @JsonProperty("transferIds")
    public List<Long> getTransferIds() {
        return transferIds;
    }

    /**
     * 
     * @param transferIds
     *     The transferIds
     */
    @JsonProperty("transferIds")
    public void setTransferIds(List<Long> transferIds) {
        this.transferIds = transferIds;
    }

    /**
     * 
     * @return
     *     The interventionTransferIds
     */
    @JsonProperty("interventionTransferIds")
    public List<Long> getInterventionTransferIds() {
        return interventionTransferIds;
    }

    /**
     * 
     * @param interventionTransferIds
     *     The interventionTransferIds
     */
    @JsonProperty("interventionTransferIds")
    public void setInterventionTransferIds(List<Long> interventionTransferIds) {
        this.interventionTransferIds = interventionTransferIds;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object view is compact or detailed
     * 
     * @return
     *     The compact
     */
    @JsonProperty("compact")
    public Boolean getCompact() {
        return compact;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object view is compact or detailed
     * 
     * @param compact
     *     The compact
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
     * @return
     *     The regex
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
     * @param regex
     *     The regex
     */
    @JsonProperty("regex")
    public void setRegex(String regex) {
        this.regex = regex;
    }

    /**
     * 
     * @return
     *     The states
     */
    @JsonProperty("states")
    public List<FileTransferStateText> getStates() {
        return states;
    }

    /**
     * 
     * @param states
     *     The states
     */
    @JsonProperty("states")
    public void setStates(List<FileTransferStateText> states) {
        this.states = states;
    }

    /**
     * 
     * @return
     *     The sourceFiles
     */
    @JsonProperty("sourceFiles")
    public List<String> getSourceFiles() {
        return sourceFiles;
    }

    /**
     * 
     * @param sourceFiles
     *     The sourceFiles
     */
    @JsonProperty("sourceFiles")
    public void setSourceFiles(List<String> sourceFiles) {
        this.sourceFiles = sourceFiles;
    }

    /**
     * 
     * @return
     *     The targetFiles
     */
    @JsonProperty("targetFiles")
    public List<String> getTargetFiles() {
        return targetFiles;
    }

    /**
     * 
     * @param targetFiles
     *     The targetFiles
     */
    @JsonProperty("targetFiles")
    public void setTargetFiles(List<String> targetFiles) {
        this.targetFiles = targetFiles;
    }

    /**
     * only for db history urls to restrict the number of responsed records; -1=unlimited
     * 
     * @return
     *     The limit
     */
    @JsonProperty("limit")
    public Integer getLimit() {
        return limit;
    }

    /**
     * only for db history urls to restrict the number of responsed records; -1=unlimited
     * 
     * @param limit
     *     The limit
     */
    @JsonProperty("limit")
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobschedulerId).append(fileIds).append(transferIds).append(interventionTransferIds).append(compact).append(regex).append(states).append(sourceFiles).append(targetFiles).append(limit).toHashCode();
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
        return new EqualsBuilder().append(jobschedulerId, rhs.jobschedulerId).append(fileIds, rhs.fileIds).append(transferIds, rhs.transferIds).append(interventionTransferIds, rhs.interventionTransferIds).append(compact, rhs.compact).append(regex, rhs.regex).append(states, rhs.states).append(sourceFiles, rhs.sourceFiles).append(targetFiles, rhs.targetFiles).append(limit, rhs.limit).isEquals();
    }

}
