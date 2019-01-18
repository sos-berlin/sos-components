
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
    private Boolean compact = false;
    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("regex")
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
    private String timeZone;
    /**
     * only for db history urls to restrict the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
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
    private String sourceFilesRegex;
    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("targetFilesRegex")
    private String targetFilesRegex;
    @JsonProperty("sourceFiles")
    private List<String> sourceFiles = new ArrayList<String>();
    @JsonProperty("targetFiles")
    private List<String> targetFiles = new ArrayList<String>();

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
     *     The profiles
     */
    @JsonProperty("profiles")
    public List<String> getProfiles() {
        return profiles;
    }

    /**
     * 
     * @param profiles
     *     The profiles
     */
    @JsonProperty("profiles")
    public void setProfiles(List<String> profiles) {
        this.profiles = profiles;
    }

    /**
     * 
     * @return
     *     The mandator
     */
    @JsonProperty("mandator")
    public String getMandator() {
        return mandator;
    }

    /**
     * 
     * @param mandator
     *     The mandator
     */
    @JsonProperty("mandator")
    public void setMandator(String mandator) {
        this.mandator = mandator;
    }

    /**
     * 
     * @return
     *     The states
     */
    @JsonProperty("states")
    public List<TransferStateText> getStates() {
        return states;
    }

    /**
     * 
     * @param states
     *     The states
     */
    @JsonProperty("states")
    public void setStates(List<TransferStateText> states) {
        this.states = states;
    }

    /**
     * 
     * @return
     *     The operations
     */
    @JsonProperty("operations")
    public List<Operation> getOperations() {
        return operations;
    }

    /**
     * 
     * @param operations
     *     The operations
     */
    @JsonProperty("operations")
    public void setOperations(List<Operation> operations) {
        this.operations = operations;
    }

    /**
     * 
     * @return
     *     The dateFrom
     */
    @JsonProperty("dateFrom")
    public String getDateFrom() {
        return dateFrom;
    }

    /**
     * 
     * @param dateFrom
     *     The dateFrom
     */
    @JsonProperty("dateFrom")
    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    /**
     * 
     * @return
     *     The dateTo
     */
    @JsonProperty("dateTo")
    public String getDateTo() {
        return dateTo;
    }

    /**
     * 
     * @param dateTo
     *     The dateTo
     */
    @JsonProperty("dateTo")
    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     * @return
     *     The timeZone
     */
    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     * @param timeZone
     *     The timeZone
     */
    @JsonProperty("timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
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

    /**
     * 
     * @return
     *     The hasIntervention
     */
    @JsonProperty("hasIntervention")
    public Boolean getHasIntervention() {
        return hasIntervention;
    }

    /**
     * 
     * @param hasIntervention
     *     The hasIntervention
     */
    @JsonProperty("hasIntervention")
    public void setHasIntervention(Boolean hasIntervention) {
        this.hasIntervention = hasIntervention;
    }

    /**
     * 
     * @return
     *     The isIntervention
     */
    @JsonProperty("isIntervention")
    public Boolean getIsIntervention() {
        return isIntervention;
    }

    /**
     * 
     * @param isIntervention
     *     The isIntervention
     */
    @JsonProperty("isIntervention")
    public void setIsIntervention(Boolean isIntervention) {
        this.isIntervention = isIntervention;
    }

    /**
     * 
     * @return
     *     The sources
     */
    @JsonProperty("sources")
    public List<ProtocolFragment> getSources() {
        return sources;
    }

    /**
     * 
     * @param sources
     *     The sources
     */
    @JsonProperty("sources")
    public void setSources(List<ProtocolFragment> sources) {
        this.sources = sources;
    }

    /**
     * 
     * @return
     *     The targets
     */
    @JsonProperty("targets")
    public List<ProtocolFragment> getTargets() {
        return targets;
    }

    /**
     * 
     * @param targets
     *     The targets
     */
    @JsonProperty("targets")
    public void setTargets(List<ProtocolFragment> targets) {
        this.targets = targets;
    }

    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     * @return
     *     The sourceFilesRegex
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
     * @param sourceFilesRegex
     *     The sourceFilesRegex
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
     * @return
     *     The targetFilesRegex
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
     * @param targetFilesRegex
     *     The targetFilesRegex
     */
    @JsonProperty("targetFilesRegex")
    public void setTargetFilesRegex(String targetFilesRegex) {
        this.targetFilesRegex = targetFilesRegex;
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

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobschedulerId).append(transferIds).append(compact).append(regex).append(profiles).append(mandator).append(states).append(operations).append(dateFrom).append(dateTo).append(timeZone).append(limit).append(hasIntervention).append(isIntervention).append(sources).append(targets).append(sourceFilesRegex).append(targetFilesRegex).append(sourceFiles).append(targetFiles).toHashCode();
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
        return new EqualsBuilder().append(jobschedulerId, rhs.jobschedulerId).append(transferIds, rhs.transferIds).append(compact, rhs.compact).append(regex, rhs.regex).append(profiles, rhs.profiles).append(mandator, rhs.mandator).append(states, rhs.states).append(operations, rhs.operations).append(dateFrom, rhs.dateFrom).append(dateTo, rhs.dateTo).append(timeZone, rhs.timeZone).append(limit, rhs.limit).append(hasIntervention, rhs.hasIntervention).append(isIntervention, rhs.isIntervention).append(sources, rhs.sources).append(targets, rhs.targets).append(sourceFilesRegex, rhs.sourceFilesRegex).append(targetFilesRegex, rhs.targetFilesRegex).append(sourceFiles, rhs.sourceFiles).append(targetFiles, rhs.targetFiles).isEquals();
    }

}
