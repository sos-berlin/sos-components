
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
    "jobschedulerId",
    "transferIds",
    "compact",
    "regex",
    "profiles",
    "mandator",
    "states",
    "operations",
    "dateFrom",
    "dateTo",
    "timeZone",
    "limit",
    "hasIntervention",
    "isIntervention",
    "sources",
    "targets",
    "sourceFilesRegex",
    "targetFilesRegex",
    "sourceFiles",
    "targetFiles"
})
public class TransferFilter {

    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    @JsonProperty("transferIds")
    private List<Long> transferIds = new ArrayList<Long>();
    /**
     * compact parameter
     * <p>
     * controls if the object view is compact or detailed
     * 
     */
    @JsonProperty("compact")
    @JsonPropertyDescription("controls if the object view is compact or detailed")
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
    @JsonProperty("profiles")
    private List<String> profiles = new ArrayList<String>();
    @JsonProperty("mandator")
    private String mandator;
    @JsonProperty("states")
    private List<TransferStateText> states = new ArrayList<TransferStateText>();
    @JsonProperty("operations")
    private List<Operation> operations = new ArrayList<Operation>();
    @JsonProperty("dateFrom")
    private String dateFrom;
    @JsonProperty("dateTo")
    private String dateTo;
    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    @JsonPropertyDescription("see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones")
    private String timeZone;
    /**
     * only for db history urls to restrict the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    @JsonPropertyDescription("only for db history urls to restrict the number of responsed records; -1=unlimited")
    private Integer limit = 10000;
    @JsonProperty("hasIntervention")
    private Boolean hasIntervention;
    @JsonProperty("isIntervention")
    private Boolean isIntervention;
    @JsonProperty("sources")
    private List<ProtocolFragment> sources = new ArrayList<ProtocolFragment>();
    @JsonProperty("targets")
    private List<ProtocolFragment> targets = new ArrayList<ProtocolFragment>();
    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("sourceFilesRegex")
    @JsonPropertyDescription("regular expression to filter JobScheduler objects by matching the path")
    private String sourceFilesRegex;
    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("targetFilesRegex")
    @JsonPropertyDescription("regular expression to filter JobScheduler objects by matching the path")
    private String targetFilesRegex;
    @JsonProperty("sourceFiles")
    private List<String> sourceFiles = new ArrayList<String>();
    @JsonProperty("targetFiles")
    private List<String> targetFiles = new ArrayList<String>();

    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    @JsonProperty("transferIds")
    public List<Long> getTransferIds() {
        return transferIds;
    }

    @JsonProperty("transferIds")
    public void setTransferIds(List<Long> transferIds) {
        this.transferIds = transferIds;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object view is compact or detailed
     * 
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

    @JsonProperty("profiles")
    public List<String> getProfiles() {
        return profiles;
    }

    @JsonProperty("profiles")
    public void setProfiles(List<String> profiles) {
        this.profiles = profiles;
    }

    @JsonProperty("mandator")
    public String getMandator() {
        return mandator;
    }

    @JsonProperty("mandator")
    public void setMandator(String mandator) {
        this.mandator = mandator;
    }

    @JsonProperty("states")
    public List<TransferStateText> getStates() {
        return states;
    }

    @JsonProperty("states")
    public void setStates(List<TransferStateText> states) {
        this.states = states;
    }

    @JsonProperty("operations")
    public List<Operation> getOperations() {
        return operations;
    }

    @JsonProperty("operations")
    public void setOperations(List<Operation> operations) {
        this.operations = operations;
    }

    @JsonProperty("dateFrom")
    public String getDateFrom() {
        return dateFrom;
    }

    @JsonProperty("dateFrom")
    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    @JsonProperty("dateTo")
    public String getDateTo() {
        return dateTo;
    }

    @JsonProperty("dateTo")
    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
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

    @JsonProperty("hasIntervention")
    public Boolean getHasIntervention() {
        return hasIntervention;
    }

    @JsonProperty("hasIntervention")
    public void setHasIntervention(Boolean hasIntervention) {
        this.hasIntervention = hasIntervention;
    }

    @JsonProperty("isIntervention")
    public Boolean getIsIntervention() {
        return isIntervention;
    }

    @JsonProperty("isIntervention")
    public void setIsIntervention(Boolean isIntervention) {
        this.isIntervention = isIntervention;
    }

    @JsonProperty("sources")
    public List<ProtocolFragment> getSources() {
        return sources;
    }

    @JsonProperty("sources")
    public void setSources(List<ProtocolFragment> sources) {
        this.sources = sources;
    }

    @JsonProperty("targets")
    public List<ProtocolFragment> getTargets() {
        return targets;
    }

    @JsonProperty("targets")
    public void setTargets(List<ProtocolFragment> targets) {
        this.targets = targets;
    }

    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("sourceFilesRegex")
    public String getSourceFilesRegex() {
        return sourceFilesRegex;
    }

    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("sourceFilesRegex")
    public void setSourceFilesRegex(String sourceFilesRegex) {
        this.sourceFilesRegex = sourceFilesRegex;
    }

    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("targetFilesRegex")
    public String getTargetFilesRegex() {
        return targetFilesRegex;
    }

    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("targetFilesRegex")
    public void setTargetFilesRegex(String targetFilesRegex) {
        this.targetFilesRegex = targetFilesRegex;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("transferIds", transferIds).append("compact", compact).append("regex", regex).append("profiles", profiles).append("mandator", mandator).append("states", states).append("operations", operations).append("dateFrom", dateFrom).append("dateTo", dateTo).append("timeZone", timeZone).append("limit", limit).append("hasIntervention", hasIntervention).append("isIntervention", isIntervention).append("sources", sources).append("targets", targets).append("sourceFilesRegex", sourceFilesRegex).append("targetFilesRegex", targetFilesRegex).append("sourceFiles", sourceFiles).append("targetFiles", targetFiles).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(mandator).append(compact).append(sources).append(profiles).append(timeZone).append(isIntervention).append(dateFrom).append(targets).append(transferIds).append(hasIntervention).append(states).append(regex).append(operations).append(sourceFilesRegex).append(sourceFiles).append(dateTo).append(limit).append(targetFilesRegex).append(jobschedulerId).append(targetFiles).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TransferFilter) == false) {
            return false;
        }
        TransferFilter rhs = ((TransferFilter) other);
        return new EqualsBuilder().append(mandator, rhs.mandator).append(compact, rhs.compact).append(sources, rhs.sources).append(profiles, rhs.profiles).append(timeZone, rhs.timeZone).append(isIntervention, rhs.isIntervention).append(dateFrom, rhs.dateFrom).append(targets, rhs.targets).append(transferIds, rhs.transferIds).append(hasIntervention, rhs.hasIntervention).append(states, rhs.states).append(regex, rhs.regex).append(operations, rhs.operations).append(sourceFilesRegex, rhs.sourceFilesRegex).append(sourceFiles, rhs.sourceFiles).append(dateTo, rhs.dateTo).append(limit, rhs.limit).append(targetFilesRegex, rhs.targetFilesRegex).append(jobschedulerId, rhs.jobschedulerId).append(targetFiles, rhs.targetFiles).isEquals();
    }

}
