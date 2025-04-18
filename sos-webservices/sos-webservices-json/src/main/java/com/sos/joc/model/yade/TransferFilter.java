
package com.sos.joc.model.yade;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * file transfer filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "compact",
    "regex",
    "profiles",
    "states",
    "operations",
    "numOfFilesFrom",
    "numOfFilesTo",
    "dateFrom",
    "dateTo",
    "timeZone",
    "limit",
    "sources",
    "targets",
    "sourceFiles",
    "targetFiles",
    "workflowNames"
})
public class TransferFilter {

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
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
     * regular expression to filter Controller objects by matching the path
     * 
     */
    @JsonProperty("regex")
    @JsonPropertyDescription("regular expression to filter Controller objects by matching the path")
    private String regex;
    @JsonProperty("profiles")
    private List<String> profiles = new ArrayList<String>();
    @JsonProperty("states")
    private List<TransferStateText> states = new ArrayList<TransferStateText>();
    @JsonProperty("operations")
    private List<Operation> operations = new ArrayList<Operation>();
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfFilesFrom")
    private Long numOfFilesFrom;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfFilesTo")
    private Long numOfFilesTo;
    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateFrom")
    @JsonPropertyDescription("0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp")
    private String dateFrom;
    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateTo")
    @JsonPropertyDescription("0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp")
    private String dateTo;
    /**
     * string without < and >
     * <p>
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
    @JsonProperty("sources")
    private List<ProtocolFragment> sources = new ArrayList<ProtocolFragment>();
    @JsonProperty("targets")
    private List<ProtocolFragment> targets = new ArrayList<ProtocolFragment>();
    @JsonProperty("sourceFiles")
    private List<String> sourceFiles = new ArrayList<String>();
    @JsonProperty("targetFiles")
    private List<String> targetFiles = new ArrayList<String>();
    @JsonProperty("workflowNames")
    private List<String> workflowNames = new ArrayList<String>();

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
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
     * regular expression to filter Controller objects by matching the path
     * 
     */
    @JsonProperty("regex")
    public String getRegex() {
        return regex;
    }

    /**
     * filter with regex
     * <p>
     * regular expression to filter Controller objects by matching the path
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

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfFilesFrom")
    public Long getNumOfFilesFrom() {
        return numOfFilesFrom;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfFilesFrom")
    public void setNumOfFilesFrom(Long numOfFilesFrom) {
        this.numOfFilesFrom = numOfFilesFrom;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfFilesTo")
    public Long getNumOfFilesTo() {
        return numOfFilesTo;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfFilesTo")
    public void setNumOfFilesTo(Long numOfFilesTo) {
        this.numOfFilesTo = numOfFilesTo;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateFrom")
    public String getDateFrom() {
        return dateFrom;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateFrom")
    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateTo")
    public String getDateTo() {
        return dateTo;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateTo")
    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    /**
     * string without < and >
     * <p>
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * string without < and >
     * <p>
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

    @JsonProperty("workflowNames")
    public List<String> getWorkflowNames() {
        return workflowNames;
    }

    @JsonProperty("workflowNames")
    public void setWorkflowNames(List<String> workflowNames) {
        this.workflowNames = workflowNames;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("compact", compact).append("regex", regex).append("profiles", profiles).append("states", states).append("operations", operations).append("numOfFilesFrom", numOfFilesFrom).append("numOfFilesTo", numOfFilesTo).append("dateFrom", dateFrom).append("dateTo", dateTo).append("timeZone", timeZone).append("limit", limit).append("sources", sources).append("targets", targets).append("sourceFiles", sourceFiles).append("targetFiles", targetFiles).append("workflowNames", workflowNames).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(compact).append(sources).append(profiles).append(timeZone).append(dateFrom).append(numOfFilesTo).append(targets).append(numOfFilesFrom).append(states).append(workflowNames).append(regex).append(operations).append(sourceFiles).append(dateTo).append(limit).append(targetFiles).toHashCode();
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
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(compact, rhs.compact).append(sources, rhs.sources).append(profiles, rhs.profiles).append(timeZone, rhs.timeZone).append(dateFrom, rhs.dateFrom).append(numOfFilesTo, rhs.numOfFilesTo).append(targets, rhs.targets).append(numOfFilesFrom, rhs.numOfFilesFrom).append(states, rhs.states).append(workflowNames, rhs.workflowNames).append(regex, rhs.regex).append(operations, rhs.operations).append(sourceFiles, rhs.sourceFiles).append(dateTo, rhs.dateTo).append(limit, rhs.limit).append(targetFiles, rhs.targetFiles).isEquals();
    }

}
